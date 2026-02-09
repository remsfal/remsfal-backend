package de.remsfal.service.control;

import de.remsfal.core.json.project.ImmutableRentJson;
import de.remsfal.core.json.project.RentJson;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.core.model.project.RentModel;
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
    void setup() {
        setupTestUsers();
        setupTestProjects();
        setupTestProperties();
        setupTestSites();
        setupTestBuildings();
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
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .addTenants(tenant)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getTenants().size());
      assertEquals(TestData.USER_EMAIL_1, result.getTenants().get(0).getEmail());

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

      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();
      RentalAgreementJson updateJson = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .addTenants(tenant)
          .build();

      RentalAgreementEntity updated = controller.updateRentalAgreement(projectId, created.getId(), updateJson);

      assertEquals(1, updated.getTenants().size());
      assertEquals(TestData.USER_EMAIL_1, updated.getTenants().get(0).getEmail());

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

    @Test
    void createRentalAgreement_SUCCESS_withApartmentRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .unitId(TestData.APARTMENT_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .operatingCostsPrepayment(200.0f)
          .heatingCostsPrepayment(150.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addApartmentRents(apartmentRent)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getTenants().size());
      assertNotNull(result.getApartmentRents());
      assertEquals(1, result.getApartmentRents().size());
      assertEquals(TestData.APARTMENT_ID_1, result.getApartmentRents().get(0).getUnitId());
      assertEquals(RentModel.BillingCycle.MONTHLY, result.getApartmentRents().get(0).getBillingCycle());
      assertEquals(1000.0f, result.getApartmentRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertRentalAgreement(result, entity);
      assertEquals(1, entity.getApartmentRents().size());
    }

    @Test
    void createRentalAgreement_SUCCESS_withMultipleRentTypes() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .unitId(TestData.APARTMENT_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .build();

      final RentJson propertyRent = ImmutableRentJson.builder()
          .unitId(TestData.PROPERTY_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(5000.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addApartmentRents(apartmentRent)
          .addPropertyRents(propertyRent)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getApartmentRents().size());
      assertEquals(1, result.getPropertyRents().size());
      assertEquals(TestData.APARTMENT_ID_1, result.getApartmentRents().get(0).getUnitId());
      assertEquals(TestData.PROPERTY_ID_1, result.getPropertyRents().get(0).getUnitId());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertRentalAgreement(result, entity);
      assertEquals(1, entity.getApartmentRents().size());
      assertEquals(1, entity.getPropertyRents().size());
    }

    @Test
    void updateRentalAgreement_SUCCESS_addRents() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      // Now add rents via update
      final RentJson apartmentRent = ImmutableRentJson.builder()
          .unitId(TestData.APARTMENT_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1200.0f)
          .build();

      RentalAgreementJson updateJson = ImmutableRentalAgreementJson.builder()
          .addApartmentRents(apartmentRent)
          .build();

      RentalAgreementEntity updated = controller.updateRentalAgreement(projectId, created.getId(), updateJson);

      assertEquals(1, updated.getApartmentRents().size());
      assertEquals(TestData.APARTMENT_ID_1, updated.getApartmentRents().get(0).getUnitId());
      assertEquals(1200.0f, updated.getApartmentRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getApartmentRents().size());
    }

    @Test
    void updateRentalAgreement_SUCCESS_replaceRents() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent1 = ImmutableRentJson.builder()
          .unitId(TestData.APARTMENT_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .addTenants(tenant)
          .addApartmentRents(apartmentRent1)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      assertEquals(1, created.getApartmentRents().size());
      assertEquals(1000.0f, created.getApartmentRents().get(0).getBasicRent());

      // Now replace with a different rent
      final RentJson apartmentRent2 = ImmutableRentJson.builder()
          .unitId(TestData.APARTMENT_ID_2)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 2, 1))
          .basicRent(1500.0f)
          .build();

      RentalAgreementJson updateJson = ImmutableRentalAgreementJson.builder()
          .addApartmentRents(apartmentRent2)
          .build();

      RentalAgreementEntity updated = controller.updateRentalAgreement(projectId, created.getId(), updateJson);

      // Old rent should be replaced by new one
      assertEquals(1, updated.getApartmentRents().size());
      assertEquals(TestData.APARTMENT_ID_2, updated.getApartmentRents().get(0).getUnitId());
      assertEquals(1500.0f, updated.getApartmentRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getApartmentRents().size());
      assertEquals(TestData.APARTMENT_ID_2, entity.getApartmentRents().get(0).getUnitId());
    }

    private void assertRentalAgreement(RentalAgreementEntity expected, RentalAgreementEntity actual) {
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getProjectId(), actual.getProjectId());
      assertEquals(expected.getStartOfRental(), actual.getStartOfRental());
      assertEquals(expected.getEndOfRental(), actual.getEndOfRental());
    }
}
