package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class ZeebeControllerTest {

    @Test
    void testStartWorkflowSuccess() {
        given()
        .contentType(ContentType.JSON)
        .body("""
               {
                 "processId": "ticket-process",
                 "variables": {
                   "approved": "true"
                 }
               }
               """)
        .when()
        .post("/zeebe/start")
        .then()
        .statusCode(Response.Status.OK.getStatusCode())
        .body(containsString("Workflow 'test-process' started with key:"));
    }

    @Test
    void testStartWorkflowMissingProcessId() {
        given()
        .contentType(ContentType.JSON)
        .body("""
               {
                 "variables": {
                   "approved": "true"
                 }
               }
               """)
        .when()
        .post("/zeebe/start")
        .then()
        .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
        .body(equalTo("Error: Missing 'processId' in request body."));
    }

}