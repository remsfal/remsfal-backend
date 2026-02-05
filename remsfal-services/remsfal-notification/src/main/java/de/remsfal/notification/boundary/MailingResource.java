package de.remsfal.notification.boundary;

import io.smallrye.common.annotation.Blocking;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.ProjectEventJson;
import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.ImmutableProjectEventJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.notification.control.MailingController;

import java.util.Locale;
import java.util.UUID;

/**
 * REST endpoint for manual testing of email notifications.
 * Uses mock data with hardcoded values for development environments only.
 * Should not be exposed in production deployments.
 */
@Path("/notification/test")
public class MailingResource {

    private static final String ERROR_SEND_EMAIL = "Failed to send email";

    @Inject
    MailingController controller;

    @GET
    @Blocking
    public Response sendTestEmails(@QueryParam("to") @NotNull @Email final String to) {
        final UserModel recipient = new UserModel() {
            @Override
            public UUID getId() {
                return UUID.randomUUID();
            }
            @Override
            public String getEmail() {
                return to;
            }
            @Override
            public String getName() {
                return "Max Mustermann";
            }
            @Override
            public Boolean isActive() {
                return true;
            }
        };

        controller.sendWelcomeEmail(recipient, "https://remsfal.de", Locale.ENGLISH);
        controller.sendWelcomeEmail(recipient, "https://remsfal.de", Locale.GERMAN);
        controller.sendNewMembershipEmail(recipient, "https://remsfal.de", Locale.ENGLISH);
        controller.sendNewMembershipEmail(recipient, "https://remsfal.de", Locale.GERMAN);
        return Response.accepted().build();
    }

    @GET
    @Path("/issue-assigned")
    @Blocking
    public Response testIssueAssigned(@QueryParam("to") @NotNull @Email final String to) {
        try {
            IssueEventJson event = createMockIssueEvent(IssueEventJson.IssueEventType.ISSUE_ASSIGNED);
            UserJson recipient = createMockRecipient(to);
            controller.sendIssueAssignedEmail(event, recipient);
            return Response.accepted().entity("Issue assigned email sent successfully").build();
        } catch (Exception e) {
            return Response.serverError().entity(ERROR_SEND_EMAIL).build();
        }
    }

    @GET
    @Path("/issue-created")
    @Blocking
    public Response testIssueCreated(@QueryParam("to") @NotNull @Email final String to) {
        try {
            IssueEventJson event = createMockIssueEvent(IssueEventJson.IssueEventType.ISSUE_CREATED);
            UserJson recipient = createMockRecipient(to);
            controller.sendIssueCreatedEmail(event, recipient);
            return Response.accepted().entity("Issue created email sent successfully").build();
        } catch (Exception e) {
            return Response.serverError().entity(ERROR_SEND_EMAIL).build();
        }
    }

    @GET
    @Path("/issue-updated")
    @Blocking
    public Response testIssueUpdated(@QueryParam("to") @NotNull @Email final String to) {
        try {
            IssueEventJson event = createMockIssueEvent(IssueEventJson.IssueEventType.ISSUE_UPDATED);
            UserJson recipient = createMockRecipient(to);
            controller.sendIssueUpdatedEmail(event, recipient);
            return Response.accepted().entity("Issue updated email sent successfully").build();
        } catch (Exception e) {
            return Response.serverError().entity(ERROR_SEND_EMAIL).build();
        }
    }

    private UserJson createMockRecipient(final String email) {
        return ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email(email)
            .name(null)
            .firstName(null)
            .lastName(null)
            .build();
    }

    private IssueEventJson createMockIssueEvent(IssueEventJson.IssueEventType eventType) {
        ProjectEventJson project = ImmutableProjectEventJson.builder()
            .id(UUID.fromString("bcf74f9f-5bf1-4fd7-9ba4-2a6cba11e67f"))
            .title("Test Project")
            .build();

        UserJson actor = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("actor@example.com")
            .name("Test Actor")
            .build();

        UserJson assignee = ImmutableUserJson.builder()
            .id(UUID.randomUUID())
            .email("assignee@example.com")
            .name("Test Owner")
            .build();

        return ImmutableIssueEventJson.builder()
            .issueEventType(eventType)
            .issueId(UUID.fromString("5505b407-d8a7-41a7-848c-77eff4c9fc81"))
            .project(project)
            .projectId(project.getId())
            .title("Test Issue Title")
            .link("https://remsfal.de/projects/bf9f-5bf1-4fd7-9ba4-2a6cb/issueedit/5507-d8a7-41a7-848c-77e81")
            .issueType(IssueType.DEFECT)
            .status(IssueStatus.OPEN)
            .reporterId(UUID.randomUUID())
            .agreementId(UUID.randomUUID())
            .assigneeId(assignee.getId())
            .description("Das ist eine Test Issue")
            .user(actor)
            .assignee(assignee)
            .build();
    }
}