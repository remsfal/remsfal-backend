package de.remsfal.service.boundary.organization;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class EmployeeResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/organization/{organizationId}/employees";
    static final String EMPLOYEE_PATH = BASE_PATH + "/{employeeId}";

    @BeforeEach
    protected void setupTestDate() {
        super.setupTestUsers();
        super.setupTestOrganizations();
    }

    @AfterEach
    @Transactional
    protected void cleanupTestData() {
        entityManager.createNativeQuery("DELETE FROM organization").executeUpdate();
    }

    @Test
    void getEmployees_FAILED_notEmployee() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.ORGANIZATION_ID.toString())
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void addEmployee_FAILED_notEmployee() {
        final String json = "{\"id\": " + null + ",\n" +
                "  \"name\": " + null + ",\n" +
                "  \"email\": \"" + TestData.EMPLOYEE_EMAIL + "\",\n" +
                "  \"active\": " + TestData.EMPLOYEE_ACTIVE + ",\n" +
                "  \"employeeRole\": \"" + TestData.EMPLOYEE_ROLE + "\"\n" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.ORGANIZATION_ID.toString())
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateEmployee_FAILED_notEmployee() {
        final String json = "{\"id\": " + null + ",\n" +
                "  \"name\": " + null + ",\n" +
                "  \"email\": " + null + ",\n" +
                "  \"active\": " + TestData.EMPLOYEE_ACTIVE + ",\n" +
                "  \"employeeRole\": \"STAFF\"\n" +
                "}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .patch(EMPLOYEE_PATH, TestData.ORGANIZATION_ID.toString(), TestData.USER_ID_1.toString())
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteEmployee_FAILED_notEmployee() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .delete(EMPLOYEE_PATH, TestData.ORGANIZATION_ID.toString(), TestData.USER_ID_1.toString())
            .then()
            .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteEmployee_FAILED_notOwner() {
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
                .delete(EMPLOYEE_PATH, TestData.ORGANIZATION_ID.toString(), TestData.USER_ID_1.toString())
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateEmployee_FAILED_emailMustBeNull() {
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"email\":\"" + TestData.USER_EMAIL_2 + "\",  \"role\":\"LESSOR\"}")
                .patch(EMPLOYEE_PATH, TestData.ORGANIZATION_ID.toString(), TestData.USER_ID_1.toString())
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getEmployee_SUCCESS_singleEmployeeReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
            .get(EMPLOYEE_PATH, TestData.ORGANIZATION_ID.toString(), TestData.USER_ID_1.toString())
            .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(TestData.USER_ID_1.toString()))
            .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_1))
            .and().body("active", Matchers.equalTo(true))
            .and().body("employeeRole", Matchers.equalTo("OWNER"));
    }

    @Test
    void getEmployees_SUCCESS_EmployeeListReturned() {
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
                .get(BASE_PATH, TestData.ORGANIZATION_ID.toString())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("employees.size()", Matchers.equalTo(1))
                .and().body("employees.id", Matchers.hasItem(TestData.USER_ID_1.toString()))
                .and().body("employees.email", Matchers.hasItem(TestData.USER_EMAIL_1))
                .and().body("employees.active", Matchers.hasItem(true))
                .and().body("employees.employeeRole", Matchers.hasItem("OWNER"));
    }

    @Test
    void addEmployee_SUCCESS_newEmployeeReturned() {
        final String json = "{\"email\": \"" + TestData.EMPLOYEE_EMAIL + "\",\n" +
                "  \"employeeRole\": \"STAFF\"\n" +
                "}";

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .post(BASE_PATH, TestData.ORGANIZATION_ID.toString())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.notNullValue())
                .and().body("email", Matchers.equalTo(TestData.EMPLOYEE_EMAIL))
                .and().body("active", Matchers.is(false))
                .and().body("employeeRole", Matchers.equalTo("STAFF"));
    }

    @Test
    void addEmployee_SUCCESS_existingEmployeeReturned() {
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"email\":\"" + TestData.USER_EMAIL_2 + "\",  \"employeeRole\":\"MANAGER\"}")
                .post(BASE_PATH, TestData.ORGANIZATION_ID.toString())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(TestData.USER_ID_2.toString()))
                .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_2))
                .and().body("active", Matchers.is(true))
                .and().body("employeeRole", Matchers.equalTo("MANAGER"));
    }

    @Test
    void updateEmployee_SUCCESS_employeeWithChangedRoleReturned() {
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"employeeRole\":\"MANAGER\"}")
                .patch(EMPLOYEE_PATH, TestData.ORGANIZATION_ID.toString(), TestData.USER_ID_1.toString())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(TestData.USER_ID_1.toString()))
                .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_1))
                .and().body("active", Matchers.is(true))
                .and().body("employeeRole", Matchers.equalTo("MANAGER"));
    }

    @Test
    void deleteEmployee_SUCCESS_userDeleted() {
        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"email\":\"" + TestData.USER_EMAIL_2 + "\",  \"employeeRole\":\"MANAGER\"}")
                .post(BASE_PATH, TestData.ORGANIZATION_ID.toString())
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
                .delete(EMPLOYEE_PATH, TestData.ORGANIZATION_ID.toString(), TestData.USER_ID_2.toString())
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    void deleteEmployee_SUCCESS_userStillThere() {
        final String USER_PATH = "/api/v1/user";

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(100)))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"email\":\"" + TestData.USER_EMAIL_2 + "\",  \"employeeRole\":\"MANAGER\"}")
                .post(BASE_PATH, TestData.ORGANIZATION_ID.toString())
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
                .cookie(buildRefreshTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
                .delete(EMPLOYEE_PATH, TestData.ORGANIZATION_ID.toString(), TestData.USER_ID_2.toString())
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given()
                .when()
                .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
                .get(USER_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .and().body("id", Matchers.equalTo(TestData.USER_ID_2.toString()))
                .and().body("firstName", Matchers.equalTo(TestData.USER_FIRST_NAME_2))
                .and().body("lastName", Matchers.equalTo(TestData.USER_LAST_NAME_2))
                .and().body("email", Matchers.equalTo(TestData.USER_EMAIL_2));
    }
}
