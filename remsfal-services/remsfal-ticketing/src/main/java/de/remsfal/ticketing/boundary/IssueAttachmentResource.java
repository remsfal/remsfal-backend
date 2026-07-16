package de.remsfal.ticketing.boundary;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.common.boundary.MultipartAttachmentProcessor;
import de.remsfal.core.api.ticketing.IssueAttachmentEndpoint;
import de.remsfal.core.json.ticketing.IssueAttachmentJson;
import de.remsfal.ticketing.control.AttachmentController;
import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class IssueAttachmentResource extends AbstractTicketingResource implements IssueAttachmentEndpoint {

    @Inject
    AttachmentController attachmentController;

    @Override
    public Response downloadAttachment(final UUID issueId, final UUID attachmentId, final String filename) {
        checkManagerIssueReadPermissions(issueId);

        IssueAttachmentEntity attachment = attachmentController.getAttachment(issueId, attachmentId);
        InputStream fileStream = attachmentController.downloadAttachment(attachment.getObjectName());

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
    public void deleteAttachment(final UUID issueId, final UUID attachmentId) {
        checkIssueWritePermissions(issueId);
        attachmentController.deleteAttachment(issueId, attachmentId);
    }

    @Override
    public Response uploadAttachments(final UUID issueId, final MultipartFormDataInput input) {
        checkIssueWritePermissions(issueId);

        Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        List<InputPart> fileParts = formDataMap.get("attachment");
        List<IssueAttachmentJson> attachments = MultipartAttachmentProcessor.processAttachmentParts(
            fileParts,
            fileData -> IssueAttachmentJson.valueOf(attachmentController.addAttachment(principal, issueId, fileData)));

        return Response.ok()
            .type(MediaType.APPLICATION_JSON)
            .entity(attachments)
            .build();
    }

}
