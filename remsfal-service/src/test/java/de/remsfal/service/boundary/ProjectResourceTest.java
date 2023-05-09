package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.TestData;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ProjectResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects";

    @BeforeEach
    void setupTestUsers() {
        super.setupTestUsers();
    }

    @Test
    void testHelloEndpoint() {
        given()
            .when()
            .log().all()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
            // .accept(ContentType.TEXT)
            .get(BASE_PATH)
            .then()
            .log().all()
            .statusCode(200)
            .body(is("Hello RESTEasy"));
    }

}