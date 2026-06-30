package de.remsfal.core.api.ticketing;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueAttachmentEndpoint {

    String SERVICE = "attachments";

    @GET
    @Path("/{attachmentId}/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download an issue attachment")
    @APIResponse(responseCode = "200", description = "Attachment downloaded successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this attachment")
    @APIResponse(responseCode = "404", description = "Attachment not found")
    Response downloadAttachment(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the attachment", required = true)
        @PathParam("attachmentId") @NotNull UUID attachmentId,
        @Parameter(description = "Filename of the attachment", required = true)
        @PathParam("filename") @NotNull String filename);

    @DELETE
    @Path("/{attachmentId}")
    @Operation(summary = "Delete an issue attachment")
    @APIResponse(responseCode = "204", description = "Attachment deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to delete this attachment")
    @APIResponse(responseCode = "404", description = "Attachment not found")
    void deleteAttachment(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the attachment", required = true)
        @PathParam("attachmentId") @NotNull UUID attachmentId);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload one or more attachments to an existing issue.",
        description = "Uploads one or more image files to an already-existing issue."
            + " Each file must be provided as a separate 'attachment' part in the multipart request.")
    @APIResponse(responseCode = "200", description = "Attachments uploaded successfully")
    @APIResponse(responseCode = "400", description = "Invalid input or unsupported file type")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403",
        description = "User does not have permission to upload attachments to this issue")
    @APIResponse(responseCode = "404", description = "Issue not found")
    Response uploadAttachments(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Multipart form data containing one or more image files", required = true)
        MultipartFormDataInput input);

}
