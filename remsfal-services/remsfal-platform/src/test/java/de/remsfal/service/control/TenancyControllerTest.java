package de.remsfal.service.control;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.tenancy.ImmutableTenancyInfoJson;
import de.remsfal.core.json.tenancy.TenancyInfoJson;
import de.remsfal.service.entity.dto.TenancyEntity;
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
class TenancyControllerTest extends AbstractServiceTest {

  @Inject
  TenancyController controller;

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
    void createTenancy_FAILED_noProject() {
        final TenancyInfoJson tenancy = ImmutableTenancyInfoJson.builder()
            .startOfRental(LocalDate.now())
            .build();
        final UUID projectId = UUID.randomUUID();

        assertThrows(NotFoundException.class,
            () -> controller.createTenancy(projectId, tenancy));
    }

    @Test
    void createTenancy_Success_idGenerated() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final TenancyInfoJson tenancy = ImmutableTenancyInfoJson.builder()
            .startOfRental(LocalDate.now())
            .build();

        TenancyEntity result = controller.createTenancy(projectId, tenancy);

        assertNotNull(result.getId());
        assertEquals(projectId, result.getProjectId());
        assertEquals(tenancy.getStartOfRental(), result.getStartOfRental());

        TenancyEntity entity = entityManager.find(TenancyEntity.class, result.getId());
        assertTenancy(result, entity);
    }

    @Test
    void getTenancy_SUCCESS_tenancyRetrieved() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final TenancyInfoJson tenancy = ImmutableTenancyInfoJson.builder()
            .startOfRental(LocalDate.now())
            .build();
        TenancyEntity created = controller.createTenancy(projectId, tenancy);

        TenancyEntity retrieved = controller.getTenancyByProject(projectId, created.getId());

        assertEquals(created.getId(), retrieved.getId());
        assertTenancy(created, retrieved);
    }

    @Test
    void getTenancy_FAILED_tenancyNotFound() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final UUID tenancyId = UUID.randomUUID();

      assertThrows(NotFoundException.class,
          () -> controller.getTenancyByProject(projectId, tenancyId));
    }

    @Test
    void updateTenancy_SUCCESS_correctlyUpdated() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenancyInfoJson tenancy = ImmutableTenancyInfoJson.builder()
          .startOfRental(LocalDate.now())
          .build();
      TenancyEntity created = controller.createTenancy(projectId, tenancy);

      TenancyInfoJson updateJson = ImmutableTenancyInfoJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .endOfRental(LocalDate.of(2026, 1, 1))
          .build();

      TenancyEntity updated = controller.updateTenancy(projectId, created.getId(), updateJson);

      assertEquals(created.getId(), updated.getId());
      assertEquals(tenancy.getStartOfRental(), created.getStartOfRental()); // Helper check on original
      assertEquals(updateJson.getStartOfRental(), updated.getStartOfRental());
      assertEquals(updateJson.getEndOfRental(), updated.getEndOfRental());

      TenancyEntity entity = entityManager.find(TenancyEntity.class, updated.getId());
      assertTenancy(updated, entity);
    }

    @Test
    void createTenancy_SUCCESS_withTenants() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final UserJson tenantUser = ImmutableUserJson.builder()
          .id(TestData.USER_ID_1)
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final TenancyInfoJson tenancy = ImmutableTenancyInfoJson.builder()
          .startOfRental(LocalDate.now())
          .addTenants(tenantUser)
          .build();

      TenancyEntity result = controller.createTenancy(projectId, tenancy);

      assertNotNull(result.getId());
      assertEquals(1, result.getTenants().size());
      assertEquals(TestData.USER_ID_1, result.getTenants().get(0).getId());

      // Verify in DB
      TenancyEntity entity = entityManager.find(TenancyEntity.class, result.getId());
      assertTenancy(result, entity);
    }

    @Test
    void updateTenancy_SUCCESS_tenantsUpdated() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenancyInfoJson startTenancy = ImmutableTenancyInfoJson.builder()
          .startOfRental(LocalDate.now())
          .build();
      TenancyEntity created = controller.createTenancy(projectId, startTenancy);

      final UserJson tenantUser = ImmutableUserJson.builder()
          .id(TestData.USER_ID_1)
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();
      TenancyInfoJson updateJson = ImmutableTenancyInfoJson.builder()
          .startOfRental(LocalDate.now())
          .addTenants(tenantUser)
          .build();

      TenancyEntity updated = controller.updateTenancy(projectId, created.getId(), updateJson);

      assertEquals(1, updated.getTenants().size());
      assertEquals(TestData.USER_ID_1, updated.getTenants().get(0).getId());

      TenancyEntity entity = entityManager.find(TenancyEntity.class, updated.getId());
      assertTenancy(updated, entity);
    }

    @Test
    void updateTenancy_FAILED_tenancyNotFound() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenancyInfoJson updateJson = ImmutableTenancyInfoJson.builder()
          .startOfRental(LocalDate.now())
          .build();
      final UUID tenancyId = UUID.randomUUID();

      assertThrows(NotFoundException.class,
          () -> controller.updateTenancy(projectId, tenancyId, updateJson));
    }

    private void assertTenancy(TenancyEntity expected, TenancyEntity actual) {
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getProjectId(), actual.getProjectId());
      assertEquals(expected.getStartOfRental(), actual.getStartOfRental());
      assertEquals(expected.getEndOfRental(), actual.getEndOfRental());
    }
}
