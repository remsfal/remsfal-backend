package de.remsfal.notification.boundary;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.mail.MailMessage;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class MailingResourceTest {

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
        assertEquals("You’ve been added to a new project", actual.getSubject());
        assertEquals(4, mailbox.getTotalMessagesSent());
    }
}