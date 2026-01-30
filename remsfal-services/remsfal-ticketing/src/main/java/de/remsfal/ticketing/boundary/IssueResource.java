package de.remsfal.ticketing.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.ArrayList;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.core.api.ticketing.IssueEndpoint;
import de.remsfal.core.json.UserJson.UserContext;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.ImmutableIssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.project.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.control.FileStorageController;
import de.remsfal.ticketing.control.IssueEventProducer;
import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.storage.FileStorage;
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
    Instance<ChatSessionResource> chatSessionResource;

    @Inject
    IssueEventProducer issueEventProducer;

    @Inject
    FileStorage fileStorage;

    @Override
    public IssueListJson getIssues(Integer offset, Integer limit,
        UUID projectId, UUID assigneeId,
        UUID tenancyId, UnitType rentalType,
        UUID rentalId, IssueStatus status) {
        logger.info("Yes i was called");
        List<UUID> projectFilter;
        if (projectId != null && principal.getProjectRoles().containsKey(projectId)) {
            projectFilter = List.of(projectId);
        } else {
            projectFilter = principal.getProjectRoles().keySet().stream().toList();
        }

        if (projectFilter.isEmpty()) {
            return getUnprivilegedIssues(offset, limit, tenancyId, status);
        } else {
            return getProjectIssues(projectFilter, assigneeId, tenancyId, rentalType, rentalId, status);
        }
    }

    private IssueListJson getProjectIssues(List<UUID> projectFilter, UUID assigneeId,
        UUID tenancyId, UnitType rentalType, UUID rentalId,
        IssueStatus status) {
        final List<? extends IssueModel> issues =
            issueController.getIssues(projectFilter, assigneeId, tenancyId, rentalType, rentalId, status);
        return IssueListJson.valueOf(issues, 0, issues.size());
    }

    private IssueListJson getUnprivilegedIssues(Integer offset, Integer limit, UUID tenancyId, IssueStatus status) {
        List<IssueModel> collected = new ArrayList<>();

        // Tenants
        collectTenancyIssues(collected, tenancyId);

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

    private void collectTenancyIssues(List<IssueModel> collected, UUID tenancyId) {
        if (!principal.getTenancyProjects().isEmpty()) {
            if (tenancyId != null && !principal.getTenancyProjects().containsKey(tenancyId)) {
                throw new ForbiddenException("User does not have permission to view issues in this tenancy");
            }
            if (tenancyId != null) {
                collected.addAll(issueController.getIssuesOfTenancy(tenancyId));
            } else {
                collected.addAll(issueController.getIssuesOfTenancies(principal.getTenancyProjects().keySet()));
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
        final URI location = uri.getAbsolutePathBuilder()
            .path(Objects.requireNonNull(issue.getProjectId())
            .toString())
            .build();
        issueEventProducer.sendIssueCreated(createdIssue, principal);
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(response)
            .build();
    }

    @Override
    public Response createIssueWithAttachments(final MultipartFormDataInput input) {
        try {
            Map<String, List<InputPart>> formDataMap = input.getFormDataMap();

            // Extract issue metadata from form fields
            String projectIdStr = extractFormField(formDataMap, "projectId");
            String title = extractFormField(formDataMap, "title");
            String typeStr = extractFormField(formDataMap, "type");
            String description = extractFormField(formDataMap, "description");

            if (projectIdStr == null || title == null || typeStr == null) {
                throw new BadRequestException("Required fields missing: projectId, title, and type are required");
            }

            UUID projectId = UUID.fromString(projectIdStr);
            IssueType issueType = IssueType.valueOf(typeStr);

            // Check permissions
            UserContext principalRole = getUserContext(projectId);
            if (principalRole == null) {
                throw new ForbiddenException("User does not have permission to create issues in this project");
            }

            // Create issue JSON
            IssueJson issueJson = ImmutableIssueJson.builder()
                .projectId(projectId)
                .title(title)
                .type(issueType)
                .description(description)
                .build();

            // Create the issue
            final IssueModel createdIssue;
            final IssueJson response;
            if (principalRole == UserContext.MANAGER) {
                createdIssue = issueController.createIssue(principal, issueJson);
                response = IssueJson.valueOf(createdIssue);
            } else if (principalRole == UserContext.TENANT) {
                createdIssue = issueController.createIssue(principal, issueJson, IssueStatus.PENDING);
                response = IssueJson.valueOfFiltered(createdIssue);
            } else {
                throw new ForbiddenException("User does not have permission to create issues in this project");
            }

            // Process attachments if any
            List<InputPart> fileParts = formDataMap.get("attachments");
            List<IssueAttachmentEntity> attachments = new ArrayList<>();
            if (fileParts != null && !fileParts.isEmpty()) {
                for (InputPart filePart : fileParts) {
                    String fileName = extractFileName(filePart.getHeaders());
                    MediaType mediaType = filePart.getMediaType();
                    String contentType = mediaType.toString();

                    // Validate content type - only allow images
                    if (!isImageContentType(contentType)) {
                        logger.warnv("Skipping non-image file: {0} with content type: {1}", fileName, contentType);
                        continue;
                    }

                    try (InputStream fileStream = filePart.getBody(InputStream.class, null)) {
                        if (fileStream == null || fileStream.available() == 0) {
                            logger.warnv("Skipping empty file: {0}", fileName);
                            continue;
                        }

                        long fileSize = fileStream.available();

                        // Upload file directly to storage
                        String objectName = fileStorage.uploadFile(fileStream, fileName, mediaType);

                        // Store attachment metadata
                        IssueAttachmentEntity attachment = issueController.addAttachment(
                            createdIssue.getId(),
                            fileName,
                            contentType,
                            FileStorage.DEFAULT_BUCKET_NAME,
                            objectName,
                            fileSize,
                            principal.getId()
                        );
                        attachments.add(attachment);
                    } catch (Exception e) {
                        logger.errorv(e, "Failed to upload attachment: {0}", fileName);
                        // Continue processing other files
                    }
                }
            }

            final URI location = uri.getAbsolutePathBuilder()
                .path(Objects.requireNonNull(createdIssue.getProjectId()).toString())
                .build();
            issueEventProducer.sendIssueCreated(createdIssue, principal);

            return Response.created(location)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
        } catch (BadRequestException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating issue with attachments", e);
            throw new BadRequestException("Failed to create issue with attachments: " + e.getMessage());
        }
    }

    @Override
    public IssueJson getIssue(final UUID issueId) {
        IssueModel issue = issueController.getIssue(issueId);
        if (principal.getProjectRoles().containsKey(issue.getProjectId())) {
            return IssueJson.valueOf(issue);
        } else if (principal.getTenancyProjects().containsKey(issue.getTenancyId())) {
            return IssueJson.valueOfFiltered(issue);
        } else if (isParticipantInIssue(issueId)) {
            return IssueJson.valueOfFiltered(issue);
        }
        throw new ForbiddenException("User does not have permission to view this issue");
    }

    @Override
    public IssueJson updateIssue(final UUID issueId, final IssueJson issue) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }
        UUID previousAssignee = entity.getAssigneeId();
        IssueModel updatedIssue = issueController.updateIssue(entity.getKey(), issue);
        IssueJson response = IssueJson.valueOf(updatedIssue);
        UUID newAssignee = updatedIssue.getAssigneeId();
        if (newAssignee != null && !Objects.equals(previousAssignee, newAssignee)) {
            issueEventProducer.sendIssueAssigned(updatedIssue, principal, newAssignee);
        } else {
            issueEventProducer.sendIssueUpdated(updatedIssue, principal);
        }
        return response;
    }

    @Override
    public void deleteIssue(final UUID issueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (principal.getProjectRoles().containsKey(entity.getProjectId())) {
            issueController.deleteIssue(entity.getKey());
        } else if (principal.getTenancyProjects().containsKey(entity.getTenancyId())) {
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

        IssueModel updatedIssue = issueController.setParentIssue(entity, parentIssueId);
        IssueJson response = IssueJson.valueOf(updatedIssue);
        issueEventProducer.sendIssueUpdated(updatedIssue, principal);
        return response;
    }

    @Override
    public IssueJson createRelation(final UUID issueId, final String relationType, final UUID relatedIssueId) {
        IssueEntity entity = issueController.getIssue(issueId);
        if (!principal.getProjectRoles().containsKey(entity.getProjectId())) {
            throw new ForbiddenException("User does not have permission to update this issue");
        }

        IssueModel updatedIssue = switch (relationType.toLowerCase()) {
            case "children" -> issueController.addChildIssue(entity, relatedIssueId);
            case "blocks" -> issueController.addBlocksRelation(entity, relatedIssueId);
            case "blocked-by" -> issueController.addBlockedByRelation(entity, relatedIssueId);
            case "related-to" -> issueController.addRelatedToRelation(entity, relatedIssueId);
            case "duplicate-of" -> issueController.addDuplicateOfRelation(entity, relatedIssueId);
            default -> throw new BadRequestException("Invalid relation type: " + relationType);
        };

        IssueJson response = IssueJson.valueOf(updatedIssue);
        issueEventProducer.sendIssueUpdated(updatedIssue, principal);
        return response;
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
    public ChatSessionResource getChatSessionResource() {
        return resourceContext.initResource(chatSessionResource.get());
    }

    private boolean isParticipantInIssue(UUID issueId) {
        return issueParticipantRepository.exists(principal.getId(), issueId);
    }

    private String extractFormField(Map<String, List<InputPart>> formDataMap, String fieldName) {
        List<InputPart> parts = formDataMap.get(fieldName);
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        try {
            return parts.get(0).getBodyAsString();
        } catch (Exception e) {
            logger.warnv(e, "Failed to extract form field: {0}", fieldName);
            return null;
        }
    }

    private String extractFileName(Map<String, List<String>> headers) {
        List<String> contentDisposition = headers.get("Content-Disposition");
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            for (String part : contentDisposition.get(0).split(";")) {
                if (part.trim().startsWith("filename")) {
                    return part.split("=")[1].trim().replaceAll("\"", "");
                }
            }
        }
        return "unknown";
    }

    private boolean isImageContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String normalizedContentType = contentType.split(";")[0].trim().toLowerCase();
        return normalizedContentType.equals("image/jpeg") ||
               normalizedContentType.equals("image/jpg") ||
               normalizedContentType.equals("image/png") ||
               normalizedContentType.equals("image/gif");
    }

    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return "unknown";
        }
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == fileUrl.length() - 1) {
            return fileUrl;
        }
        return fileUrl.substring(lastSlashIndex + 1);
    }

}
