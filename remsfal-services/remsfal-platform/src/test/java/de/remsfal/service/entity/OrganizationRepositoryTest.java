package de.remsfal.service.entity;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.OrganizationRepository;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class OrganizationRepositoryTest extends AbstractServiceTest {

    @Inject
    OrganizationRepository repository;

    private static final UUID PROJECT_ID = UUID.fromString("dd000000-0000-0000-0000-000000000001");
    private static final UUID CONTRACTOR_ID_1 = UUID.fromString("ee000000-0000-0000-0000-000000000001");

    @BeforeEach
    protected void setupTestData() {
        setupTestUsers();
        setupTestOrganizations();
    }

    @Test
    void searchByName_SUCCESS_partialMatch() {
        List<OrganizationEntity> results = repository.searchByName(
            TestData.ORGANIZATION_NAME.substring(0, 4), 0, 10);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(o -> o.getId().equals(TestData.ORGANIZATION_ID)));
    }

    @Test
    void searchByName_SUCCESS_caseInsensitive() {
        List<OrganizationEntity> results = repository.searchByName(
            TestData.ORGANIZATION_NAME.toUpperCase(), 0, 10);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void searchByName_SUCCESS_noMatch() {
        List<OrganizationEntity> results = repository.searchByName(
            "xyzzy_no_match_org_9999", 0, 10);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchByName_SUCCESS_pagination() {
        List<OrganizationEntity> page0 = repository.searchByName("", 0, 2);
        List<OrganizationEntity> page1 = repository.searchByName("", 2, 2);
        assertNotNull(page0);
        assertNotNull(page1);
        assertEquals(2, page0.size());
        assertEquals(1, page1.size());
    }

    @Test
    void countByName_SUCCESS_matchesSearch() {
        long count = repository.countByName(TestData.ORGANIZATION_NAME.substring(0, 4));
        assertTrue(count > 0);
    }

    @Test
    void countByName_SUCCESS_noMatch() {
        long count = repository.countByName("xyzzy_no_match_org_9999");
        assertEquals(0, count);
    }

    @Test
    void countByName_SUCCESS_allOrganizations() {
        long count = repository.countByName("");
        assertEquals(3, count);
    }

    @Test
    void findContractorOrganizationsByUser_SUCCESS_emptyWhenNoContractors() {
        List<OrganizationEntity> result = repository.findContractorOrganizationsByUser(
            TestData.USER_ID, 0, 10);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findContractorOrganizationsByUser_SUCCESS_directProjectMember() {
        insertProject(PROJECT_ID, "Test Project");
        insertProjectMember(PROJECT_ID, TestData.USER_ID, "MANAGER");
        insertContractor(CONTRACTOR_ID_1, PROJECT_ID, "Contractor A", TestData.ORGANIZATION_ID);

        List<OrganizationEntity> result = repository.findContractorOrganizationsByUser(
            TestData.USER_ID, 0, 10);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TestData.ORGANIZATION_ID, result.get(0).getId());
    }

    @Test
    void countContractorOrganizationsByUser_SUCCESS_zero() {
        long count = repository.countContractorOrganizationsByUser(TestData.USER_ID);
        assertEquals(0, count);
    }

    @Test
    void countContractorOrganizationsByUser_SUCCESS_withContractors() {
        insertProject(PROJECT_ID, "Test Project");
        insertProjectMember(PROJECT_ID, TestData.USER_ID, "MANAGER");
        insertContractor(CONTRACTOR_ID_1, PROJECT_ID, "Contractor A", TestData.ORGANIZATION_ID);

        long count = repository.countContractorOrganizationsByUser(TestData.USER_ID);
        assertEquals(1, count);
    }

    @Test
    void findClientUsersByOrganization_SUCCESS_emptyWhenNoContractor() {
        List<UserEntity> result = repository.findClientUsersByOrganization(TestData.ORGANIZATION_ID_3, 0, 10);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, repository.countClientUsersByOrganization(TestData.ORGANIZATION_ID_3));
    }

    @Test
    void findClientUsersByOrganization_SUCCESS_projectManagerWithoutOrganization() {
        // USER_ID_4 manages the project as a single person and registered ORGANIZATION_ID_3 as contractor
        insertProject(PROJECT_ID, "Test Project");
        insertProjectMember(PROJECT_ID, TestData.USER_ID_4, "MANAGER");
        insertContractor(CONTRACTOR_ID_1, PROJECT_ID, "Contractor A", TestData.ORGANIZATION_ID_3);

        List<UserEntity> result = repository.findClientUsersByOrganization(TestData.ORGANIZATION_ID_3, 0, 10);
        assertEquals(1, result.size());
        assertEquals(TestData.USER_ID_4, result.get(0).getId());
        assertEquals(1, repository.countClientUsersByOrganization(TestData.ORGANIZATION_ID_3));
    }

    @Test
    void findClientUsersByOrganization_SUCCESS_distinctAcrossProjects() {
        final UUID projectId2 = UUID.fromString("dd000000-0000-0000-0000-000000000002");
        final UUID contractorId2 = UUID.fromString("ee000000-0000-0000-0000-000000000002");
        insertProject(PROJECT_ID, "Test Project");
        insertProject(projectId2, "Test Project 2");
        insertProjectMember(PROJECT_ID, TestData.USER_ID_4, "MANAGER");
        insertProjectMember(projectId2, TestData.USER_ID_4, "PROPRIETOR");
        insertContractor(CONTRACTOR_ID_1, PROJECT_ID, "Contractor A", TestData.ORGANIZATION_ID_3);
        insertContractor(contractorId2, projectId2, "Contractor A", TestData.ORGANIZATION_ID_3);

        List<UserEntity> result = repository.findClientUsersByOrganization(TestData.ORGANIZATION_ID_3, 0, 10);
        assertEquals(1, result.size());
        assertEquals(1, repository.countClientUsersByOrganization(TestData.ORGANIZATION_ID_3));
    }

    @Test
    void findClientUsersByOrganization_SUCCESS_excludesEmployeesOfContractorOrganization() {
        // USER_ID_3 is employee of the contractor organization ORGANIZATION_ID_3
        insertProject(PROJECT_ID, "Test Project");
        insertProjectMember(PROJECT_ID, TestData.USER_ID_3, "MANAGER");
        insertContractor(CONTRACTOR_ID_1, PROJECT_ID, "Contractor A", TestData.ORGANIZATION_ID_3);

        List<UserEntity> result = repository.findClientUsersByOrganization(TestData.ORGANIZATION_ID_3, 0, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void findClientUsersByOrganization_SUCCESS_ignoresNonClientRoles() {
        insertProject(PROJECT_ID, "Test Project");
        insertProjectMember(PROJECT_ID, TestData.USER_ID_4, "STAFF");
        insertContractor(CONTRACTOR_ID_1, PROJECT_ID, "Contractor A", TestData.ORGANIZATION_ID_3);

        List<UserEntity> result = repository.findClientUsersByOrganization(TestData.ORGANIZATION_ID_3, 0, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void findClientUsersByOrganization_SUCCESS_emptyForOtherOrganization() {
        insertProject(PROJECT_ID, "Test Project");
        insertProjectMember(PROJECT_ID, TestData.USER_ID_4, "MANAGER");
        insertContractor(CONTRACTOR_ID_1, PROJECT_ID, "Contractor A", TestData.ORGANIZATION_ID_3);

        List<UserEntity> result = repository.findClientUsersByOrganization(TestData.ORGANIZATION_ID, 0, 10);
        assertTrue(result.isEmpty());
    }
}
