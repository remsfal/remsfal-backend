package de.remsfal.ticketing.control;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.ticketing.TenantTimelineJson;
import de.remsfal.core.model.UserContext;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.ticketing.boundary.eventing.IssueEventProducer;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dao.TenantTimelineRepository;
import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TenantTimelineController {

    @Inject
    Logger logger;

    @Inject
    TenantTimelineRepository timelineRepository;

    @Inject
    IssueRepository issueRepository;

    @Inject
    IssueEventProducer issueEventProducer;

    @Inject
    QuotationRequestRepository quotationRequestRepository;

    @Inject
    RemsfalPrincipal principal;

    public List<TenantTimelineEntity> getTimelineEntries(
        final UUID tenancyId,
        final UUID issueId,
        final UUID projectId) {
        logger.infov("Retrieving timeline entries (issueId={0}, projectId={1}, tenancyId={2})",
            issueId, projectId, tenancyId);
        return timelineRepository.findByIssue(tenancyId, issueId, projectId);
    }

    /**
     * Attachments are only visible to a tenant if they are referenced by some
     * {@link TenantTimelineEntity} of the issue — this computes that visible set.
     */
    public Set<UUID> getVisibleAttachmentIds(final UUID tenancyId, final UUID issueId, final UUID projectId) {
        return getTimelineEntries(tenancyId, issueId, projectId).stream()
            .map(TenantTimelineEntity::getAttachmentIds)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    }

    @Transactional
    public TenantTimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UserModel sender, final UserContext contextRole, final TenantTimelineJson timeline,
        final List<UUID> attachmentIds) {
        return createTimelineEntry(tenancyId, issueId, projectId, sender, contextRole,
            timeline.getPurpose(), timeline.getMessage(), attachmentIds);
    }

    @Transactional
    public TenantTimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UserModel sender, final UserContext contextRole, final MessagePurpose purpose, final String message) {
        return createTimelineEntry(tenancyId, issueId, projectId, sender, contextRole, purpose, message, null);
    }

    @Transactional
    public TenantTimelineEntity createTimelineEntry(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UserModel sender, final UserContext contextRole, final MessagePurpose purpose, final String message,
        final List<UUID> attachmentIds) {
        logger.infov("Creating timeline entry (issueId={0}, projectId={1}, tenancyId={2})",
            issueId, projectId, tenancyId);

        final TenantTimelineKey key = new TenantTimelineKey();
        key.setTenancyId(tenancyId);
        key.setIssueId(issueId);
        key.setProjectId(projectId);
        key.setTimelineId(UUIDv7.randomUUID());

        final TenantTimelineEntity entity = new TenantTimelineEntity();
        entity.setKey(key);
        entity.setAttachmentIds(attachmentIds);
        entity.setSenderId(sender.getId());
        entity.setSenderName(sender.getName());
        entity.setSenderRole(resolveSenderRole(tenancyId, issueId, projectId, contextRole));
        entity.setPurpose(purpose);
        entity.setMessage(message);

        final Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);

        final TenantTimelineEntity created = timelineRepository.insert(entity);

        final IssueModel issue = issueRepository.findByIssueId(issueId).orElse(null);
        issueEventProducer.sendTimelineEntryCreated(issue, TenantTimelineJson.valueOf(created), sender);

        return created;
    }

    /**
     * Derives the sender role from the claims of the caller's JWT. The endpoint context
     * ({@code contextRole}) only decides if the token grants more than one role for this issue.
     */
    private UserContext resolveSenderRole(final UUID tenancyId, final UUID issueId, final UUID projectId,
        final UserContext contextRole) {
        final Set<UserContext> tokenRoles = resolveTokenRoles(tenancyId, issueId, projectId);
        if (tokenRoles.size() == 1) {
            return tokenRoles.iterator().next();
        }
        if (contextRole != null && tokenRoles.contains(contextRole)) {
            return contextRole;
        }
        logger.warnv("Unable to resolve sender role from token (issueId={0}, tokenRoles={1}, contextRole={2})",
            issueId, tokenRoles, contextRole);
        return null;
    }

    private Set<UserContext> resolveTokenRoles(final UUID tenancyId, final UUID issueId, final UUID projectId) {
        final Set<UserContext> roles = EnumSet.noneOf(UserContext.class);
        if (projectId != null && principal.getProjectRole(projectId) != null) {
            roles.add(UserContext.MANAGER);
        }
        if (tenancyId != null && projectId != null && projectId.equals(principal.getTenancyProject(tenancyId))) {
            roles.add(UserContext.TENANT);
        }
        final Set<UUID> organizationIds = principal.getOrganizationRoles().keySet();
        if (!organizationIds.isEmpty() && quotationRequestRepository.findByIssueId(issueId).stream()
            .anyMatch(request -> organizationIds.contains(request.getOrganizationId()))) {
            roles.add(UserContext.CONTRACTOR);
        }
        return roles;
    }

}
