package de.remsfal.service.control;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.test.TestData;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RentalAgreementControllerTest extends AbstractServiceTest {

  @Inject
  RentalAgreementController controller;

    @BeforeEach
    void setupTestProjects() {
        // Setup users for tenant tests
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO users (id, first_name, last_name, email) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_1)
            .setParameter(2, TestData.USER_FIRST_NAME_1)
            .setParameter(3, TestData.USER_LAST_NAME_1)
            .setParameter(4, TestData.USER_EMAIL_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO users (id, first_name, last_name, email) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_2)
            .setParameter(2, TestData.USER_FIRST_NAME_2)
            .setParameter(3, TestData.USER_LAST_NAME_2)
            .setParameter(4, TestData.USER_EMAIL_2)
            .executeUpdate());

        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_1)
            .setParameter(2, TestData.PROJECT_TITLE_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_2)
            .setParameter(2, TestData.PROJECT_TITLE_2)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_3)
            .setParameter(2, TestData.PROJECT_TITLE_3)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_4)
            .setParameter(2, TestData.PROJECT_TITLE_4)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
            .setParameter(1, TestData.PROJECT_ID_5)
            .setParameter(2, TestData.PROJECT_TITLE_5)
            .executeUpdate());
  }

    @Test
    void createRentalAgreement_FAILED_noProject() {
        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .startOfRental(LocalDate.now())
            .build();
        final UUID projectId = UUID.randomUUID();

        assertThrows(NotFoundException.class,
            () -> controller.createRentalAgreement(projectId, agreement));
    }

    @Test
    void createRentalAgreement_Success_idGenerated() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .startOfRental(LocalDate.now())
            .build();

        RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

        assertNotNull(result.getId());
        assertEquals(projectId, result.getProjectId());
        assertEquals(agreement.getStartOfRental(), result.getStartOfRental());

        RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
        assertRentalAgreement(result, entity);
    }

    @Test
    void getRentalAgreement_SUCCESS_agreementRetrieved() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .startOfRental(LocalDate.now())
            .build();
        RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);

        RentalAgreementEntity retrieved = controller.getRentalAgreementByProject(projectId, created.getId());

        assertEquals(created.getId(), retrieved.getId());
        assertRentalAgreement(created, retrieved);
    }

    @Test
    void getRentalAgreement_FAILED_agreementNotFound() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final UUID agreementId = UUID.randomUUID();

      assertThrows(NotFoundException.class,
          () -> controller.getRentalAgreementByProject(projectId, agreementId));
    }

    @Test
    void updateRentalAgreement_SUCCESS_correctlyUpdated() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);

      RentalAgreementJson updateJson = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .endOfRental(LocalDate.of(2026, 1, 1))
          .build();

      RentalAgreementEntity updated = controller.updateRentalAgreement(projectId, created.getId(), updateJson);

      assertEquals(created.getId(), updated.getId());
      assertEquals(agreement.getStartOfRental(), created.getStartOfRental()); // Helper check on original
      assertEquals(updateJson.getStartOfRental(), updated.getStartOfRental());
      assertEquals(updateJson.getEndOfRental(), updated.getEndOfRental());

      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertRentalAgreement(updated, entity);
    }

    @Test
    void createRentalAgreement_SUCCESS_withTenants() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final UserJson tenantUser = ImmutableUserJson.builder()
          .id(TestData.USER_ID_1)
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .addTenants(tenantUser)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getTenants().size());
      assertEquals(TestData.USER_ID_1, result.getTenants().get(0).getId());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertRentalAgreement(result, entity);
    }

    @Test
    void updateRentalAgreement_SUCCESS_tenantsUpdated() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final UserJson tenantUser = ImmutableUserJson.builder()
          .id(TestData.USER_ID_1)
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();
      RentalAgreementJson updateJson = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .addTenants(tenantUser)
          .build();

      RentalAgreementEntity updated = controller.updateRentalAgreement(projectId, created.getId(), updateJson);

      assertEquals(1, updated.getTenants().size());
      assertEquals(TestData.USER_ID_1, updated.getTenants().get(0).getId());

      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertRentalAgreement(updated, entity);
    }

    @Test
    void updateRentalAgreement_FAILED_agreementNotFound() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final RentalAgreementJson updateJson = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .build();
      final UUID agreementId = UUID.randomUUID();

      assertThrows(NotFoundException.class,
          () -> controller.updateRentalAgreement(projectId, agreementId, updateJson));
    }

    private void assertRentalAgreement(RentalAgreementEntity expected, RentalAgreementEntity actual) {
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getProjectId(), actual.getProjectId());
      assertEquals(expected.getStartOfRental(), actual.getStartOfRental());
      assertEquals(expected.getEndOfRental(), actual.getEndOfRental());
    }
}
