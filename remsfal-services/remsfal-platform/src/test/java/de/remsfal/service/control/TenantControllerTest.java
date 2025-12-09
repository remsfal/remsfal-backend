package de.remsfal.service.control;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.TenancyRepository;
import de.remsfal.service.entity.dto.TenancyEntity;
import de.remsfal.service.entity.dto.UserEntity;
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
    TenancyRepository tenancyRepository;

    // --- Testdaten ---
    private static final UUID TEST_TENANCY_ID = UUID.randomUUID();

    @BeforeEach
    void setupTestData() {
        // 1. Projekt erstellen (Kontext für die Tenancy)
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID_1)
                .setParameter(2, TestData.PROJECT_TITLE_1)
                .executeUpdate());

        insertTestUser(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        insertTestUser(TestData.USER_ID_2, TestData.USER_EMAIL_2);
    }

    private void insertTestUser(UUID id, String email) {
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO users (id, token_id, email, first_name, last_name, address_id) VALUES (?,?,?,?,?,?)")
                .setParameter(1, id)
                .setParameter(2, UUID.randomUUID().toString())
                .setParameter(3, email)
                .setParameter(4, "First")
                .setParameter(5, "Last")
                .setParameter(6, null)
                .executeUpdate());
    }

    @Test
    void createTenant_SUCCESS_NewUserAndTenancyCreatedIfNecessary() {
        final UserJson newTenantJson = TestData.userBuilder()
                .email("new.tenant.created@test.de")
                .build();

        final CustomerModel tenantModel = tenantController.createTenant(TestData.PROJECT_ID_1, newTenantJson);

        assertNotNull(tenantModel.getId(), "Tenant ID should be generated");
        assertEquals(newTenantJson.getEmail(), tenantModel.getEmail());

        Optional<TenancyEntity> tenancyOptional = tenancyRepository.findTenancyByProjectId(TestData.PROJECT_ID_1);
        assertTrue(tenancyOptional.isPresent(), "Tenancy must be created for the project.");

        TenancyEntity tenancy = tenancyOptional.get();
        assertTrue(tenancy.getTenants().stream().anyMatch(t -> t.getId().equals(tenantModel.getId())),
                "The newly created tenant must be linked to the tenancy.");
    }

   @Test
   void createTenant_FAILED_DuplicateEmailInProject() {
       final String UNIQUE_EMAIL = "test_dupe_check@example.com";
       final UserJson firstTenantJson = TestData.userBuilder()
               .email(UNIQUE_EMAIL)
               .build();

       tenantController.createTenant(TestData.PROJECT_ID_1, firstTenantJson);

       final UserJson duplicateAttemptJson = TestData.userBuilder()
               .email(UNIQUE_EMAIL)
               .build();

       assertThrows(BadRequestException.class,
               () -> tenantController.createTenant(TestData.PROJECT_ID_1, duplicateAttemptJson),
               "Sollte fehlschlagen, da die E-Mail bereits im Projekt existiert.");
   }


    @Test
    void getTenants_SUCCESS_ListReturned() {
        final CustomerModel tenant1 = tenantController.createTenant(TestData.PROJECT_ID_1, TestData.userBuilder().email("t1@test.de").build());
        final CustomerModel tenant2 = tenantController.createTenant(TestData.PROJECT_ID_1, TestData.userBuilder().email("t2@test.de").build());

        final List<CustomerModel> result = tenantController.getTenants(TestData.PROJECT_ID_1);

        assertFalse(result.isEmpty(), "Die Liste sollte nicht leer sein.");
        assertEquals(2, result.size(), "Es sollten genau 2 Mieter zurückgegeben werden.");
        assertTrue(result.stream().anyMatch(t -> t.getId().equals(tenant1.getId())));
        assertTrue(result.stream().anyMatch(t -> t.getId().equals(tenant2.getId())));
    }

    @Test
    void deleteTenant_SUCCESS_RemovedFromTenancy() {
        final CustomerModel tenant = tenantController.createTenant(TestData.PROJECT_ID_1, TestData.userBuilder().email("t_del@test.de").build());
        final UUID tenantId = tenant.getId();

        tenantController.deleteTenant(TestData.PROJECT_ID_1, tenantId);

        assertThrows(NotFoundException.class,
                () -> tenantController.getTenant(TestData.PROJECT_ID_1, tenantId),
                "Mieter sollte nach dem Löschen nicht mehr im Projekt existieren.");

        UserEntity userAfterDelete = entityManager.find(UserEntity.class, tenantId);
        assertNotNull(userAfterDelete, "Der Benutzer selbst darf nicht gelöscht werden.");
    }

    @Test
    void deleteTenant_FAILED_TenantNotInProject() {

        assertThrows(NotFoundException.class,
                () -> tenantController.deleteTenant(TestData.PROJECT_ID_1, TestData.USER_ID_2),
                "Sollte fehlschlagen, da der Benutzer kein Mieter dieses Projekts ist.");
    }
}
