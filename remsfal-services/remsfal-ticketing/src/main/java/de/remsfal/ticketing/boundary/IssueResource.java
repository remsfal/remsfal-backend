package de.remsfal.ticketing.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.common.model.FileUploadData;
import de.remsfal.core.api.ticketing.IssueEndpoint;
import de.remsfal.core.json.UserJson.UserContext;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.validation.TenancyValidation;
import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import io.quarkus.security.Authenticated;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueResource extends AbstractTicketingResource implements IssueEndpoint {

    @Inject
    Logger logger;

    @Inject
    Validator validator;

    @Inject
    Instance<ChatSessionResource> chatSessionResource;

    @Override
    public IssueListJson getIssues(final Integer offset, final Integer limit, final boolean preferTenancyIssues,
        final UUID projectId, final UUID assigneeId, final UUID agreementId,
        final UnitType rentalUnitType, final UUID rentalUnitId, final IssueStatus status) {
        if (projectId != null && preferTenancyIssues
            && principal.getTenancyProjects().containsValue(projectId)) {
            return getTenancyIssues(offset, limit, List.of(projectId), agreementId, status);
        } else if (projectId != null && principal.getProjectRoles().containsKey(projectId)) {
            return getProjectIssues(offset, limit, List.of(projectId), assigneeId, agreementId,
                rentalUnitType, rentalUnitId, status);
        } else if (preferTenancyIssues) {
            List<UUID> projectFilter = principal.getTenancyProjects().values().stream().toList();
            return getTenancyIssues(offset, limit, projectFilter, agreementId, status);
        } else {
            List<UUID> projectFilter = principal.getProjectRoles().keySet().stream().toList();
            return getProjectIssues(offset, limit, projectFilter, assigneeId, agreementId,
                rentalUnitType, rentalUnitId, status);
        }
    }

    private IssueListJson getTenancyIssues(final Integer offset, final Integer limit,
        final List<UUID> projectIds, final UUID agreementId, final IssueStatus status) {
        final List<UUID> agreementFilter;
        if (agreementId != null && principal.getTenancyProjects().containsKey(agreementId)) {
            agreementFilter = List.of(agreementId);
        } else {
            agreementFilter = principal.getTenancyProjects().keySet().stream().toList();
        }
        final List<? extends IssueModel> issues = issueController.getIssues(projectIds, null, agreementFilter,
            null, null, status, offset, limit);
        return IssueListJson.valueOfTenancyIssues(issues, offset);
    }

    private IssueListJson getProjectIssues(final Integer offset, final Integer limit,
        final List<UUID> projectIds, final UUID assigneeId, final UUID agreementId,
        final UnitType rentalUnitType, final UUID rentalUnitId, final IssueStatus status) {
        final List<? extends IssueModel> issues;
        if(agreementId != null) {
            issues = issueController.getIssues(projectIds, assigneeId, List.of(agreementId),
                rentalUnitType, rentalUnitId, status, offset, limit);
        } else {
            issues = issueController.getIssues(projectIds, assigneeId, null,
                rentalUnitType, rentalUnitId, status, offset, limit);
        }
        return IssueListJson.valueOfProjectIssues(issues, offset);
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
    public Response createTenancyIssueWithAttachments(final MultipartFormDataInput input) {
        // Extract issue json from multipart form data
        final IssueJson issue = extractIssueJson(input);
        // Check permissions
        final UUID projectId = checkTenancyIssueCreatePermissions(issue.getAgreementId());
        if (projectId == null) {
            throw new ForbiddenException("User does not have permission to create issues in this tenancy");
        }
        // Create issue
        IssueModel createdIssue = issueController.createIssue(principal, issue, projectId, IssueStatus.PENDING);
        // Process attachments
        Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        List<InputPart> fileParts = formDataMap.get("attachment");
        List<IssueAttachmentJson> attachments = new ArrayList<>();
        if (fileParts != null && !fileParts.isEmpty()) {
            for (InputPart inputPart : fileParts) {
                try {
                    InputStream inputStream = inputPart.getBody(InputStream.class, null);
                    FileUploadData fileData = new FileUploadData(
                        inputStream,
                        inputPart.getFileName(),
                        inputPart.getMediaType());
                    IssueAttachmentModel attachmentModel = issueController
                        .addAttachment(principal, createdIssue.getId(), fileData);
                    attachments.add(IssueAttachmentJson.valueOf(attachmentModel));
                } catch (IOException e) {
                    throw new BadRequestException("Failed to read file data", e);
                }
            }
        }

        return getCreatedResponseBuilder(createdIssue.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(IssueJson.valueOfTenancyIssue(createdIssue).withAttachments(attachments))
            .build();
    }

    private IssueJson extractIssueJson(final MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
            List<InputPart> issueParts = formDataMap.get("issue");
            if (issueParts == null || issueParts.isEmpty()) {
                throw new BadRequestException("Missing 'issue' part in multipart request");
            }
            if (issueParts.size() > 1) {
                throw new BadRequestException("Multiple 'issue' parts found in multipart request");
            }
            if (issueParts.get(0) == null) {
                throw new BadRequestException("Issue part is null");
            }
            if (issueParts.get(0).getMediaType() == null ||
                !issueParts.get(0).getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                throw new BadRequestException("Issue part must be of type application/json");
            }
            final IssueJson issue = issueParts.get(0).getBody(IssueJson.class, IssueJson.class);
            if (issue == null) {
                throw new BadRequestException("Unable to parse issue data from request");
            }
            Set<ConstraintViolation<IssueJson>> violations = validator.validate(issue, TenancyValidation.class);
            if (!violations.isEmpty()) {
                String errorMessages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
                throw new BadRequestException("Invalid issue data provided: " + errorMessages);
            }
            return issue;
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse issue data", e);
        }
    }

    @Override
    public IssueJson getIssue(final UUID issueId) {
        final UserContext context = checkIssueReadPermissions(issueId);
        IssueModel issue = issueController.getIssue(issueId);

        // Create base IssueJson
        IssueJson issueJson;
        if (context == UserContext.MANAGER) {
            issueJson = IssueJson.valueOfProjectIssue(issue);
        } else if (context == UserContext.TENANT) {
            issueJson = IssueJson.valueOfTenancyIssue(issue);
        } else {
            throw new ForbiddenException("User does not have permission to view this issue");
        }

        // Lazy-load attachments and add to response
        List<? extends IssueAttachmentModel> attachments = issueController.getAttachments(issueId);
        List<IssueAttachmentJson> attachmentJsons = attachments.stream()
            .map(IssueAttachmentJson::valueOf)
            .collect(Collectors.toList());

        return issueJson.withAttachments(attachmentJsons);
    }

    @Override
    public IssueJson updateIssue(final UUID issueId, final IssueJson issue) {
        checkIssueWritePermissions(issueId);
        IssueModel updatedIssue = issueController.updateIssue(issueId, issue);
        return IssueJson.valueOfProjectIssue(updatedIssue);
    }

    @Override
    public void deleteIssue(final UUID issueId) {
        final UserContext context = checkIssueReadPermissions(issueId);
        IssueEntity entity = issueController.getIssue(issueId);
        if (context == UserContext.TENANT) {
            issueController.closeIssue(entity.getKey());
        } else if (context == UserContext.MANAGER && checkIssueWritePermissions(issueId) != null) {
            issueController.deleteIssue(entity.getKey());
        } else {
            throw new ForbiddenException("User does not have permission to delete this issue");
        }
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
    public Response downloadAttachment(UUID issueId, UUID attachmentId, String filename) {
        checkIssueReadPermissions(issueId);

        // Retrieve attachment metadata
        IssueAttachmentEntity attachment = issueController.getAttachment(issueId, attachmentId);
        // Download file from storage
        InputStream fileStream = issueController.downloadAttachment(attachment.getObjectName());

        // Stream response with Content-Disposition header
        return Response.ok((StreamingOutput) output -> {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        })
        .type(MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", "attachment; filename=\"" + attachment.getFileName() + "\"")
        .build();
    }

    @Override
    public void deleteAttachment(UUID issueId, UUID attachmentId) {
        checkIssueWritePermissions(issueId);

        // Delete attachment (includes storage and database)
        issueController.deleteAttachment(issueId, attachmentId);
    }

    @Override
    public Response uploadAttachments(final UUID issueId, final MultipartFormDataInput input) {
        checkIssueWritePermissions(issueId);

        // Process attachment parts
        Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        List<InputPart> fileParts = formDataMap.get("attachment");
        List<IssueAttachmentJson> attachments = new ArrayList<>();
        if (fileParts != null && !fileParts.isEmpty()) {
            for (InputPart inputPart : fileParts) {
                try {
                    InputStream inputStream = inputPart.getBody(InputStream.class, null);
                    FileUploadData fileData = new FileUploadData(
                        inputStream,
                        inputPart.getFileName(),
                        inputPart.getMediaType());
                    IssueAttachmentModel attachmentModel =
                        issueController.addAttachment(principal, issueId, fileData);
                    attachments.add(IssueAttachmentJson.valueOf(attachmentModel));
                } catch (IOException e) {
                    throw new BadRequestException("Failed to read file data", e);
                }
            }
        }

        return Response.ok()
            .type(MediaType.APPLICATION_JSON)
            .entity(attachments)
            .build();
    }

    @Override
    public ChatSessionResource getChatSessionResource() {
        return resourceContext.initResource(chatSessionResource.get());
    }

}
