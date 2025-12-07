package de.remsfal.service.boundary;

import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class OrganizationResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/organization";

    @BeforeEach
    protected void setupTestUsers() {
        super.setupTestUsers();
    }

    @Test
    void getOrganizations_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void createOrganization_SUCCESS_organizationIsCreated() {
        final String json = "{ \"id\": null,\n" +
                "  \"name\": \"TEst\",\n" +
                "  \"phone\": \"39779772519\",\n" +
                "  \"email\": \"test@test.com\",\n" +
                "  \"trade\": \"string\"\n" +
                "}";
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(ContentType.JSON)
                .header("location", Matchers.containsString(BASE_PATH + "/"))
//                .and().body("id", Matchers.notNullValue())
//                .and().body("title", Matchers.equalTo(TestData.PROJECT_TITLE))
//                .and().body("members.id", Matchers.hasItem(TestData.USER_ID.toString()))
//                .and().body("members.role", Matchers.hasItem("MANAGER"))
                ;

//        long enties = entityManager
//                .createQuery("SELECT count(project) FROM ProjectEntity project where project.title = :title", Long.class)
//                .setParameter("title", TestData.PROJECT_TITLE)
//                .getSingleResult();
//        assertEquals(1, enties);
    }
}
