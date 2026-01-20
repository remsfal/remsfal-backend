package de.remsfal.service.boundary.project;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ProjectOrganizationResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/organizations";
    static final String ORGANIZATION_PATH = BASE_PATH + "/{organizationId}";

    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestOrganizations();
    }

    @Test
    void getProjectOrganizations_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void addProjectOrganization_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"organizationId\":\"" + TestData.ORGANIZATION_ID_2 + "\",  \"role\":\"LESSOR\"}")
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void addProjectOrganization_FAILED_notPrivileged() {
        // Add USER_2 as STAFF (not privileged)
        insertProjectMember(TestData.PROJECT_ID, TestData.USER_ID_2, "STAFF");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"organizationId\":\"" + TestData.ORGANIZATION_ID_2 + "\",  \"role\":\"LESSOR\"}")
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateProjectOrganization_FAILED_notMember() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID, "LESSOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"role\":\"MANAGER\"}")
            .patch(ORGANIZATION_PATH, TestData.PROJECT_ID.toString(), TestData.ORGANIZATION_ID.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateProjectOrganization_FAILED_notPrivileged() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID, "LESSOR");
        insertProjectMember(TestData.PROJECT_ID, TestData.USER_ID_2, "STAFF");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"role\":\"MANAGER\"}")
            .patch(ORGANIZATION_PATH, TestData.PROJECT_ID.toString(), TestData.ORGANIZATION_ID.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteProjectOrganization_FAILED_notMember() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID, "LESSOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .delete(ORGANIZATION_PATH, TestData.PROJECT_ID.toString(), TestData.ORGANIZATION_ID.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteProjectOrganization_FAILED_notPrivileged() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID, "LESSOR");
        insertProjectMember(TestData.PROJECT_ID, TestData.USER_ID_2, "STAFF");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .delete(ORGANIZATION_PATH, TestData.PROJECT_ID.toString(), TestData.ORGANIZATION_ID.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateProjectOrganization_FAILED_organizationIdMustBeNull() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID, "LESSOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"organizationId\":\"" + TestData.ORGANIZATION_ID + "\",  \"role\":\"MANAGER\"}")
            .patch(ORGANIZATION_PATH, TestData.PROJECT_ID.toString(), TestData.ORGANIZATION_ID.toString())
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getProjectOrganizations_SUCCESS_noOrganizationsReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizations.size()", Matchers.equalTo(0));
    }

    @Test
    void getProjectOrganizations_SUCCESS_oneOrganizationReturned() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID, "LESSOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizations.size()", Matchers.equalTo(1))
            .and().body("organizations.organizationId", Matchers.hasItem(TestData.ORGANIZATION_ID.toString()))
            .and().body("organizations.organizationName", Matchers.hasItem(TestData.ORGANIZATION_NAME))
            .and().body("organizations.role", Matchers.hasItem("LESSOR"));
    }

    @Test
    void addProjectOrganization_SUCCESS_newOrganizationReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"organizationId\":\"" + TestData.ORGANIZATION_ID_2 + "\",  \"role\":\"STAFF\"}")
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizationId", Matchers.equalTo(TestData.ORGANIZATION_ID_2.toString()))
            .and().body("organizationName", Matchers.equalTo(TestData.ORGANIZATION_NAME_2))
            .and().body("role", Matchers.equalTo("STAFF"));
    }

    @Test
    void addProjectOrganization_SUCCESS_asManager() {
        // Add USER_2 as MANAGER
        insertProjectMember(TestData.PROJECT_ID, TestData.USER_ID_2, "MANAGER");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"organizationId\":\"" + TestData.ORGANIZATION_ID_2 + "\",  \"role\":\"LESSOR\"}")
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizationId", Matchers.equalTo(TestData.ORGANIZATION_ID_2.toString()))
            .and().body("organizationName", Matchers.equalTo(TestData.ORGANIZATION_NAME_2))
            .and().body("role", Matchers.equalTo("LESSOR"));
    }

    @Test
    void updateProjectOrganization_SUCCESS_organizationWithChangedRoleReturned() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID, "STAFF");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"role\":\"MANAGER\"}")
            .patch(ORGANIZATION_PATH, TestData.PROJECT_ID.toString(), TestData.ORGANIZATION_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizationId", Matchers.equalTo(TestData.ORGANIZATION_ID.toString()))
            .and().body("organizationName", Matchers.equalTo(TestData.ORGANIZATION_NAME))
            .and().body("role", Matchers.equalTo("MANAGER"));
    }

    @Test
    void updateProjectOrganization_SUCCESS_asManager() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID, "STAFF");
        insertProjectMember(TestData.PROJECT_ID, TestData.USER_ID_2, "MANAGER");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"role\":\"LESSOR\"}")
            .patch(ORGANIZATION_PATH, TestData.PROJECT_ID.toString(), TestData.ORGANIZATION_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizationId", Matchers.equalTo(TestData.ORGANIZATION_ID.toString()))
            .and().body("organizationName", Matchers.equalTo(TestData.ORGANIZATION_NAME))
            .and().body("role", Matchers.equalTo("LESSOR"));
    }

    @Test
    void deleteProjectOrganization_SUCCESS_organizationDeleted() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID_2, "LESSOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .delete(ORGANIZATION_PATH, TestData.PROJECT_ID.toString(), TestData.ORGANIZATION_ID_2.toString())
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // Verify organization is no longer in project
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizations.size()", Matchers.equalTo(0));
    }

    @Test
    void deleteProjectOrganization_SUCCESS_asManager() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID_2, "LESSOR");
        insertProjectMember(TestData.PROJECT_ID, TestData.USER_ID_2, "MANAGER");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .delete(ORGANIZATION_PATH, TestData.PROJECT_ID.toString(), TestData.ORGANIZATION_ID_2.toString())
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void addProjectOrganization_FAILED_organizationDoesNotExist() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"organizationId\":\"00000000-0000-0000-0000-000000000000\",  \"role\":\"STAFF\"}")
            .post(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void getProjectOrganizations_SUCCESS_multipleOrganizationsReturned() {
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID, "LESSOR");
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID_2, "STAFF");
        insertProjectOrganization(TestData.PROJECT_ID, TestData.ORGANIZATION_ID_3, "MANAGER");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("organizations.size()", Matchers.equalTo(3))
            .and().body("organizations.organizationId", Matchers.hasItems(
                TestData.ORGANIZATION_ID.toString(),
                TestData.ORGANIZATION_ID_2.toString(),
                TestData.ORGANIZATION_ID_3.toString()))
            .and().body("organizations.role", Matchers.hasItems("LESSOR", "STAFF", "MANAGER"));
    }

}
