package de.remsfal.ticketing.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import de.remsfal.core.api.ticketing.IssueAttachmentEndpoint;
import de.remsfal.core.api.ticketing.IssueEndpoint;
import de.remsfal.core.api.ticketing.IssueQuotationEndpoint;
import de.remsfal.core.api.ticketing.IssueQuotationRequestEndpoint;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.control.AttachmentController;
import de.remsfal.ticketing.entity.filter.IssueFilter;
import io.quarkus.security.Authenticated;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueResource extends AbstractTicketingResource implements IssueEndpoint {

    @Inject
    AttachmentController attachmentController;

    @Inject
    Instance<IssueAttachmentResource> attachmentResource;

    @Inject
    Instance<IssueQuotationRequestResource> quotationRequestResource;

    @Inject
    Instance<IssueQuotationResource> quotationResource;

    @Inject
    Instance<ChatSessionResource> chatSessionResource;

    @Inject
    Instance<IssueTimelineResource> timelineResource;

    @Override
    public IssueListJson getIssues(final UUID projectId, final UUID assigneeId, final UUID agreementId,
        final UnitType rentalUnitType, final UUID rentalUnitId, final List<IssueType> type,
        final List<IssueStatus> status, final UUID cursor, final Integer limit) {
        checkProjectReadPermissions(projectId);
        final IssueFilter filter = new IssueFilter(projectId, assigneeId, agreementId,
            rentalUnitType, rentalUnitId, type, status);
        final List<? extends IssueModel> issues = issueController.getProjectIssues(filter, cursor, limit);
        return IssueListJson.valueOfProjectIssues(issues, nextCursorOf(issues, limit));
    }

    @Override
    public Response createIssue(final IssueJson issue) {
        checkProjectIssueCreatePermissions(issue.getProjectId());
        final IssueModel createdIssue = issueController.createProjectIssue(principal, issue);
        return getCreatedResponseBuilder(createdIssue.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(IssueJson.valueOf(createdIssue))
            .build();
    }

    @Override
    public IssueJson getIssue(final UUID issueId) {
        final IssueModel issue = checkProjectIssueAccessPermissions(issueId);

        // Lazy-load attachments and add to response
        List<? extends IssueAttachmentModel> attachments = attachmentController.getAttachments(issueId);
        List<IssueAttachmentJson> attachmentJsons = attachments.stream()
            .map(IssueAttachmentJson::valueOf)
            .collect(Collectors.toList());

        return IssueJson.valueOf(issue).withAttachments(attachmentJsons);
    }

    @Override
    public IssueJson updateIssue(final UUID issueId, final IssueJson issue) {
        checkProjectIssueAccessPermissions(issueId);
        final IssueModel updatedIssue = issueController.updateIssue(issueId, issue);
        return IssueJson.valueOf(updatedIssue);
    }

    @Override
    public void deleteIssue(final UUID issueId) {
        checkProjectIssueAccessPermissions(issueId);
        issueController.deleteIssue(issueId);
    }

    @Override
    public IssueJson setParent(final UUID issueId, final UUID parentIssueId) {
        final IssueModel issue = checkProjectIssueAccessPermissions(issueId);
        return IssueJson.valueOf(issueController.setParentRelation(principal, issue, parentIssueId));
    }

    @Override
    public IssueJson createRelation(final UUID issueId, final String relationType, final UUID relatedIssueId) {
        final IssueModel issue = checkProjectIssueAccessPermissions(issueId);

        IssueModel updatedIssue = switch (relationType.toLowerCase()) {
            case "children" -> issueController.addChildRelation(principal, issue, relatedIssueId);
            case "blocks" -> issueController.addBlocksRelation(principal, issue, relatedIssueId);
            case "blocked-by" -> issueController.addBlockedByRelation(principal, issue, relatedIssueId);
            case "related-to" -> issueController.addRelatedToRelation(principal, issue, relatedIssueId);
            case "duplicate-of" -> issueController.addDuplicateOfRelation(principal, issue, relatedIssueId);
            default -> throw new BadRequestException("Invalid relation type: " + relationType);
        };

        return IssueJson.valueOf(updatedIssue);
    }

    @Override
    public void deleteRelation(final UUID issueId, final String relationType, final UUID relatedIssueId) {
        final IssueModel issue = checkProjectIssueAccessPermissions(issueId);

        switch (relationType.toLowerCase()) {
            case "parent" -> issueController.deleteParentRelation(principal, issue, relatedIssueId);
            case "children" -> issueController.deleteChildRelation(principal, issue, relatedIssueId);
            case "blocks" -> issueController.deleteBlocksRelation(principal, issue, relatedIssueId);
            case "blocked-by" -> issueController.deleteBlockedByRelation(principal, issue, relatedIssueId);
            case "related-to" -> issueController.deleteRelatedToRelation(principal, issue, relatedIssueId);
            case "duplicate-of" -> issueController.deleteDuplicateOfRelation(principal, issue, relatedIssueId);
            default -> throw new BadRequestException("Invalid relation type: " + relationType);
        }
    }

    @Override
    public IssueAttachmentEndpoint getAttachmentResource() {
        return resourceContext.initResource(attachmentResource.get());
    }

    @Override
    public IssueQuotationRequestEndpoint getQuotationRequestResource() {
        return resourceContext.initResource(quotationRequestResource.get());
    }

    @Override
    public IssueQuotationEndpoint getQuotationResource() {
        return resourceContext.initResource(quotationResource.get());
    }

    @Override
    public ChatSessionResource getChatSessionResource() {
        return resourceContext.initResource(chatSessionResource.get());
    }

    @Override
    public IssueTimelineResource getTimelineResource() {
        return resourceContext.initResource(timelineResource.get());
    }

}
