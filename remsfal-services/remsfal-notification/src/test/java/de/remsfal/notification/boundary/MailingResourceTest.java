package de.remsfal.notification.boundary;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.mail.MailMessage;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;

import de.remsfal.test.AbstractTest;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class MailingResourceTest extends AbstractTest {

    static final String BASE_PATH = "/notification/test";

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void init() {
        mailbox.clear();
    }

    @Test
    void shouldFail_whenMissingRecipient() {
        given()
                .queryParam("name", "NoRecipient")
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void testTextMail() {
        // call a REST endpoint that sends email
        given()
                .queryParam("to", "test@example.com")
                .queryParam("name", "TestUser")
                .queryParam("template", "new-membership")
                .queryParam("link", "https://remsfal.de")
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(Status.ACCEPTED.getStatusCode());

        // verify that it was sent
        List<MailMessage> sent = mailbox. getMailMessagesSentTo("test@example.com");
        assertEquals(4, sent.size());
        MailMessage actual = sent.get(2);
        assertTrue(actual.getHtml().contains("You have been added to a new project."));
        assertTrue(actual.getSubject().contains("been added to a new project"));
        assertEquals(4, mailbox.getTotalMessagesSent());
    }

    @Test
    void testIssueAssignedEmail() {
        given()
                .queryParam("to", "assigned@example.com")
                .when()
                .get(BASE_PATH + "/issue-assigned")
                .then()
                .statusCode(Status.ACCEPTED.getStatusCode());

        List<MailMessage> sent = mailbox.getMailMessagesSentTo("assigned@example.com");
        assertEquals(1, sent.size());
        
        MailMessage actual = sent.get(0);
        assertTrue(actual.getHtml().contains("Test Issue Title"));
        assertTrue(actual.getHtml().contains("Test Project"));
        assertTrue(actual.getHtml().contains("OPEN"));
        assertEquals("[Issue Assigned] Test Issue Title", actual.getSubject());
        assertEquals(1, mailbox.getTotalMessagesSent());
    }

    @Test
    void testIssueCreatedEmail() {
        given()
                .queryParam("to", "created@example.com")
                .when()
                .get(BASE_PATH + "/issue-created")
                .then()
                .statusCode(Status.ACCEPTED.getStatusCode());

        List<MailMessage> sent = mailbox.getMailMessagesSentTo("created@example.com");
        assertEquals(1, sent.size());
        
        MailMessage actual = sent.get(0);
        assertTrue(actual.getHtml().contains("Test Issue Title"));
        assertTrue(actual.getHtml().contains("Test Project"));
        assertTrue(actual.getHtml().contains("OPEN"));
        assertEquals("[Issue Created] Test Issue Title", actual.getSubject());
        assertEquals(1, mailbox.getTotalMessagesSent());
    }

    @Test
    void testIssueUpdatedEmail() {
        given()
                .queryParam("to", "updated@example.com")
                .when()
                .get(BASE_PATH + "/issue-updated")
                .then()
                .statusCode(Status.ACCEPTED.getStatusCode());

        List<MailMessage> sent = mailbox.getMailMessagesSentTo("updated@example.com");
        assertEquals(1, sent.size());
        
        MailMessage actual = sent.get(0);
        assertTrue(actual.getHtml().contains("Test Issue Title"));
        assertTrue(actual.getHtml().contains("Test Project"));
        assertTrue(actual.getHtml().contains("OPEN"));
        assertEquals("[Issue Updated] Test Issue Title", actual.getSubject());
        assertEquals(1, mailbox.getTotalMessagesSent());
    }

    @Test
    void testIssueAssignedEmail_HandlesNullRecipientName() {
        given()
                .queryParam("to", "noname@example.com")
                .when()
                .get(BASE_PATH + "/issue-assigned")
                .then()
                .statusCode(Status.ACCEPTED.getStatusCode());

        List<MailMessage> sent = mailbox.getMailMessagesSentTo("noname@example.com");
        assertEquals(1, sent.size());
        
        MailMessage actual = sent.get(0);
        // Should default to "User" greeting
        assertTrue(actual.getHtml().contains("Dear User") || actual.getHtml().contains("Liebe(r) User"));
        assertEquals(1, mailbox.getTotalMessagesSent());
    }
}