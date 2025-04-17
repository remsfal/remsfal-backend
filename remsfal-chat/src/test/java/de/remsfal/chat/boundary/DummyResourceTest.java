package de.remsfal.chat.boundary;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class DummyResourceTest {

    static final String BASE_PATH = "/api/v1/address";

    @Test
    void getSupportedCountries_SUCCESS_nullIsReturned() {
        given()
            .when()
            .get(BASE_PATH + "/countries")
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

}