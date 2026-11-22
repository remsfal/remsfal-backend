package de.remsfal.service.boundary.project;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * Tests for the managing organization of a project and the invariant that every project always has a proprietor.
 * In the test organizations, USER_1 is OWNER, USER_2 is MANAGER and USER_3 is STAFF of ORGANIZATION_3.
 * USER_4 is not an employee of any organization.
 */
@QuarkusTest
class ManagingOrganizationResourceTest extends AbstractResourceTest {

    static final String PROJECTS_PATH = "/api/v1/projects";
    static final String PROJECT_PATH = PROJECTS_PATH + "/{projectId}";
    static final String PROJECT_ORGANIZATION_PATH = PROJECT_PATH + "/organizations/{organizationId}";
    static final String PROJECT_MEMBER_PATH = PROJECT_PATH + "/members/{memberId}";
    static final String ORGANIZATION_PATH = "/api/v1/organizations/{organizationId}";
    static final String USER_PATH = "/api/v1/user";

    @BeforeEach
    void setupTestData() {
        setupTestUsers();
        setupTestOrganizations();
    }

    private ValidatableResponse createProject(final UUID userId, final String email, final UUID organizationId) {
        final String json = organizationId == null
            ? "{\"title\":\"" + TestData.PROJECT_TITLE + "\"}"
            : "{\"title\":\"" + TestData.PROJECT_TITLE + "\",\"managingOrganizationId\":\"" + organizationId + "\"}";
        return given()
            .when()
            .cookie(buildAccessTokenCookie(userId, email, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(PROJECTS_PATH)
            .then();
    }

    private String createOrganizationProject(final UUID userId, final String email) {
        return createProject(userId, email, TestData.ORGANIZATION_ID_3)
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");
    }

    private long countDirectMembers(final String projectId) {
        return entityManager
            .createQuery("SELECT count(m) FROM ProjectMembershipEntity m WHERE m.project.id = :projectId", Long.class)
            .setParameter("projectId", UUID.fromString(projectId))
            .getSingleResult();
    }

    private UUID getManagingOrganizationId(final String projectId) {
        final Object result = entityManager
            .createNativeQuery("SELECT managing_organization_id FROM projects WHERE id = ?")
            .setParameter(1, UUID.fromString(projectId))
            .getSingleResult();
        return result == null ? null : UUID.fromString(result.toString());
    }

    // --- create ---

    @Test
    void createProject_SUCCESS_forOrganizationAsManager() {
        final String projectId = createProject(TestData.USER_ID_2, TestData.USER_EMAIL_2, TestData.ORGANIZATION_ID_3)
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .body("managingOrganizationId", Matchers.equalTo(TestData.ORGANIZATION_ID_3.toString()))
            .body("managingOrganizationName", Matchers.equalTo(TestData.ORGANIZATION_NAME_3))
            .body("owner", Matchers.equalTo(TestData.ORGANIZATION_NAME_3))
            .body("billingAddress.street", Matchers.equalTo(TestData.ADDRESS_STREET_2))
            .extract().path("id");

        assertEquals(0, countDirectMembers(projectId));
        final String role = entityManager
            .createQuery("SELECT po.role FROM ProjectOrganizationEntity po "
                + "WHERE po.project.id = :projectId AND po.organization.id = :organizationId", Object.class)
            .setParameter("projectId", UUID.fromString(projectId))
            .setParameter("organizationId", TestData.ORGANIZATION_ID_3)
            .getSingleResult().toString();
        assertEquals("PROPRIETOR", role);
    }

    @Test
    void createProject_SUCCESS_forOrganizationAsOwner() {
        createProject(TestData.USER_ID_1, TestData.USER_EMAIL_1, TestData.ORGANIZATION_ID_3)
            .statusCode(Status.CREATED.getStatusCode())
            .body("managingOrganizationId", Matchers.equalTo(TestData.ORGANIZATION_ID_3.toString()));
    }

    @Test
    void createProject_SUCCESS_withoutOrganizationCreatorIsProprietor() {
        createProject(TestData.USER_ID_1, TestData.USER_EMAIL_1, null)
            .statusCode(Status.CREATED.getStatusCode())
            .body("managingOrganizationId", Matchers.nullValue())
            .body("members.id", Matchers.hasItem(TestData.USER_ID_1.toString()))
            .body("members.role", Matchers.hasItem("PROPRIETOR"));
    }

    @Test
    void createProject_FAILED_forOrganizationAsStaff() {
        createProject(TestData.USER_ID_3, TestData.USER_EMAIL_3, TestData.ORGANIZATION_ID_3)
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void createProject_FAILED_notEmployee() {
        createProject(TestData.USER_ID_4, TestData.USER_EMAIL_4, TestData.ORGANIZATION_ID_3)
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void createProject_FAILED_organizationNotFound() {
        createProject(TestData.USER_ID_1, TestData.USER_EMAIL_1, UUID.randomUUID())
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void createProject_FAILED_managingOrganizationNameProvided() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body("{\"title\":\"" + TestData.PROJECT_TITLE + "\",\"managingOrganizationName\":\"Any\"}")
            .post(PROJECTS_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    // --- access through the organization ---

    @Test
    void getProjects_SUCCESS_organizationProjectVisibleForEmployees() {
        final String projectId = createOrganizationProject(TestData.USER_ID_2, TestData.USER_EMAIL_2);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(PROJECTS_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body("total", Matchers.equalTo(1))
            .body("projects.id", Matchers.hasItem(projectId))
            .body("projects.memberRole", Matchers.hasItem("STAFF"));

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .get(PROJECTS_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body("projects.find { it.id == '" + projectId + "' }.memberRole", Matchers.equalTo("PROPRIETOR"));

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .get(PROJECTS_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body("total", Matchers.equalTo(0));
    }

    @Test
    void getProject_SUCCESS_organizationProjectReadableForEmployees() {
        final String projectId = createOrganizationProject(TestData.USER_ID_2, TestData.USER_EMAIL_2);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_3, TestData.USER_EMAIL_3, Duration.ofMinutes(10)))
            .get(PROJECT_PATH, projectId)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body("managingOrganizationName", Matchers.equalTo(TestData.ORGANIZATION_NAME_3));
    }

    // --- change the managing organization ---

    @Test
    void updateProject_SUCCESS_managingOrganizationChanged() {
        final String projectId = createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        insertProjectOrganization(UUID.fromString(projectId), TestData.ORGANIZATION_ID, "PROPRIETOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"title\":\"" + TestData.PROJECT_TITLE + "\",\"managingOrganizationId\":\""
                + TestData.ORGANIZATION_ID + "\"}")
            .patch(PROJECT_PATH, projectId)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .body("managingOrganizationId", Matchers.equalTo(TestData.ORGANIZATION_ID.toString()))
            .body("managingOrganizationName", Matchers.equalTo(TestData.ORGANIZATION_NAME));
    }

    @Test
    void updateProject_FAILED_managingOrganizationIsNotProprietor() {
        final String projectId = createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        insertProjectOrganization(UUID.fromString(projectId), TestData.ORGANIZATION_ID, "STAFF");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"title\":\"" + TestData.PROJECT_TITLE + "\",\"managingOrganizationId\":\""
                + TestData.ORGANIZATION_ID + "\"}")
            .patch(PROJECT_PATH, projectId)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateProject_FAILED_managingOrganizationIsNotMember() {
        final String projectId = createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"title\":\"" + TestData.PROJECT_TITLE + "\",\"managingOrganizationId\":\""
                + TestData.ORGANIZATION_ID_2 + "\"}")
            .patch(PROJECT_PATH, projectId)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateProject_FAILED_managerCannotChangeManagingOrganization() {
        final String projectId = createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        insertProjectOrganization(UUID.fromString(projectId), TestData.ORGANIZATION_ID, "PROPRIETOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_2, TestData.USER_EMAIL_2, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"title\":\"" + TestData.PROJECT_TITLE + "\",\"managingOrganizationId\":\""
                + TestData.ORGANIZATION_ID + "\"}")
            .patch(PROJECT_PATH, projectId)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    // --- a project always has a proprietor ---

    @Test
    void deleteProjectOrganization_FAILED_managingOrganizationIsOnlyProprietor() {
        final String projectId = createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .delete(PROJECT_ORGANIZATION_PATH, projectId, TestData.ORGANIZATION_ID_3.toString())
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());

        assertEquals(TestData.ORGANIZATION_ID_3, getManagingOrganizationId(projectId));
    }

    @Test
    void updateProjectOrganization_FAILED_managingOrganizationIsOnlyProprietor() {
        final String projectId = createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"role\":\"MANAGER\"}")
            .patch(PROJECT_ORGANIZATION_PATH, projectId, TestData.ORGANIZATION_ID_3.toString())
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());
    }

    @Test
    void deleteProjectOrganization_SUCCESS_otherProprietorRemains() {
        final String projectId = createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        insertProjectMember(UUID.fromString(projectId), TestData.USER_ID_4, "PROPRIETOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .delete(PROJECT_ORGANIZATION_PATH, projectId, TestData.ORGANIZATION_ID_3.toString())
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        assertNull(getManagingOrganizationId(projectId));
    }

    @Test
    void updateProjectOrganization_SUCCESS_downgradeClearsManagingOrganization() {
        final String projectId = createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        insertProjectMember(UUID.fromString(projectId), TestData.USER_ID_4, "PROPRIETOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"role\":\"MANAGER\"}")
            .patch(PROJECT_ORGANIZATION_PATH, projectId, TestData.ORGANIZATION_ID_3.toString())
            .then()
            .statusCode(Status.OK.getStatusCode());

        assertNull(getManagingOrganizationId(projectId));
    }

    @Test
    void deleteProjectMember_FAILED_lastProprietor() {
        final String projectId = createProject(TestData.USER_ID_4, TestData.USER_EMAIL_4, null)
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .delete(PROJECT_MEMBER_PATH, projectId, TestData.USER_ID_4.toString())
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());
    }

    @Test
    void updateProjectMember_FAILED_lastProprietorDowngraded() {
        final String projectId = createProject(TestData.USER_ID_4, TestData.USER_EMAIL_4, null)
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"role\":\"MANAGER\"}")
            .patch(PROJECT_MEMBER_PATH, projectId, TestData.USER_ID_4.toString())
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());
    }

    @Test
    void deleteOrganization_FAILED_onlyProprietorOfProject() {
        createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .delete(ORGANIZATION_PATH, TestData.ORGANIZATION_ID_3.toString())
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());
    }

    @Test
    void deleteUser_FAILED_onlyProprietorOfProject() {
        createProject(TestData.USER_ID_4, TestData.USER_EMAIL_4, null)
            .statusCode(Status.CREATED.getStatusCode());

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .delete(USER_PATH)
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());
    }

    @Test
    void deleteUser_SUCCESS_otherProprietorRemains() {
        final String projectId = createProject(TestData.USER_ID_4, TestData.USER_EMAIL_4, null)
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");
        insertProjectOrganization(UUID.fromString(projectId), TestData.ORGANIZATION_ID, "PROPRIETOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .delete(USER_PATH)
            .then()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    // --- active proprietors and deletion of accounts and organizations ---

    private UUID insertInactiveUser(final String email) {
        final UUID userId = UUID.randomUUID();
        insertUser(userId, null, email, "Inactive", "User", null);
        return userId;
    }

    private UUID insertInactiveOrganization() {
        final UUID organizationId = UUID.randomUUID();
        insertOrganization(organizationId, "Inactive Org", null, "inactive-org@example.org", null, null);
        insertEmployee(organizationId, insertInactiveUser("inactive-owner@example.org"), "OWNER");
        return organizationId;
    }

    private UUID insertProjectWithOrganization(final UUID organizationId) {
        final UUID projectId = UUID.randomUUID();
        insertProject(projectId, TestData.PROJECT_TITLE);
        insertProjectOrganization(projectId, organizationId, "PROPRIETOR");
        return projectId;
    }

    private void setManagingOrganization(final UUID projectId, final UUID organizationId) {
        runInTransaction(() -> entityManager
            .createNativeQuery("UPDATE projects SET managing_organization_id = ? WHERE id = ?")
            .setParameter(1, organizationId)
            .setParameter(2, projectId)
            .executeUpdate());
    }

    private long countOrganizations(final UUID organizationId) {
        return entityManager
            .createQuery("SELECT count(o) FROM OrganizationEntity o WHERE o.id = :id", Long.class)
            .setParameter("id", organizationId)
            .getSingleResult();
    }

    private long countUsers(final UUID userId) {
        return entityManager
            .createQuery("SELECT count(u) FROM UserEntity u WHERE u.id = :id", Long.class)
            .setParameter("id", userId)
            .getSingleResult();
    }

    private ValidatableResponse deleteUser(final UUID userId, final String email) {
        return given()
            .when()
            .cookie(buildAccessTokenCookie(userId, email, Duration.ofMinutes(10)))
            .delete(USER_PATH)
            .then();
    }

    @Test
    void deleteUser_FAILED_onlyInactiveProprietorRemains() {
        final String projectId = createProject(TestData.USER_ID_4, TestData.USER_EMAIL_4, null)
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");
        insertProjectMember(UUID.fromString(projectId), insertInactiveUser("invited@example.org"), "PROPRIETOR");

        deleteUser(TestData.USER_ID_4, TestData.USER_EMAIL_4)
            .statusCode(Status.CONFLICT.getStatusCode());
        assertEquals(1, countUsers(TestData.USER_ID_4));
    }

    @Test
    void deleteProjectMember_FAILED_onlyInactiveProprietorRemains() {
        final String projectId = createProject(TestData.USER_ID_4, TestData.USER_EMAIL_4, null)
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");
        insertProjectMember(UUID.fromString(projectId), insertInactiveUser("invited@example.org"), "PROPRIETOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .delete(PROJECT_MEMBER_PATH, projectId, TestData.USER_ID_4.toString())
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());
    }

    @Test
    void deleteUser_SUCCESS_organizationsWithoutOtherActiveAdminAreDeleted() {
        // USER_ID_1 is the only employee of ORGANIZATION_ID and ORGANIZATION_ID_2,
        // ORGANIZATION_ID_3 has USER_ID_2 as another active MANAGER
        deleteUser(TestData.USER_ID_1, TestData.USER_EMAIL_1)
            .statusCode(Status.NO_CONTENT.getStatusCode());

        assertEquals(0, countUsers(TestData.USER_ID_1));
        assertEquals(0, countOrganizations(TestData.ORGANIZATION_ID));
        assertEquals(0, countOrganizations(TestData.ORGANIZATION_ID_2));
        assertEquals(1, countOrganizations(TestData.ORGANIZATION_ID_3));
    }

    @Test
    void deleteUser_SUCCESS_organizationWithOnlyInactiveAdminIsDeleted() {
        insertEmployee(TestData.ORGANIZATION_ID, insertInactiveUser("invited@example.org"), "MANAGER");

        deleteUser(TestData.USER_ID_1, TestData.USER_EMAIL_1)
            .statusCode(Status.NO_CONTENT.getStatusCode());

        assertEquals(0, countOrganizations(TestData.ORGANIZATION_ID));
    }

    @Test
    void deleteUser_FAILED_lastAdminOfOrganizationThatIsOnlyProprietor() {
        insertProjectWithOrganization(TestData.ORGANIZATION_ID);

        deleteUser(TestData.USER_ID_1, TestData.USER_EMAIL_1)
            .statusCode(Status.CONFLICT.getStatusCode());

        assertEquals(1, countUsers(TestData.USER_ID_1));
        assertEquals(1, countOrganizations(TestData.ORGANIZATION_ID));
    }

    @Test
    void deleteUser_FAILED_lastAdminOfManagingOrganization() {
        final UUID projectId = insertProjectWithOrganization(TestData.ORGANIZATION_ID);
        insertProjectMember(projectId, TestData.USER_ID_4, "PROPRIETOR");
        setManagingOrganization(projectId, TestData.ORGANIZATION_ID);

        deleteUser(TestData.USER_ID_1, TestData.USER_EMAIL_1)
            .statusCode(Status.CONFLICT.getStatusCode());

        assertEquals(1, countUsers(TestData.USER_ID_1));
        assertEquals(1, countOrganizations(TestData.ORGANIZATION_ID));
        assertEquals(TestData.ORGANIZATION_ID, getManagingOrganizationId(projectId.toString()));
    }

    @Test
    void deleteUser_SUCCESS_organizationWithOtherActiveAdminRemainsManagingOrganization() {
        final String projectId = createOrganizationProject(TestData.USER_ID_2, TestData.USER_EMAIL_2);

        deleteUser(TestData.USER_ID_1, TestData.USER_EMAIL_1)
            .statusCode(Status.NO_CONTENT.getStatusCode());

        assertEquals(TestData.ORGANIZATION_ID_3, getManagingOrganizationId(projectId));
    }

    @Test
    void deleteOrganization_FAILED_managingOrganization() {
        final String projectId = createOrganizationProject(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        insertProjectMember(UUID.fromString(projectId), TestData.USER_ID_4, "PROPRIETOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_1, TestData.USER_EMAIL_1, Duration.ofMinutes(10)))
            .delete(ORGANIZATION_PATH, TestData.ORGANIZATION_ID_3.toString())
            .then()
            .statusCode(Status.CONFLICT.getStatusCode());

        assertEquals(1, countOrganizations(TestData.ORGANIZATION_ID_3));
        assertEquals(TestData.ORGANIZATION_ID_3, getManagingOrganizationId(projectId));
    }

    @Test
    void updateProject_FAILED_managingOrganizationIsInactive() {
        final String projectId = createProject(TestData.USER_ID_4, TestData.USER_EMAIL_4, null)
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");
        final UUID organizationId = insertInactiveOrganization();
        insertProjectOrganization(UUID.fromString(projectId), organizationId, "PROPRIETOR");

        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID_4, TestData.USER_EMAIL_4, Duration.ofMinutes(10)))
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"title\":\"" + TestData.PROJECT_TITLE + "\",\"managingOrganizationId\":\""
                + organizationId + "\"}")
            .patch(PROJECT_PATH, projectId)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
        assertNull(getManagingOrganizationId(projectId));
    }

    @Test
    void deleteUser_FAILED_inactiveOrganizationIsOnlyOtherProprietor() {
        final String projectId = createProject(TestData.USER_ID_4, TestData.USER_EMAIL_4, null)
            .statusCode(Status.CREATED.getStatusCode())
            .extract().path("id");
        insertProjectOrganization(UUID.fromString(projectId), insertInactiveOrganization(), "PROPRIETOR");

        deleteUser(TestData.USER_ID_4, TestData.USER_EMAIL_4)
            .statusCode(Status.CONFLICT.getStatusCode());
    }

}
