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
import java.util.UUID;

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
        final String json = "{\"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";
        final String organizationId = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(BASE_PATH + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("name", Matchers.equalTo(TestData.ORGANIZATION_NAME))
            .and().body("phone", Matchers.equalTo(TestData.ORGANIZATION_PHONE))
            .and().body("email", Matchers.equalTo(TestData.ORGANIZATION_EMAIL))
            .and().body("trade", Matchers.equalTo(TestData.ORGANIZATION_TRADE))
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");;

        long entities = entityManager
                .createQuery("SELECT count(organization) FROM OrganizationEntity organization where organization.id = :organizationId", Long.class)
                .setParameter("organizationId", UUID.fromString(organizationId))
                .getSingleResult();
        assertEquals(1, entities);
    }

    @Test
    void createProject_FAILED_idIsProvided() {
        final String json = "{ \"id\": " + TestData.ORGANIZATION_ID + ",\n" +
                "  \"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(ContentType.JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void createProject_FAILED_noAuthentication() {
        final String json = "{\"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        given()
            .when()
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void createProject_FAILED_noTitle() {
        final String json = "{\"name\": \" \",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProject_SUCCESS_sameProjectIsReturned() {
        final String json = "{\"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        final io.restassured.response.Response res = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .post(BASE_PATH)
                .thenReturn();

        final String organizationId = res.then()
                .contentType(MediaType.APPLICATION_JSON)
                .extract().path("id");

        final String organizationUrl = res.then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("location", Matchers.startsWith("http://localhost:8081/api/v1/organization"))
                .header("location", Matchers.endsWith(organizationId))
                .extract().header("location");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .get(organizationUrl)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(organizationId))
                .and().body("name", Matchers.equalTo(TestData.ORGANIZATION_NAME))
                .and().body("phone", Matchers.equalTo(TestData.ORGANIZATION_PHONE))
                .and().body("email", Matchers.equalTo(TestData.ORGANIZATION_EMAIL))
                .and().body("trade", Matchers.equalTo(TestData.ORGANIZATION_TRADE));
    }

    @Test
    void updateProject_SUCCESS_changedTitle() {
        final String json = "{\"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        final String organizationId = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH)
            .then()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .extract().path("id");

        final String json_updated = "{\"id\": \"" + organizationId + "\",\n" +
                "  \"name\": \"" + TestData.ORGANIZATION_NAME_2 + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE_2 + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL_2 + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE_2 + "\"\n" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json_updated)
            .patch(BASE_PATH + "/" + organizationId)
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(organizationId))
            .and().body("name", Matchers.equalTo(TestData.ORGANIZATION_NAME_2))
            .and().body("phone", Matchers.equalTo(TestData.ORGANIZATION_PHONE_2))
            .and().body("email", Matchers.equalTo(TestData.ORGANIZATION_EMAIL_2))
            .and().body("trade", Matchers.equalTo(TestData.ORGANIZATION_TRADE_2));
    }

    @Test
    void deleteProject_SUCCESS_singleProject() {
        final String json = "{\"name\": \"" + TestData.ORGANIZATION_NAME + "\",\n" +
                "  \"phone\": \"" + TestData.ORGANIZATION_PHONE + "\",\n" +
                "  \"email\": \"" + TestData.ORGANIZATION_EMAIL + "\",\n" +
                "  \"trade\": \"" + TestData.ORGANIZATION_TRADE + "\"\n" +
                "}";

        final String organizationId = given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .post(BASE_PATH)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().path("id");

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
                .delete(BASE_PATH + "/" + organizationId)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        long enties = entityManager
                .createQuery("SELECT count(organization) FROM OrganizationEntity organization where organization.id = :id", Long.class)
                .setParameter("id", UUID.fromString(organizationId))
                .getSingleResult();
        assertEquals(0, enties);
    }
}
