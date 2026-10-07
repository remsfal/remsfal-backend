package de.remsfal.service.control;

import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.organization.ClientProjectJson;
import de.remsfal.core.json.organization.ClientListJson;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.service.boundary.eventing.OrganizationEventProducer;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@QuarkusTest
public class OrganizationControllerTest extends AbstractResourceTest {

    @Inject
    OrganizationController organizationController;

    @Inject
    UserController userController;

    @InjectMock
    OrganizationEventProducer organizationEventProducer;

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

    @Test
    void getClients_SUCCESS_emptyWhenNoContractor() {
        ClientListJson result = organizationController.getClients(TestData.ORGANIZATION_ID_3, 0, 10);
        assertTrue(result.getProjects().isEmpty());
        assertEquals(0L, result.getTotal());
    }

    @Test
    void getClients_SUCCESS_projectWithBillingData() {
        final UUID projectId = UUID.fromString("dd000000-0000-0000-0000-000000000097");
        final UUID contractorId = UUID.fromString("ee000000-0000-0000-0000-000000000097");
        final UUID addressId = UUID.fromString("aa000000-0000-0000-0000-000000000097");
        insertProject(projectId, "Test Project 3");
        insertAddress(addressId, "Musterstraße 1", "Berlin", "Berlin", "10115", "DE");
        updateProjectBilling(projectId, "WEG Musterstraße", "Hausverwaltung Süd GmbH", addressId);
        insertContractor(contractorId, projectId, "Test Contractor 3", TestData.ORGANIZATION_ID_3);

        ClientListJson result = organizationController.getClients(TestData.ORGANIZATION_ID_3, 0, 10);
        assertEquals(1L, result.getTotal());
        ClientProjectJson project = result.getProjects().get(0);
        assertEquals(projectId, project.getId());
        assertEquals("Test Project 3", project.getTitle());
        assertEquals("WEG Musterstraße", project.getOwner());
        assertEquals("Hausverwaltung Süd GmbH", project.getCareOf());
        assertEquals("Musterstraße 1", project.getBillingAddress().getStreet());
        assertEquals("10115", project.getBillingAddress().getZip());
        assertEquals("Berlin", project.getBillingAddress().getCity());
    }

    @Test
    void getClients_SUCCESS_projectWithoutBillingData() {
        final UUID projectId = UUID.fromString("dd000000-0000-0000-0000-000000000098");
        insertProject(projectId, "Test Project 4");
        insertContractor(UUID.fromString("ee000000-0000-0000-0000-000000000098"), projectId, "Contractor",
            TestData.ORGANIZATION_ID_3);

        ClientProjectJson project = organizationController.getClients(TestData.ORGANIZATION_ID_3, 0, 10)
            .getProjects().get(0);
        assertNull(project.getOwner());
        assertNull(project.getCareOf());
        assertNull(project.getBillingAddress());
    }

    @Test
    void getClients_SUCCESS_paginatesProjects() {
        final UUID projectId1 = UUID.fromString("dd000000-0000-0000-0000-000000000095");
        final UUID projectId2 = UUID.fromString("dd000000-0000-0000-0000-000000000096");
        insertProject(projectId1, "A Project");
        insertProject(projectId2, "B Project");
        insertContractor(UUID.fromString("ee000000-0000-0000-0000-000000000095"), projectId1, "Contractor",
            TestData.ORGANIZATION_ID_3);
        insertContractor(UUID.fromString("ee000000-0000-0000-0000-000000000096"), projectId2, "Contractor",
            TestData.ORGANIZATION_ID_3);

        ClientListJson firstPage = organizationController.getClients(TestData.ORGANIZATION_ID_3, 0, 1);
        ClientListJson secondPage = organizationController.getClients(TestData.ORGANIZATION_ID_3, 1, 1);
        assertEquals("A Project", firstPage.getProjects().get(0).getTitle());
        assertEquals("B Project", secondPage.getProjects().get(0).getTitle());
        assertEquals(2L, secondPage.getTotal());
        assertEquals(1, secondPage.getOffset());
    }

    @Test
    void updateOrganization_SUCCESS_notifiesLinkedContractorOnRelevantChange() {
        final UserModel user = userController.getUser(TestData.USER_ID_1);
        final UUID projectId = UUID.fromString("dd000000-0000-0000-0000-000000000097");
        final UUID contractorId = UUID.fromString("ee000000-0000-0000-0000-000000000097");
        insertProject(projectId, "Test Project 3");
        insertContractor(contractorId, projectId, "Test Contractor 3", TestData.ORGANIZATION_ID);

        final String newPhone = "+491234567899";
        OrganizationJson update = ImmutableOrganizationJson.builder().phone(newPhone).build();

        organizationController.updateOrganization(user, TestData.ORGANIZATION_ID, update);

        final ArgumentCaptor<UUID> orgIdCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<OrganizationJson> orgJsonCaptor = ArgumentCaptor.forClass(OrganizationJson.class);
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<AffectedContractorJson>> contractorsCaptor = ArgumentCaptor.forClass(List.class);
        verify(organizationEventProducer).sendOrganizationUpdated(orgIdCaptor.capture(), orgJsonCaptor.capture(),
            contractorsCaptor.capture(), eq(user.getId()), eq(user.getName()));

        assertEquals(TestData.ORGANIZATION_ID, orgIdCaptor.getValue());
        assertEquals(newPhone, orgJsonCaptor.getValue().getPhone());
        assertEquals(1, contractorsCaptor.getValue().size());
        assertEquals(contractorId, contractorsCaptor.getValue().get(0).getContractorId());
        assertEquals(projectId, contractorsCaptor.getValue().get(0).getProjectId());
    }

    @Test
    void updateOrganization_SUCCESS_noEventWhenNothingChanged() {
        final UserModel user = userController.getUser(TestData.USER_ID_1);
        OrganizationJson update = ImmutableOrganizationJson.builder().build();

        organizationController.updateOrganization(user, TestData.ORGANIZATION_ID, update);

        verifyNoInteractions(organizationEventProducer);
    }
}
