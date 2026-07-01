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
 * Reusable attachment sub-resource, mountable under a quotation request, quotation, or order placement.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface OrderAttachmentEndpoint {

    String SERVICE = "attachments";

    @GET
    @Path("/{attachmentId}/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Download an order attachment")
    @APIResponse(responseCode = "200", description = "Attachment downloaded successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this attachment")
    @APIResponse(responseCode = "404", description = "Attachment not found")
    Response downloadAttachment(
        @Parameter(description = "ID of the quotation request, quotation, or order placement", required = true)
        @PathParam("processId") UUID processId,
        @Parameter(description = "ID of the attachment", required = true)
        @PathParam("attachmentId") @NotNull UUID attachmentId,
        @Parameter(description = "Filename of the attachment", required = true)
        @PathParam("filename") @NotNull String filename);

    @DELETE
    @Path("/{attachmentId}")
    @Operation(summary = "Delete an order attachment")
    @APIResponse(responseCode = "204", description = "Attachment deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to delete this attachment")
    @APIResponse(responseCode = "404", description = "Attachment not found")
    void deleteAttachment(
        @Parameter(description = "ID of the quotation request, quotation, or order placement", required = true)
        @PathParam("processId") UUID processId,
        @Parameter(description = "ID of the attachment", required = true)
        @PathParam("attachmentId") @NotNull UUID attachmentId);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload one or more attachments.",
        description = "Uploads one or more files to an already-existing quotation request, quotation,"
            + " or order placement. Each file must be provided as a separate 'attachment' part"
            + " in the multipart request.")
    @APIResponse(responseCode = "200", description = "Attachments uploaded successfully")
    @APIResponse(responseCode = "400", description = "Invalid input or unsupported file type")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403",
        description = "User does not have permission to upload attachments here")
    @APIResponse(responseCode = "404", description = "The quotation request, quotation, or order placement not found")
    Response uploadAttachments(
        @Parameter(description = "ID of the quotation request, quotation, or order placement", required = true)
        @PathParam("processId") UUID processId,
        @Parameter(description = "Multipart form data containing one or more files", required = true)
        MultipartFormDataInput input);

}
