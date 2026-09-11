package de.remsfal.ticketing.boundary.tenant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.control.IssueRequestController;
import de.remsfal.core.json.ticketing.ImmutableIssueRequestJson;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class TenantIssueRequestResourceTest extends AbstractTicketingTest {

    static final String REQUESTS_PATH = "/ticketing/v1/tenant-relations/issues/{issueId}/requests";

    static final UUID PROJECT_ID = UUID.randomUUID();
    static final UUID AGREEMENT_ID = UUID.randomUUID();
    static final UUID ISSUE_ID_WITH_AGREEMENT = UUID.randomUUID();
    static final UUID ISSUE_ID_WITHOUT_AGREEMENT = UUID.randomUUID();

    @Inject
    IssueRequestController issueRequestController;

    @BeforeEach
    void setUpIssues() {
        insertIssue(PROJECT_ID, ISSUE_ID_WITH_AGREEMENT,
            "Tenant issue", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), AGREEMENT_ID, null, "Issue for tenant issue-request tests");
        insertIssue(PROJECT_ID, ISSUE_ID_WITHOUT_AGREEMENT,
            "Tenant issue without agreement", IssueType.TASK, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), null, null, "Issue without an agreement");
    }

    private io.restassured.http.Cookie tenantCookie() {
        return buildCookie(UUID.randomUUID(), "tenant@example.com", "Tenant", Map.of(), Map.of(),
            Map.of(AGREEMENT_ID.toString(), PROJECT_ID.toString()));
    }

    @Test
    void getRequests_SUCCESS_returnsRequestsAcrossAllContractorOrganizations() {
        issueRequestController.createRequest(ISSUE_ID_WITH_AGREEMENT, UUID.randomUUID(),
            ImmutableIssueRequestJson.builder().message("Nachricht Firma A").build());
        issueRequestController.createRequest(ISSUE_ID_WITH_AGREEMENT, UUID.randomUUID(),
            ImmutableIssueRequestJson.builder().message("Nachricht Firma B").build());
        issueRequestController.createRequest(UUID.randomUUID(), UUID.randomUUID(),
            ImmutableIssueRequestJson.builder().message("Anderes Issue").build());

        given()
            .when()
            .cookie(tenantCookie())
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("requests", hasSize(2));
    }

    @Test
    void getRequests_FAILED_issueHasNoAgreement_returns403() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "tenant@example.com", "Tenant", Map.of(), Map.of(),
                Map.of()))
            .get(REQUESTS_PATH, ISSUE_ID_WITHOUT_AGREEMENT)
            .then()
            .statusCode(403);
    }

    @Test
    void getRequests_FAILED_tenantNotOnThisAgreement_returns403() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other-tenant@example.com", "Other Tenant",
                Map.of(), Map.of(), Map.of(UUID.randomUUID().toString(), PROJECT_ID.toString())))
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(403);
    }

    @Test
    void getRequests_FAILED_tenancyProjectMismatch_returns403() {
        given()
            .when()
            .cookie(buildCookie(UUID.randomUUID(), "other-tenant@example.com", "Other Tenant",
                Map.of(), Map.of(), Map.of(AGREEMENT_ID.toString(), UUID.randomUUID().toString())))
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(403);
    }

    @Test
    void getRequests_FAILED_managerCannotUseTenantEndpoint_returns403() {
        given()
            .when()
            .cookie(buildManagerCookie(Map.of(PROJECT_ID.toString(), "MANAGER")))
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(403);
    }

    @Test
    void getRequests_FAILED_unknownIssueId_returns404() {
        given()
            .when()
            .cookie(tenantCookie())
            .get(REQUESTS_PATH, UUID.randomUUID())
            .then()
            .statusCode(404);
    }

    @Test
    void getRequests_FAILED_unauthenticated_returns401() {
        given()
            .when()
            .get(REQUESTS_PATH, ISSUE_ID_WITH_AGREEMENT)
            .then()
            .statusCode(401);
    }
}
