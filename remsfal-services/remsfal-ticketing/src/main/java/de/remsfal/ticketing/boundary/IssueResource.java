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
import de.remsfal.core.api.ticketing.TimelineEndpoint;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.ticketing.control.AttachmentController;
import de.remsfal.ticketing.entity.dto.IssueEntity;
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
        final UnitType rentalUnitType, final UUID rentalUnitId, final IssueStatus status,
        final String cursor, final Integer limit) {
        checkProjectIssueReadPermissions(projectId);
        final List<? extends IssueModel> issues = issueController.getProjectIssues(projectId, assigneeId,
            agreementId, rentalUnitType, rentalUnitId, status, parseCursor(cursor), limit);
        return IssueListJson.valueOfProjectIssues(issues, nextCursorOf(issues, limit));
    }

    @Override
    public Response createProjectIssue(final IssueJson issue) {
        checkProjectIssueCreatePermissions(issue.getProjectId());
        final IssueModel createdIssue = issueController.createIssue(principal, issue);
        return getCreatedResponseBuilder(createdIssue.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(IssueJson.valueOfProjectIssue(createdIssue))
            .build();
    }

    @Override
    public IssueJson getIssue(final UUID issueId) {
        checkManagerIssueReadPermissions(issueId);
        IssueModel issue = issueController.getIssue(issueId);

        // Lazy-load attachments and add to response
        List<? extends IssueAttachmentModel> attachments = attachmentController.getAttachments(issueId);
        List<IssueAttachmentJson> attachmentJsons = attachments.stream()
            .map(IssueAttachmentJson::valueOf)
            .collect(Collectors.toList());

        return IssueJson.valueOfProjectIssue(issue).withAttachments(attachmentJsons);
    }

    @Override
    public IssueJson updateIssue(final UUID issueId, final IssueJson issue) {
        checkIssueWritePermissions(issueId);
        IssueModel updatedIssue = issueController.updateIssue(issueId, issue);
        return IssueJson.valueOfProjectIssue(updatedIssue);
    }

    @Override
    public void deleteIssue(final UUID issueId) {
        checkIssueWritePermissions(issueId);
        issueController.deleteIssue(issueId);
    }

    @Override
    public IssueJson setParent(final UUID issueId, final UUID parentIssueId) {
        checkIssueWritePermissions(issueId);
        IssueEntity entity = issueController.getIssue(issueId);
        return IssueJson.valueOfProjectIssue(issueController.setParentRelation(entity, parentIssueId));
    }

    @Override
    public IssueJson createRelation(final UUID issueId, final String relationType, final UUID relatedIssueId) {
        checkIssueWritePermissions(issueId);
        IssueEntity entity = issueController.getIssue(issueId);

        IssueModel updatedIssue = switch (relationType.toLowerCase()) {
            case "children" -> issueController.addChildRelation(entity, relatedIssueId);
            case "blocks" -> issueController.addBlocksRelation(entity, relatedIssueId);
            case "blocked-by" -> issueController.addBlockedByRelation(entity, relatedIssueId);
            case "related-to" -> issueController.addRelatedToRelation(entity, relatedIssueId);
            case "duplicate-of" -> issueController.addDuplicateOfRelation(entity, relatedIssueId);
            default -> throw new BadRequestException("Invalid relation type: " + relationType);
        };

        return IssueJson.valueOfProjectIssue(updatedIssue);
    }

    @Override
    public void deleteRelation(final UUID issueId, final String relationType, final UUID relatedIssueId) {
        checkIssueWritePermissions(issueId);
        IssueEntity entity = issueController.getIssue(issueId);

        switch (relationType.toLowerCase()) {
            case "parent" -> issueController.deleteParentRelation(entity, relatedIssueId);
            case "children" -> issueController.deleteChildRelation(entity, relatedIssueId);
            case "blocks" -> issueController.deleteBlocksRelation(entity, relatedIssueId);
            case "blocked-by" -> issueController.deleteBlockedByRelation(entity, relatedIssueId);
            case "related-to" -> issueController.deleteRelatedToRelation(entity, relatedIssueId);
            case "duplicate-of" -> issueController.deleteDuplicateOfRelation(entity, relatedIssueId);
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
    public TimelineEndpoint getTimelineResource() {
        return resourceContext.initResource(timelineResource.get());
    }

}
