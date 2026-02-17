package de.remsfal.ticketing.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
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
    public IssueListJson getIssues(Integer offset, Integer limit,
        UUID projectId, UUID assigneeId,
        UUID agreementId, UnitType rentalType,
        UUID rentalId, IssueStatus status) {
        logger.info("Yes i was called");
        List<UUID> projectFilter;
        if (projectId != null && principal.getProjectRoles().containsKey(projectId)) {
            projectFilter = List.of(projectId);
        } else {
            projectFilter = principal.getProjectRoles().keySet().stream().toList();
        }

        if (projectFilter.isEmpty()) {
            return getUnprivilegedIssues(offset, limit, agreementId, status);
        } else {
            return getProjectIssues(projectFilter, assigneeId, agreementId, rentalType, rentalId, status);
        }
    }

    private IssueListJson getProjectIssues(List<UUID> projectFilter, UUID assigneeId,
        UUID agreementId, UnitType rentalType, UUID rentalId,
        IssueStatus status) {
        final List<? extends IssueModel> issues =
            issueController.getIssues(projectFilter, assigneeId, agreementId, rentalType, rentalId, status);
        return IssueListJson.valueOf(issues, 0, issues.size());
    }

    private IssueListJson getUnprivilegedIssues(Integer offset, Integer limit, UUID agreementId, IssueStatus status) {
        List<IssueModel> collected = new ArrayList<>();

        // Tenants
        collectTenancyIssues(collected, agreementId);

        // Participants
        collectParticipantIssues(collected);

        Map<UUID, IssueModel> unique = new LinkedHashMap<>();
        for (IssueModel issue : collected) {
            if (issue == null || issue.getId() == null) {
                continue;
            }
            unique.put(issue.getId(), issue);
        }
        List<IssueModel> issues = new ArrayList<>(unique.values());

        if (status != null) {
            issues = issues.stream()
                    .filter(i -> Objects.equals(i.getStatus(), status))
                    .toList();
        }

        int totalCount = issues.size();
        int actualOffset = (offset != null) ? offset : 0;
        int actualLimit = (limit != null) ? limit : totalCount;

        int fromIndex = Math.min(actualOffset, totalCount);
        int toIndex = Math.min(actualOffset + actualLimit, totalCount);

        List<IssueModel> paginatedIssues = issues.subList(fromIndex, toIndex);

        return IssueListJson.valueOf(paginatedIssues, actualOffset, totalCount);
    }

    private void collectTenancyIssues(List<IssueModel> collected, UUID agreementId) {
        if (!principal.getTenancyProjects().isEmpty()) {
            if (agreementId != null && !principal.getTenancyProjects().containsKey(agreementId)) {
                throw new ForbiddenException("User does not have permission to view issues in this tenancy");
            }
            if (agreementId != null) {
                collected.addAll(issueController.getIssuesOfAgreement(agreementId));
            } else {
                collected.addAll(issueController.getIssuesOfAgreements(principal.getTenancyProjects().keySet()));
            }
        }
    }

    private void collectParticipantIssues(List<IssueModel> collected) {
        List<UUID> participantIssueIds = issueParticipantRepository.findIssueIdsByParticipant(principal.getId());
        for (UUID pid : participantIssueIds) {
            try {
                collected.add(issueController.getIssue(pid));
            } catch (NotFoundException ignored) {
                // Intentionally ignored:
                // The user may be listed as a participant of an issue that was deleted or closed.
                // In this case, the issue no longer exists and should simply be skipped
                // without failing the entire request.
            }
        }
    }

    @Override
    public Response createIssue(final IssueJson issue) {
        UserContext principalRole = getUserContext(issue.getProjectId());
        if (principalRole == null) {
            throw new ForbiddenException("User does not have permission to create issues in this project");
        }
        final IssueJson response;
        final IssueModel createdIssue;
        if (principalRole == UserContext.MANAGER) {
            createdIssue = issueController.createIssue(principal, issue);
            response = IssueJson.valueOf(createdIssue);
        } else if (principalRole == UserContext.TENANT) {
            createdIssue = issueController.createIssue(principal, issue, IssueStatus.PENDING);
            response = IssueJson.valueOfFiltered(createdIssue);
        } else {
            throw new ForbiddenException("User does not have permission to create issues in this project");
        }
        return getCreatedResponseBuilder(createdIssue.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(response)
            .build();
    }

    @Override
    public Response createIssueWithAttachments(final MultipartFormDataInput input) {
        // Extract issue json from multipart form data
        final IssueJson issue = extractIssueJson(input);
        // Check permissions
        checkTenancyIssueCreatePermissions(issue.getProjectId(), issue.getAgreementId());
        // Create issue
        IssueModel createdIssue = issueController.createIssue(principal, issue, IssueStatus.PENDING);
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
            .entity(IssueJson.valueOfFiltered(createdIssue).withAttachments(attachments))
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
            if (issueParts.get(0).getMediaType() == null || !issueParts.get(0).getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                throw new BadRequestException("Issue part must be of type application/json");
            }
            final IssueJson issue = issueParts.get(0).getBody(IssueJson.class, IssueJson.class);
            if (issue == null || !validator.validate(issue).isEmpty()) {
                throw new BadRequestException("Invalid issue data provided");
            }
            return issue;
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse issue data", e);
        }
    }

    @Override
    public IssueJson getIssue(final UUID issueId) {
        IssueModel issue = issueController.getIssue(issueId);

        // Create base IssueJson
        IssueJson issueJson;
        if (principal.getProjectRoles().containsKey(issue.getProjectId())) {
            issueJson = IssueJson.valueOf(issue);
        } else if (principal.getTenancyProjects().containsKey(issue.getAgreementId())) {
            issueJson = IssueJson.valueOfFiltered(issue);
        } else if (isParticipantInIssue(issueId)) {
            issueJson = IssueJson.valueOfFiltered(issue);
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
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }
        return IssueJson.valueOf(issueController.updateIssue(entity.getKey(), issue));
    }

    @Override
    public void deleteIssue(final UUID issueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (principal.getProjectRoles().containsKey(entity.getProjectId())) {
            issueController.deleteIssue(entity.getKey());
        } else if (principal.getTenancyProjects().containsKey(entity.getAgreementId())) {
            issueController.closeIssue(entity.getKey());
        } else {
            throw new ForbiddenException("User does not have permission to delete this issue");
        }
    }

    @Override
    public IssueJson setParent(final UUID issueId, final UUID parentIssueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }

        return IssueJson.valueOf(issueController.setParentRelation(entity, parentIssueId));
    }

    @Override
    public IssueJson createRelation(final UUID issueId, final String relationType, final UUID relatedIssueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }

        IssueModel updatedIssue = switch (relationType.toLowerCase()) {
            case "children" -> issueController.addChildRelation(entity, relatedIssueId);
            case "blocks" -> issueController.addBlocksRelation(entity, relatedIssueId);
            case "blocked-by" -> issueController.addBlockedByRelation(entity, relatedIssueId);
            case "related-to" -> issueController.addRelatedToRelation(entity, relatedIssueId);
            case "duplicate-of" -> issueController.addDuplicateOfRelation(entity, relatedIssueId);
            default -> throw new BadRequestException("Invalid relation type: " + relationType);
        };

        return IssueJson.valueOf(updatedIssue);
    }

    @Override
    public void deleteRelation(final UUID issueId, final String relationType, final UUID relatedIssueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }

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
        // Check read permissions (managers, tenants, participants)
        IssueModel issue = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(issue.getProjectId())
            && !principal.getTenancyProjects().containsKey(issue.getAgreementId())
            && !isParticipantInIssue(issueId)) {
            throw new ForbiddenException("User does not have permission to access this attachment");
        }

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
        // Check write permissions (managers only)
        IssueModel issue = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(issue.getProjectId())) {
            throw new ForbiddenException("User does not have permission to delete this attachment");
        }

        // Delete attachment (includes storage and database)
        issueController.deleteAttachment(issueId, attachmentId);
    }

    @Override
    public ChatSessionResource getChatSessionResource() {
        return resourceContext.initResource(chatSessionResource.get());
    }

    private boolean isParticipantInIssue(UUID issueId) {
        return issueParticipantRepository.exists(principal.getId(), issueId);
    }

}
