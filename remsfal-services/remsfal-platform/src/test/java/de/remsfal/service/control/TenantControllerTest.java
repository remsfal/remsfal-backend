package de.remsfal.service.control;

import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.RentalAgreementRepository;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.service.entity.dto.TenantEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TenantControllerTest extends AbstractServiceTest {

    @Inject
    TenantController tenantController;

    @Inject
    RentalAgreementRepository rentalAgreementRepository;

    @BeforeEach
    void setupTestData() {
        insertProject(TestData.PROJECT_ID_1, TestData.PROJECT_TITLE_1);
        insertTestUser(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        insertTestUser(TestData.USER_ID_2, TestData.USER_EMAIL_2);
    }

    private void insertTestUser(UUID id, String email) {
        insertUser(id, UUID.randomUUID().toString(), email, "First", "Last", null);
    }

    @Test
    void createTenant_SUCCESS_NewUserAndTenancyCreatedIfNecessary() {
        final TenantJson newTenantJson = ImmutableTenantJson.builder()
                .firstName("New")
                .lastName("Tenant")
                .email("new.tenant.created@test.de")
                .build();

        final TenantModel tenantModel = tenantController.createTenant(TestData.PROJECT_ID_1, newTenantJson);

        assertNotNull(tenantModel.getId(), "Tenant ID should be generated");
        assertEquals(newTenantJson.getEmail(), tenantModel.getEmail());

        Optional<RentalAgreementEntity> tenancyOptional = rentalAgreementRepository.findRentalAgreementByProjectId(TestData.PROJECT_ID_1);
        assertTrue(tenancyOptional.isPresent(), "Tenancy must be created for the project.");

        RentalAgreementEntity tenancy = tenancyOptional.get();
        assertTrue(tenancy.getTenants().stream().anyMatch(t -> t.getId().equals(tenantModel.getId())),
                "The newly created tenant must be linked to the tenancy.");
    }

   @Test
   void createTenant_FAILED_DuplicateEmailInProject() {
       final String UNIQUE_EMAIL = "test_dupe_check@example.com";
       final TenantJson firstTenantJson = ImmutableTenantJson.builder()
               .firstName("First")
               .lastName("Tenant")
               .email(UNIQUE_EMAIL)
               .build();

       tenantController.createTenant(TestData.PROJECT_ID_1, firstTenantJson);

       final TenantJson duplicateAttemptJson = ImmutableTenantJson.builder()
               .firstName("Duplicate")
               .lastName("Attempt")
               .email(UNIQUE_EMAIL)
               .build();

       assertThrows(BadRequestException.class,
               () -> tenantController.createTenant(TestData.PROJECT_ID_1, duplicateAttemptJson),
               "Should fail because the e-mail already exists in project.");
   }

    @Test
    void getTenants_SUCCESS_ListReturned() {
        final TenantModel tenant1 = tenantController.createTenant(TestData.PROJECT_ID_1,
            ImmutableTenantJson.builder().firstName("T1").lastName("Test").email("t1@test.de").build());
        final TenantModel tenant2 = tenantController.createTenant(TestData.PROJECT_ID_1,
            ImmutableTenantJson.builder().firstName("T2").lastName("Test").email("t2@test.de").build());

        final List<TenantModel> result = tenantController.getTenants(TestData.PROJECT_ID_1);

        assertEquals(2, result.size(), "Exactly 2 tenants should be returned.");
        assertTrue(result.stream().anyMatch(t -> t.getId().equals(tenant1.getId())));
        assertTrue(result.stream().anyMatch(t -> t.getId().equals(tenant2.getId())));
    }

    @Test
    void updateTenant_FAILED_TenantNotFound() {
        final UUID NON_PROJECT_TENANT_ID = TestData.USER_ID_2;
        final UUID PROJECT_ID = TestData.PROJECT_ID_1;

        TenantJson dummyJson = ImmutableTenantJson.builder()
                .firstName("Dummy")
                .lastName("Tenant")
                .build();

        assertThrows(NotFoundException.class,
                () -> tenantController.updateTenant(PROJECT_ID, NON_PROJECT_TENANT_ID, dummyJson),
                "Should fail because the tenant is not part of the project's tenancy.");
    }

    @Test
    void deleteTenant_SUCCESS_RemovedFromTenancy() {
        final TenantModel tenant = tenantController.createTenant(TestData.PROJECT_ID_1,
            ImmutableTenantJson.builder().firstName("Delete").lastName("Me").email("t_del@test.de").build());
        final UUID tenantId = tenant.getId();

        tenantController.deleteTenant(TestData.PROJECT_ID_1, tenantId);

        assertThrows(NotFoundException.class,
                () -> tenantController.getTenant(TestData.PROJECT_ID_1, tenantId),
                "Tenant should no longer exist within the project after deletion.");

        TenantEntity tenantAfterDelete = entityManager.find(TenantEntity.class, tenantId);
        assertNull(tenantAfterDelete, "The tenant entity must be deleted due to orphanRemoval.");
    }

    @Test
    void deleteTenant_FAILED_TenantNotInProject() {

        assertThrows(NotFoundException.class,
                () -> tenantController.deleteTenant(TestData.PROJECT_ID_1, TestData.USER_ID_2),
                "Should fail because the user is not a tenant of this project.");
    }
}
