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
class ContractorResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/contractors";
    static final String CONTRACTOR_PATH = BASE_PATH + "/{contractorId}";

    // Test data for contractors
    private static final String COMPANY_NAME_1 = "Test Contractor 1";
    private static final String COMPANY_NAME_2 = "Test Contractor 2";
    private static final String PHONE_1 = "+491234567890";
    private static final String PHONE_2 = "+491234567891";
    private static final String EMAIL_1 = "contractor1@example.com";
    private static final String EMAIL_2 = "contractor2@example.com";
    private static final String TRADE_1 = "Plumbing";
    private static final String TRADE_2 = "Electrical";

    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
    }

    @Test
    void getContractors_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void createContractor_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"companyName\":\"" + COMPANY_NAME_1 + "\", \"phone\":\"" + PHONE_1 + "\", \"email\":\"" + EMAIL_1 + "\", \"trade\":\"" + TRADE_1 + "\"}")
            .post(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void getContractor_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .get(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), "b9440c43-b5c0-4951-9c29-000000000001")
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void updateContractor_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{ \"id\":\"b9440c43-b5c0-4951-9c29-000000000001\", \"companyName\":\"" + COMPANY_NAME_2 + "\"}")
            .patch(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), "b9440c43-b5c0-4951-9c29-000000000001")
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void deleteContractor_FAILED_notMember() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID_2.toString(), TestData.USER_EMAIL_2, Duration.ofMinutes(100)))
            .delete(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), "b9440c43-b5c0-4951-9c29-000000000001")
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void createContractor_SUCCESS_contractorCreated() {
        String json = "{ \"companyName\":\"" + COMPANY_NAME_1 + "\", \"phone\":\"" + PHONE_1 + "\", \"email\":\"" + EMAIL_1 + "\", \"trade\":\"" + TRADE_1 + "\"}";

        String contractorId = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(BASE_PATH.replace("{projectId}", TestData.PROJECT_ID_1.toString()) + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("companyName", Matchers.equalTo(COMPANY_NAME_1))
            .and().body("phone", Matchers.equalTo(PHONE_1))
            .and().body("email", Matchers.equalTo(EMAIL_1))
            .and().body("trade", Matchers.equalTo(TRADE_1))
            .extract().path("id");

        // Verify the contractor was created
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("contractors.size()", Matchers.equalTo(1))
            .and().body("contractors[0].id", Matchers.equalTo(contractorId))
            .and().body("contractors[0].companyName", Matchers.equalTo(COMPANY_NAME_1));
    }

    @Test
    void getContractors_SUCCESS_emptyList() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .get(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("contractors.size()", Matchers.equalTo(0))
            .and().body("offset", Matchers.equalTo(0))
            .and().body("total", Matchers.equalTo(0));
    }

    @Test
    void getContractor_SUCCESS_contractorReturned() {
        // Create a contractor
        String json = "{ \"companyName\":\"" + COMPANY_NAME_1 + "\", \"phone\":\"" + PHONE_1 + "\", \"email\":\"" + EMAIL_1 + "\", \"trade\":\"" + TRADE_1 + "\"}";

        String contractorId = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        // Get the contractor
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .get(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), contractorId)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(contractorId))
            .and().body("companyName", Matchers.equalTo(COMPANY_NAME_1))
            .and().body("phone", Matchers.equalTo(PHONE_1))
            .and().body("email", Matchers.equalTo(EMAIL_1))
            .and().body("trade", Matchers.equalTo(TRADE_1));
    }

    @Test
    void getContractor_FAILED_contractorNotFound() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .get(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), "b9440c43-b5c0-4951-9c29-000000000099")
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void updateContractor_SUCCESS_contractorUpdated() {
        // Create a contractor
        String json = "{ \"companyName\":\"" + COMPANY_NAME_1 + "\", \"phone\":\"" + PHONE_1 + "\", \"email\":\"" + EMAIL_1 + "\", \"trade\":\"" + TRADE_1 + "\"}";

        String contractorId = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        // Update the contractor
        String updateJson = "{ \"id\":\"" + contractorId + "\", \"companyName\":\"" + COMPANY_NAME_2 + "\", \"phone\":\"" + PHONE_2 + "\", \"email\":\"" + EMAIL_2 + "\", \"trade\":\"" + TRADE_2 + "\"}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(updateJson)
            .patch(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), contractorId)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("id", Matchers.equalTo(contractorId))
            .and().body("companyName", Matchers.equalTo(COMPANY_NAME_2))
            .and().body("phone", Matchers.equalTo(PHONE_2))
            .and().body("email", Matchers.equalTo(EMAIL_2))
            .and().body("trade", Matchers.equalTo(TRADE_2));

        // Verify the contractor was updated
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .get(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), contractorId)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("companyName", Matchers.equalTo(COMPANY_NAME_2));
    }

    @Test
    void updateContractor_FAILED_contractorNotFound() {
        String updateJson = "{ \"id\":\"b9440c43-b5c0-4951-9c29-000000000099\", \"companyName\":\"" + COMPANY_NAME_2 + "\"}";

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(updateJson)
            .patch(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), "b9440c43-b5c0-4951-9c29-000000000099")
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteContractor_SUCCESS_contractorDeleted() {
        // Create a contractor
        String json = "{ \"companyName\":\"" + COMPANY_NAME_1 + "\", \"phone\":\"" + PHONE_1 + "\", \"email\":\"" + EMAIL_1 + "\", \"trade\":\"" + TRADE_1 + "\"}";

        String contractorId = given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID_1.toString())
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        // Delete the contractor
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .delete(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), contractorId)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // Verify the contractor was deleted
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .get(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), contractorId)
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteContractor_FAILED_contractorNotFound() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID.toString(), TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .delete(CONTRACTOR_PATH, TestData.PROJECT_ID_1.toString(), "b9440c43-b5c0-4951-9c29-000000000099")
            .then()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }
}
