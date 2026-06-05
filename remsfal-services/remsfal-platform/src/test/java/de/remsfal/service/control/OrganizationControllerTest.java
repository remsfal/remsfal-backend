package de.remsfal.service.control;

import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class OrganizationControllerTest extends AbstractResourceTest {

    @Inject
    OrganizationController organizationController;

    @Inject
    UserController userController;

    @BeforeEach
    protected void setupTestData() {
        super.setupTestUsers();
        super.setupTestOrganizations();
    }

    @Test
    void updateOrganization_SUCCESS_NothingChangedIfNull() {
        final UserModel user = userController.getUser(TestData.USER_ID_1);

        OrganizationEntity entity = new OrganizationEntity();

        entity.setName(null);
        entity.setEmail(null);
        entity.setPhone(null);
        entity.setTrade(null);
        entity.setVatIdentificationNumber(null);
        entity.setAddress(null);

        OrganizationJson json = OrganizationJson.valueOf(entity);

        OrganizationEntity updatedEntity = organizationController.updateOrganization(user, TestData.ORGANIZATION_ID, json);

        assertEquals(TestData.ORGANIZATION_NAME, updatedEntity.getName());
        assertEquals(TestData.ORGANIZATION_EMAIL, updatedEntity.getEmail());
        assertEquals(TestData.ORGANIZATION_PHONE, updatedEntity.getPhone());
        assertEquals(TestData.ORGANIZATION_TRADE, updatedEntity.getTrade());
        assertNull(updatedEntity.getVatIdentificationNumber());
        assertEquals(TestData.ADDRESS_ID, updatedEntity.getAddress().getId());
    }

    @Test
    void searchOrganizations_SUCCESS_partialMatch() {
        List<OrganizationEntity> results = organizationController.searchOrganizations(
            TestData.ORGANIZATION_NAME.substring(0, 4), 0, 10);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(o -> o.getId().equals(TestData.ORGANIZATION_ID)));
    }

    @Test
    void searchOrganizations_SUCCESS_noMatch() {
        List<OrganizationEntity> results = organizationController.searchOrganizations(
            "xyzzy_no_match_9999", 0, 10);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void countSearchOrganizations_SUCCESS_matchesResults() {
        long count = organizationController.countSearchOrganizations(
            TestData.ORGANIZATION_NAME.substring(0, 4));
        assertTrue(count > 0);
    }

    @Test
    void countSearchOrganizations_SUCCESS_noMatch() {
        long count = organizationController.countSearchOrganizations("xyzzy_no_match_9999");
        assertEquals(0, count);
    }

    @Test
    void getContractorOrganizations_SUCCESS_emptyWhenNoContractors() {
        final UserModel user = userController.getUser(TestData.USER_ID_1);
        List<OrganizationEntity> results = organizationController.getContractorOrganizations(user, 0, 10);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void countContractorOrganizations_SUCCESS_zeroWhenNoContractors() {
        final UserModel user = userController.getUser(TestData.USER_ID_1);
        long count = organizationController.countContractorOrganizations(user);
        assertEquals(0, count);
    }

    @Test
    void getContractorOrganizations_SUCCESS_directProjectMember() {
        final UserModel user = userController.getUser(TestData.USER_ID_1);
        final UUID projectId = UUID.fromString("dd000000-0000-0000-0000-000000000099");
        final UUID contractorId = UUID.fromString("ee000000-0000-0000-0000-000000000099");
        insertProject(projectId, "Test Project");
        insertProjectMember(projectId, TestData.USER_ID_1, "MANAGER");
        insertContractor(contractorId, projectId, "Test Contractor", TestData.ORGANIZATION_ID);

        List<OrganizationEntity> results = organizationController.getContractorOrganizations(user, 0, 10);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TestData.ORGANIZATION_ID, results.get(0).getId());
    }

    @Test
    void countContractorOrganizations_SUCCESS_withContractors() {
        final UserModel user = userController.getUser(TestData.USER_ID_1);
        final UUID projectId = UUID.fromString("dd000000-0000-0000-0000-000000000098");
        final UUID contractorId = UUID.fromString("ee000000-0000-0000-0000-000000000098");
        insertProject(projectId, "Test Project 2");
        insertProjectMember(projectId, TestData.USER_ID_1, "MANAGER");
        insertContractor(contractorId, projectId, "Test Contractor 2", TestData.ORGANIZATION_ID);

        long count = organizationController.countContractorOrganizations(user);
        assertEquals(1, count);
    }
}
