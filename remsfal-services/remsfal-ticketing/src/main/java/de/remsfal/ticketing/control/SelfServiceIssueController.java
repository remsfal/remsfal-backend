package de.remsfal.ticketing.control;

import org.jboss.logging.Logger;

import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.eventing.AffectedTenantJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.filter.IssueFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Creates the self-service issues a manager has to approve when a tenant or contractor changes their own data.
 * <p>
 * There is at most one open self-service issue per tenant and per contractor of a project. A further change
 * while such an issue is still open is merged into it: since the update always carries the complete current
 * data set, merging replaces the stored update with the newer one.
 */
@ApplicationScoped
public class SelfServiceIssueController {

    private static final String SELF_SERVICE_ISSUE_TITLE = "Selbstständige Datensatzänderung";

    /** Statuses in which a self-service issue still awaits a decision of the manager. */
    static final List<IssueStatus> OPEN_STATUSES =
        List.of(IssueStatus.PENDING, IssueStatus.OPEN, IssueStatus.IN_PROGRESS);

    /** Upper bound for the open self-service issues of one project that are checked for a merge. */
    static final int OPEN_ISSUE_LOOKUP_LIMIT = 1000;

    @Inject
    IssueRepository issueRepository;

    @Inject
    Logger logger;

    public void createIssueForAffectedTenant(final UUID reporterId, final UserJson reporter,
        final AffectedTenantJson affectedTenant) {
        final UUID tenantId = affectedTenant.getTenantId();
        final Optional<IssueEntity> openIssue = findOpenIssue(affectedTenant.getProjectId(),
            issue -> issue.getTenantUpdate() != null && tenantId.equals(issue.getTenantUpdate().getId()));
        final IssueEntity entity = openIssue.orElseGet(() -> newIssue(affectedTenant.getProjectId()));
        entity.setReporterId(reporterId);
        entity.setReportedBy(reporter.getName());
        entity.setTenantUpdate(TenantJson.valueOf(reporter, tenantId));
        if (openIssue.isPresent()) {
            issueRepository.update(entity);
            logger.infov("Merged tenant change into open self-service issue (issueId={0}, tenantId={1})",
                entity.getId(), tenantId);
        } else {
            issueRepository.insert(entity);
            logger.infov("Created self-service issue for affected tenant (tenantId={0}, projectId={1})",
                tenantId, affectedTenant.getProjectId());
        }
    }

    public void createIssueForAffectedContractor(final UUID changedByUserId, final String changedByName,
        final OrganizationJson organization, final AffectedContractorJson affectedContractor) {
        final UUID contractorId = affectedContractor.getContractorId();
        final Optional<IssueEntity> openIssue = findOpenIssue(affectedContractor.getProjectId(),
            issue -> issue.getContractorUpdate() != null
                && contractorId.equals(issue.getContractorUpdate().getId()));
        final IssueEntity entity = openIssue.orElseGet(() -> newIssue(affectedContractor.getProjectId()));
        entity.setReporterId(changedByUserId);
        entity.setReportedBy(changedByName);
        entity.setContractorUpdate(ContractorJson.valueOf(organization, contractorId));
        if (openIssue.isPresent()) {
            issueRepository.update(entity);
            logger.infov("Merged contractor change into open self-service issue (issueId={0}, contractorId={1})",
                entity.getId(), contractorId);
        } else {
            issueRepository.insert(entity);
            logger.infov("Created self-service issue for affected contractor (contractorId={0}, projectId={1})",
                contractorId, affectedContractor.getProjectId());
        }
    }

    private Optional<IssueEntity> findOpenIssue(final UUID projectId, final Predicate<IssueEntity> concerns) {
        final IssueFilter filter = new IssueFilter(projectId, null, null, null, null,
            List.of(IssueType.SELF_SERVICE), OPEN_STATUSES, null);
        return issueRepository.findByQuery(filter, null, OPEN_ISSUE_LOOKUP_LIMIT).stream()
            .filter(concerns)
            .findFirst();
    }

    private static IssueEntity newIssue(final UUID projectId) {
        final IssueEntity entity = new IssueEntity();
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setTitle(SELF_SERVICE_ISSUE_TITLE);
        entity.setType(IssueType.SELF_SERVICE);
        entity.setStatus(IssueStatus.PENDING);
        entity.setPriority(IssuePriority.UNCLASSIFIED);
        entity.setVisibleToTenants(false);
        return entity;
    }

}
