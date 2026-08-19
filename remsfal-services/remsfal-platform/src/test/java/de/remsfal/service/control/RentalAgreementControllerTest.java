package de.remsfal.service.control;

import de.remsfal.core.json.project.ImmutableRentJson;
import de.remsfal.core.json.project.RentJson;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.core.json.project.RentalAgreementKeysJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementKeysJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.service.entity.dto.TenantEntity;
import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.test.TestData;

import java.time.LocalDate;
import java.util.List;
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

    /**
     * Inserts a property with no sites or buildings beneath it, i.e. a leaf rental unit.
     * {@link TestData#PROPERTY_ID_1} and {@link TestData#PROPERTY_ID_2} both have children via
     * {@code setupTestSites}/{@code setupTestBuildings} and are therefore not leaves.
     */
    private UUID insertLeafProperty() {
        final UUID propertyId = UUID.randomUUID();
        insertProperty(propertyId, TestData.PROJECT_ID_1, TestData.PROPERTY_TITLE_1,
            TestData.PROPERTY_LOCATION_1, TestData.PROPERTY_DESCRIPTION_1, TestData.PROPERTY_LAND_REGISTRY_1,
            TestData.PROPERTY_CADASTRAL_DESTRICT_1, TestData.PROPERTY_SHEET_NUMBER_1,
            TestData.PROPERTY_PLOT_NUMBER_1, TestData.PROPERTY_CADASTRAL_SECTION_1,
            TestData.PROPERTY_PLOT_1, TestData.PROPERTY_ECONOMY_TYPE_1, TestData.PROPERTY_PLOT_AREA_1);
        return propertyId;
    }

    /**
     * Inserts a building with no apartments, commercials or storages beneath it, i.e. a leaf
     * rental unit. {@link TestData#BUILDING_ID_1} has such children via {@code setupTestBuildings}
     * and is therefore not a leaf.
     */
    private UUID insertLeafBuilding() {
        final UUID buildingId = UUID.randomUUID();
        final UUID addressId = UUID.randomUUID();
        insertAddress(addressId, TestData.ADDRESS_STREET_6, TestData.ADDRESS_CITY_6,
            TestData.ADDRESS_PROVINCE_6, TestData.ADDRESS_ZIP_6, TestData.ADDRESS_COUNTRY_6);
        insertBuilding(buildingId, TestData.PROJECT_ID_1, TestData.PROPERTY_ID_1,
            TestData.BUILDING_TITLE_1, TestData.BUILDING_DESCRIPTION_1,
            null, null, null, null, null, null, addressId);
        return buildingId;
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
            .tenants(List.of())
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
            .tenants(List.of())
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
          .tenants(List.of())
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
          .tenants(List.of())
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
    void createRentalAgreement_SUCCESS_withKeys() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final RentalAgreementKeysJson key = ImmutableRentalAgreementKeysJson.builder()
          .amountOfKeys(2)
          .issuedAt(LocalDate.of(2025, 1, 1))
          .returnedAt(LocalDate.of(2025, 12, 1))
          .keyDescription("Haustürschlüssel")
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .tenants(List.of())
          .addKeys(key)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getKeys().size());
      assertEquals(2, result.getKeys().get(0).getAmountOfKeys());
      assertEquals(key.getIssuedAt(), result.getKeys().get(0).getIssuedAt());
      assertEquals(key.getReturnedAt(), result.getKeys().get(0).getReturnedAt());
      assertEquals("Haustürschlüssel", result.getKeys().get(0).getKeyDescription());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertEquals(1, entity.getKeys().size());
      assertEquals(2, entity.getKeys().get(0).getAmountOfKeys());
    }

    @Test
    void updateRentalAgreement_SUCCESS_replaceKeys() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final RentalAgreementKeysJson key1 = ImmutableRentalAgreementKeysJson.builder()
          .amountOfKeys(2)
          .keyDescription("Haustürschlüssel")
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .tenants(List.of())
          .addKeys(key1)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      assertEquals(1, created.getKeys().size());
      assertEquals("Haustürschlüssel", created.getKeys().get(0).getKeyDescription());

      // Now replace with a different key
      final RentalAgreementKeysJson key2 = ImmutableRentalAgreementKeysJson.builder()
          .amountOfKeys(1)
          .keyDescription("Briefkastenschlüssel")
          .build();

      RentalAgreementJson updateJson = ImmutableRentalAgreementJson.builder()
          .addKeys(key2)
          .build();

      RentalAgreementEntity updated = controller.updateRentalAgreement(projectId, created.getId(), updateJson);

      // Old key should be replaced by new one
      assertEquals(1, updated.getKeys().size());
      assertEquals("Briefkastenschlüssel", updated.getKeys().get(0).getKeyDescription());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getKeys().size());
      assertEquals("Briefkastenschlüssel", entity.getKeys().get(0).getKeyDescription());
    }

    @Test
    void updateRentalAgreement_SUCCESS_keysUnchangedWhenNotProvided() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final RentalAgreementKeysJson key = ImmutableRentalAgreementKeysJson.builder()
          .amountOfKeys(2)
          .keyDescription("Haustürschlüssel")
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.now())
          .tenants(List.of())
          .addKeys(key)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      // Update without touching keys
      RentalAgreementJson updateJson = ImmutableRentalAgreementJson.builder()
          .endOfRental(LocalDate.now().plusYears(1))
          .build();

      RentalAgreementEntity updated = controller.updateRentalAgreement(projectId, created.getId(), updateJson);

      assertEquals(1, updated.getKeys().size());
      assertEquals("Haustürschlüssel", updated.getKeys().get(0).getKeyDescription());
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
          .rentalUnitId(TestData.APARTMENT_ID_1)
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
      assertEquals(TestData.APARTMENT_ID_1, result.getApartmentRents().get(0).getRentalUnitId());
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
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .build();

      final UUID leafPropertyId = insertLeafProperty();
      final RentJson propertyRent = ImmutableRentJson.builder()
          .rentalUnitId(leafPropertyId)
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
      assertEquals(TestData.APARTMENT_ID_1, result.getApartmentRents().get(0).getRentalUnitId());
      assertEquals(leafPropertyId, result.getPropertyRents().get(0).getRentalUnitId());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertRentalAgreement(result, entity);
      assertEquals(1, entity.getApartmentRents().size());
      assertEquals(1, entity.getPropertyRents().size());
    }

    @Test
    void createRentalAgreement_FAILED_propertyRentNotLeaf() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      // PROPERTY_ID_1 has sites and buildings beneath it, so it is not a leaf rental unit
      final RentJson propertyRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.PROPERTY_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(5000.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addPropertyRents(propertyRent)
          .build();

      assertThrows(jakarta.ws.rs.BadRequestException.class,
          () -> controller.createRentalAgreement(projectId, agreement));
    }

    @Test
    void createRentalAgreement_FAILED_buildingRentNotLeaf() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      // BUILDING_ID_1 has apartments, commercials and storages beneath it, so it is not a leaf
      final RentJson buildingRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.BUILDING_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(3500.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addBuildingRents(buildingRent)
          .build();

      assertThrows(jakarta.ws.rs.BadRequestException.class,
          () -> controller.createRentalAgreement(projectId, agreement));
    }

    @Test
    void updateRentalAgreement_SUCCESS_rentsIgnored() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent1 = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
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
      assertEquals(TestData.APARTMENT_ID_1, created.getApartmentRents().get(0).getRentalUnitId());

      // Rents are no longer processed by updateRentalAgreement, even if provided in the request
      final RentJson apartmentRent2 = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_2)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 2, 1))
          .basicRent(1500.0f)
          .build();

      RentalAgreementJson updateJson = ImmutableRentalAgreementJson.builder()
          .addApartmentRents(apartmentRent2)
          .build();

      RentalAgreementEntity updated = controller.updateRentalAgreement(projectId, created.getId(), updateJson);

      // Original rent must remain untouched
      assertEquals(1, updated.getApartmentRents().size());
      assertEquals(TestData.APARTMENT_ID_1, updated.getApartmentRents().get(0).getRentalUnitId());
      assertEquals(1000.0f, updated.getApartmentRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getApartmentRents().size());
      assertEquals(TestData.APARTMENT_ID_1, entity.getApartmentRents().get(0).getRentalUnitId());
    }

    @Test
    void addRent_SUCCESS_firstRentForUnit() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1200.0f)
          .build();

      RentalAgreementEntity updated = controller.addRent(
          projectId, created.getId(), UnitType.APARTMENT, TestData.APARTMENT_ID_1, apartmentRent);

      assertEquals(1, updated.getApartmentRents().size());
      assertEquals(TestData.APARTMENT_ID_1, updated.getApartmentRents().get(0).getRentalUnitId());
      assertEquals(LocalDate.of(2025, 1, 1), updated.getApartmentRents().get(0).getFirstPaymentDate());
      assertNull(updated.getApartmentRents().get(0).getLastPaymentDate());
      assertEquals(RentModel.BillingCycle.MONTHLY, updated.getApartmentRents().get(0).getBillingCycle());
      assertEquals(1200.0f, updated.getApartmentRents().get(0).getBasicRent());

      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getApartmentRents().size());
    }

    @Test
    void addRent_SUCCESS_endsPreviousRentAndInheritsBillingCycle() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent1 = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .billingCycle(RentModel.BillingCycle.WEEKLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addApartmentRents(apartmentRent1)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final RentJson apartmentRent2 = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 3, 1))
          .basicRent(1500.0f)
          .build();

      RentalAgreementEntity updated = controller.addRent(
          projectId, created.getId(), UnitType.APARTMENT, TestData.APARTMENT_ID_1, apartmentRent2);

      assertEquals(2, updated.getApartmentRents().size());
      final RentModel previousRent = updated.getApartmentRents().stream()
          .filter(r -> r.getFirstPaymentDate().equals(LocalDate.of(2025, 1, 1)))
          .findFirst().orElseThrow();
      final RentModel newRent = updated.getApartmentRents().stream()
          .filter(r -> r.getFirstPaymentDate().equals(LocalDate.of(2025, 3, 1)))
          .findFirst().orElseThrow();

      assertEquals(LocalDate.of(2025, 2, 28), previousRent.getLastPaymentDate());
      assertNull(newRent.getLastPaymentDate());
      assertEquals(RentModel.BillingCycle.WEEKLY, newRent.getBillingCycle());
      assertEquals(1500.0f, newRent.getBasicRent());
    }

    @Test
    void addRent_FAILED_missingFirstPaymentDate() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .basicRent(1200.0f)
          .build();

      assertThrows(jakarta.ws.rs.BadRequestException.class, () -> controller.addRent(
          projectId, created.getId(), UnitType.APARTMENT, TestData.APARTMENT_ID_1, apartmentRent));
    }

    @Test
    void addRent_FAILED_firstPaymentDateBeforeStartOfRental() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 2, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1200.0f)
          .build();

      assertThrows(jakarta.ws.rs.BadRequestException.class, () -> controller.addRent(
          projectId, created.getId(), UnitType.APARTMENT, TestData.APARTMENT_ID_1, apartmentRent));
    }

    @Test
    void addRent_FAILED_firstPaymentDateAfterEndOfRental() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .endOfRental(LocalDate.of(2025, 6, 30))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 7, 1))
          .basicRent(1200.0f)
          .build();

      assertThrows(jakarta.ws.rs.BadRequestException.class, () -> controller.addRent(
          projectId, created.getId(), UnitType.APARTMENT, TestData.APARTMENT_ID_1, apartmentRent));
    }

    @Test
    void addRent_FAILED_lastPaymentDateAfterEndOfRental() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .endOfRental(LocalDate.of(2025, 6, 30))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .lastPaymentDate(LocalDate.of(2025, 7, 1))
          .basicRent(1200.0f)
          .build();

      assertThrows(jakarta.ws.rs.BadRequestException.class, () -> controller.addRent(
          projectId, created.getId(), UnitType.APARTMENT, TestData.APARTMENT_ID_1, apartmentRent));
    }

    @Test
    void addRent_FAILED_agreementNotFound() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final UUID agreementId = UUID.randomUUID();

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .build();

      assertThrows(NotFoundException.class, () -> controller.addRent(
          projectId, agreementId, UnitType.APARTMENT, TestData.APARTMENT_ID_1, apartmentRent));
    }

    @Test
    void deleteRents_SUCCESS_removesAllRentsForUnit() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent1 = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .build();
      final RentJson otherApartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_2)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(900.0f)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addApartmentRents(apartmentRent1)
          .addApartmentRents(otherApartmentRent)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);
      assertEquals(2, created.getApartmentRents().size());

      controller.deleteRents(projectId, created.getId(), UnitType.APARTMENT, TestData.APARTMENT_ID_1);

      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, created.getId());
      assertEquals(1, entity.getApartmentRents().size());
      assertEquals(TestData.APARTMENT_ID_2, entity.getApartmentRents().get(0).getRentalUnitId());
    }

    @Test
    void deleteRents_FAILED_agreementNotFound() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final UUID agreementId = UUID.randomUUID();

      assertThrows(NotFoundException.class, () -> controller.deleteRents(
          projectId, agreementId, UnitType.APARTMENT, TestData.APARTMENT_ID_1));
    }

    @Test
    void addRent_SUCCESS_propertyRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);
      assertNull(created.getPropertyRents());

      final UUID leafPropertyId = insertLeafProperty();
      final RentJson propertyRent = ImmutableRentJson.builder()
          .rentalUnitId(leafPropertyId)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(5000.0f)
          .heatingCostsPrepayment(400.0f)
          .build();

      RentalAgreementEntity updated = controller.addRent(
          projectId, created.getId(), UnitType.PROPERTY, leafPropertyId, propertyRent);

      assertEquals(1, updated.getPropertyRents().size());
      assertEquals(leafPropertyId, updated.getPropertyRents().get(0).getRentalUnitId());
      assertEquals(5000.0f, updated.getPropertyRents().get(0).getBasicRent());
      assertEquals(400.0f, updated.getPropertyRents().get(0).getHeatingCostsPrepayment());
      assertEquals(RentModel.BillingCycle.MONTHLY, updated.getPropertyRents().get(0).getBillingCycle());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getPropertyRents().size());
    }

    @Test
    void addRent_SUCCESS_siteRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final RentJson siteRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.SITE_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(150.0f)
          .build();

      RentalAgreementEntity updated = controller.addRent(
          projectId, created.getId(), UnitType.SITE, TestData.SITE_ID_1, siteRent);

      assertEquals(1, updated.getSiteRents().size());
      assertEquals(TestData.SITE_ID_1, updated.getSiteRents().get(0).getRentalUnitId());
      assertEquals(150.0f, updated.getSiteRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getSiteRents().size());
    }

    @Test
    void deleteRents_SUCCESS_noOpWhenPropertyRentsListIsNull() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);
      assertNull(created.getPropertyRents());

      // The unit was never rented, so the property-rents list is still null; deleting must be a no-op
      controller.deleteRents(projectId, created.getId(), UnitType.PROPERTY, TestData.PROPERTY_ID_1);

      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, created.getId());
      assertTrue(entity.getPropertyRents() == null || entity.getPropertyRents().isEmpty());
    }

    @Test
    void deleteRents_SUCCESS_removesSiteRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson siteRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.SITE_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(150.0f)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addSiteRents(siteRent)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);
      assertEquals(1, created.getSiteRents().size());

      controller.deleteRents(projectId, created.getId(), UnitType.SITE, TestData.SITE_ID_1);

      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, created.getId());
      assertEquals(0, entity.getSiteRents().size());
    }

    @Test
    void deleteRents_SUCCESS_removesBuildingRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final UUID leafBuildingId = insertLeafBuilding();
      final RentJson buildingRent = ImmutableRentJson.builder()
          .rentalUnitId(leafBuildingId)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(3500.0f)
          .build();
      controller.addRent(projectId, created.getId(), UnitType.BUILDING, leafBuildingId, buildingRent);

      controller.deleteRents(projectId, created.getId(), UnitType.BUILDING, leafBuildingId);

      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, created.getId());
      assertEquals(0, entity.getBuildingRents().size());
    }

    @Test
    void deleteRents_SUCCESS_removesStorageRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson storageRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.STORAGE_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(85.0f)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addStorageRents(storageRent)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);
      assertEquals(1, created.getStorageRents().size());

      controller.deleteRents(projectId, created.getId(), UnitType.STORAGE, TestData.STORAGE_ID_1);

      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, created.getId());
      assertEquals(0, entity.getStorageRents().size());
    }

    @Test
    void deleteRents_SUCCESS_removesCommercialRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson commercialRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.COMMERCIAL_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(2800.0f)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addCommercialRents(commercialRent)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);
      assertEquals(1, created.getCommercialRents().size());

      controller.deleteRents(projectId, created.getId(), UnitType.COMMERCIAL, TestData.COMMERCIAL_ID_1);

      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, created.getId());
      assertEquals(0, entity.getCommercialRents().size());
    }

    @Test
    void getRentalAgreementsByProject_SUCCESS_filterByUnitTypeAndId() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .build();
      final RentalAgreementJson agreementWithApartment = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addApartmentRents(apartmentRent)
          .build();
      RentalAgreementEntity withApartment = controller.createRentalAgreement(projectId, agreementWithApartment);

      final RentalAgreementJson agreementWithoutApartment = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      controller.createRentalAgreement(projectId, agreementWithoutApartment);

      List<RentalAgreementEntity> filtered = controller.getRentalAgreementsByProject(
          projectId, UnitType.APARTMENT, TestData.APARTMENT_ID_1);

      assertEquals(1, filtered.size());
      assertEquals(withApartment.getId(), filtered.get(0).getId());
    }

    @Test
    void getRentalAgreementsByProject_SUCCESS_filterByUnitTypeOnly_ignoresUnitId() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_2)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .build();
      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addApartmentRents(apartmentRent)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);

      List<RentalAgreementEntity> filtered = controller.getRentalAgreementsByProject(
          projectId, UnitType.APARTMENT, null);

      assertEquals(1, filtered.size());
      assertEquals(created.getId(), filtered.get(0).getId());
    }

    @Test
    void getRentalAgreementsByProject_SUCCESS_filterExcludesNonMatchingUnit() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .build();
      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addApartmentRents(apartmentRent)
          .build();
      controller.createRentalAgreement(projectId, agreement);

      List<RentalAgreementEntity> filtered = controller.getRentalAgreementsByProject(
          projectId, UnitType.APARTMENT, TestData.APARTMENT_ID_2);

      assertTrue(filtered.isEmpty());
    }

    @Test
    void getRentalAgreementsByProject_SUCCESS_filterByPropertyType() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();
      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final UUID leafPropertyId = insertLeafProperty();
      final RentJson propertyRent = ImmutableRentJson.builder()
          .rentalUnitId(leafPropertyId)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(5000.0f)
          .build();
      controller.addRent(projectId, created.getId(), UnitType.PROPERTY, leafPropertyId, propertyRent);

      List<RentalAgreementEntity> filtered = controller.getRentalAgreementsByProject(
          projectId, UnitType.PROPERTY, leafPropertyId);

      assertEquals(1, filtered.size());
      assertEquals(created.getId(), filtered.get(0).getId());
    }

    @Test
    void getRentalAgreementsByProject_SUCCESS_filterBySiteType() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();
      final RentJson siteRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.SITE_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(150.0f)
          .build();
      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addSiteRents(siteRent)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);

      List<RentalAgreementEntity> filtered = controller.getRentalAgreementsByProject(
          projectId, UnitType.SITE, TestData.SITE_ID_1);

      assertEquals(1, filtered.size());
      assertEquals(created.getId(), filtered.get(0).getId());
    }

    @Test
    void getRentalAgreementsByProject_SUCCESS_filterByBuildingType() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();
      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final UUID leafBuildingId = insertLeafBuilding();
      final RentJson buildingRent = ImmutableRentJson.builder()
          .rentalUnitId(leafBuildingId)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(3500.0f)
          .build();
      controller.addRent(projectId, created.getId(), UnitType.BUILDING, leafBuildingId, buildingRent);

      List<RentalAgreementEntity> filtered = controller.getRentalAgreementsByProject(
          projectId, UnitType.BUILDING, leafBuildingId);

      assertEquals(1, filtered.size());
      assertEquals(created.getId(), filtered.get(0).getId());
    }

    @Test
    void getRentalAgreementsByProject_SUCCESS_filterByStorageType() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();
      final RentJson storageRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.STORAGE_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(85.0f)
          .build();
      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addStorageRents(storageRent)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);

      List<RentalAgreementEntity> filtered = controller.getRentalAgreementsByProject(
          projectId, UnitType.STORAGE, TestData.STORAGE_ID_1);

      assertEquals(1, filtered.size());
      assertEquals(created.getId(), filtered.get(0).getId());
    }

    @Test
    void getRentalAgreementsByProject_SUCCESS_filterByCommercialType() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();
      final RentJson commercialRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.COMMERCIAL_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(2800.0f)
          .build();
      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addCommercialRents(commercialRent)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);

      List<RentalAgreementEntity> filtered = controller.getRentalAgreementsByProject(
          projectId, UnitType.COMMERCIAL, TestData.COMMERCIAL_ID_1);

      assertEquals(1, filtered.size());
      assertEquals(created.getId(), filtered.get(0).getId());
    }

    @Test
    void createRentalAgreement_SUCCESS_withBuildingRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final UUID leafBuildingId = insertLeafBuilding();
      final RentJson buildingRent = ImmutableRentJson.builder()
          .rentalUnitId(leafBuildingId)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(3500.0f)
          .operatingCostsPrepayment(450.0f)
          .heatingCostsPrepayment(300.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addBuildingRents(buildingRent)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getTenants().size());
      assertNotNull(result.getBuildingRents());
      assertEquals(1, result.getBuildingRents().size());
      assertEquals(leafBuildingId, result.getBuildingRents().get(0).getRentalUnitId());
      assertEquals(RentModel.BillingCycle.MONTHLY, result.getBuildingRents().get(0).getBillingCycle());
      assertEquals(3500.0f, result.getBuildingRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertRentalAgreement(result, entity);
      assertEquals(1, entity.getBuildingRents().size());
    }

    @Test
    void createRentalAgreement_SUCCESS_withCommercialRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_2)
          .firstName(TestData.USER_FIRST_NAME_2)
          .lastName(TestData.USER_LAST_NAME_2)
          .build();

      final RentJson commercialRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.COMMERCIAL_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 2, 1))
          .basicRent(2800.0f)
          .operatingCostsPrepayment(400.0f)
          .heatingCostsPrepayment(250.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 2, 1))
          .addTenants(tenant)
          .addCommercialRents(commercialRent)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getTenants().size());
      assertNotNull(result.getCommercialRents());
      assertEquals(1, result.getCommercialRents().size());
      assertEquals(TestData.COMMERCIAL_ID_1, result.getCommercialRents().get(0).getRentalUnitId());
      assertEquals(RentModel.BillingCycle.MONTHLY, result.getCommercialRents().get(0).getBillingCycle());
      assertEquals(2800.0f, result.getCommercialRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertRentalAgreement(result, entity);
      assertEquals(1, entity.getCommercialRents().size());
    }

    @Test
    void createRentalAgreement_SUCCESS_withSiteRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_3)
          .firstName(TestData.USER_FIRST_NAME_3)
          .lastName(TestData.USER_LAST_NAME_3)
          .build();

      final RentJson siteRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.SITE_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 3, 1))
          .basicRent(150.0f)
          .operatingCostsPrepayment(25.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 3, 1))
          .addTenants(tenant)
          .addSiteRents(siteRent)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getTenants().size());
      assertNotNull(result.getSiteRents());
      assertEquals(1, result.getSiteRents().size());
      assertEquals(TestData.SITE_ID_1, result.getSiteRents().get(0).getRentalUnitId());
      assertEquals(RentModel.BillingCycle.MONTHLY, result.getSiteRents().get(0).getBillingCycle());
      assertEquals(150.0f, result.getSiteRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertRentalAgreement(result, entity);
      assertEquals(1, entity.getSiteRents().size());
    }

    @Test
    void createRentalAgreement_SUCCESS_withStorageRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_4)
          .firstName(TestData.USER_FIRST_NAME_4)
          .lastName(TestData.USER_LAST_NAME_4)
          .build();

      final RentJson storageRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.STORAGE_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 4, 1))
          .basicRent(75.0f)
          .operatingCostsPrepayment(10.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 4, 1))
          .addTenants(tenant)
          .addStorageRents(storageRent)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getTenants().size());
      assertNotNull(result.getStorageRents());
      assertEquals(1, result.getStorageRents().size());
      assertEquals(TestData.STORAGE_ID_1, result.getStorageRents().get(0).getRentalUnitId());
      assertEquals(RentModel.BillingCycle.MONTHLY, result.getStorageRents().get(0).getBillingCycle());
      assertEquals(75.0f, result.getStorageRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertRentalAgreement(result, entity);
      assertEquals(1, entity.getStorageRents().size());
    }

    @Test
    void createRentalAgreement_SUCCESS_withAllRentTypes() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson apartmentRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.APARTMENT_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(1000.0f)
          .build();

      final UUID leafPropertyId = insertLeafProperty();
      final RentJson propertyRent = ImmutableRentJson.builder()
          .rentalUnitId(leafPropertyId)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(5000.0f)
          .build();

      final UUID leafBuildingId = insertLeafBuilding();
      final RentJson buildingRent = ImmutableRentJson.builder()
          .rentalUnitId(leafBuildingId)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(3500.0f)
          .build();

      final RentJson commercialRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.COMMERCIAL_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(2800.0f)
          .build();

      final RentJson siteRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.SITE_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(150.0f)
          .build();

      final RentJson storageRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.STORAGE_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(75.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addApartmentRents(apartmentRent)
          .addPropertyRents(propertyRent)
          .addBuildingRents(buildingRent)
          .addCommercialRents(commercialRent)
          .addSiteRents(siteRent)
          .addStorageRents(storageRent)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getApartmentRents().size());
      assertEquals(1, result.getPropertyRents().size());
      assertEquals(1, result.getBuildingRents().size());
      assertEquals(1, result.getCommercialRents().size());
      assertEquals(1, result.getSiteRents().size());
      assertEquals(1, result.getStorageRents().size());

      assertEquals(TestData.APARTMENT_ID_1, result.getApartmentRents().get(0).getRentalUnitId());
      assertEquals(leafPropertyId, result.getPropertyRents().get(0).getRentalUnitId());
      assertEquals(leafBuildingId, result.getBuildingRents().get(0).getRentalUnitId());
      assertEquals(TestData.COMMERCIAL_ID_1, result.getCommercialRents().get(0).getRentalUnitId());
      assertEquals(TestData.SITE_ID_1, result.getSiteRents().get(0).getRentalUnitId());
      assertEquals(TestData.STORAGE_ID_1, result.getStorageRents().get(0).getRentalUnitId());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertRentalAgreement(result, entity);
      assertEquals(1, entity.getApartmentRents().size());
      assertEquals(1, entity.getPropertyRents().size());
      assertEquals(1, entity.getBuildingRents().size());
      assertEquals(1, entity.getCommercialRents().size());
      assertEquals(1, entity.getSiteRents().size());
      assertEquals(1, entity.getStorageRents().size());
    }

    @Test
    void addRent_SUCCESS_buildingRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final UUID leafBuildingId = insertLeafBuilding();
      final RentJson buildingRent = ImmutableRentJson.builder()
          .rentalUnitId(leafBuildingId)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(3500.0f)
          .build();

      RentalAgreementEntity updated = controller.addRent(
          projectId, created.getId(), UnitType.BUILDING, leafBuildingId, buildingRent);

      assertEquals(1, updated.getBuildingRents().size());
      assertEquals(leafBuildingId, updated.getBuildingRents().get(0).getRentalUnitId());
      assertEquals(3500.0f, updated.getBuildingRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getBuildingRents().size());
    }

    @Test
    void addRent_FAILED_propertyRentNotLeaf() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      // PROPERTY_ID_1 has sites and buildings beneath it, so it is not a leaf rental unit
      final RentJson propertyRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.PROPERTY_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(5000.0f)
          .build();

      assertThrows(jakarta.ws.rs.BadRequestException.class, () -> controller.addRent(
          projectId, created.getId(), UnitType.PROPERTY, TestData.PROPERTY_ID_1, propertyRent));
    }

    @Test
    void addRent_FAILED_buildingRentNotLeaf() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      // BUILDING_ID_1 has apartments, commercials and storages beneath it, so it is not a leaf
      final RentJson buildingRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.BUILDING_ID_1)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(3500.0f)
          .build();

      assertThrows(jakarta.ws.rs.BadRequestException.class, () -> controller.addRent(
          projectId, created.getId(), UnitType.BUILDING, TestData.BUILDING_ID_1, buildingRent));
    }

    @Test
    void addRent_SUCCESS_commercialRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final RentJson commercialRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.COMMERCIAL_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(2800.0f)
          .operatingCostsPrepayment(350.0f)
          .build();

      RentalAgreementEntity updated = controller.addRent(
          projectId, created.getId(), UnitType.COMMERCIAL, TestData.COMMERCIAL_ID_1, commercialRent);

      assertEquals(1, updated.getCommercialRents().size());
      assertEquals(TestData.COMMERCIAL_ID_1, updated.getCommercialRents().get(0).getRentalUnitId());
      assertEquals(2800.0f, updated.getCommercialRents().get(0).getBasicRent());
      assertEquals(350.0f, updated.getCommercialRents().get(0).getOperatingCostsPrepayment());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getCommercialRents().size());
      assertEquals(TestData.COMMERCIAL_ID_1, entity.getCommercialRents().get(0).getRentalUnitId());
    }

    @Test
    void addRent_SUCCESS_storageRent() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentalAgreementJson startAgreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .build();
      RentalAgreementEntity created = controller.createRentalAgreement(projectId, startAgreement);

      final RentJson storageRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.STORAGE_ID_1)
          .billingCycle(RentModel.BillingCycle.MONTHLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 1))
          .basicRent(85.0f)
          .build();

      RentalAgreementEntity updated = controller.addRent(
          projectId, created.getId(), UnitType.STORAGE, TestData.STORAGE_ID_1, storageRent);

      assertEquals(1, updated.getStorageRents().size());
      assertEquals(TestData.STORAGE_ID_1, updated.getStorageRents().get(0).getRentalUnitId());
      assertEquals(85.0f, updated.getStorageRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, updated.getId());
      assertEquals(1, entity.getStorageRents().size());
      assertEquals(TestData.STORAGE_ID_1, entity.getStorageRents().get(0).getRentalUnitId());
    }

    @Test
    void createRentalAgreement_SUCCESS_withWeeklyBillingCycle() {
      final UUID projectId = TestData.PROJECT_ID_1;
      final TenantJson tenant = ImmutableTenantJson.builder()
          .email(TestData.USER_EMAIL_1)
          .firstName(TestData.USER_FIRST_NAME_1)
          .lastName(TestData.USER_LAST_NAME_1)
          .build();

      final RentJson siteRent = ImmutableRentJson.builder()
          .rentalUnitId(TestData.SITE_ID_1)
          .billingCycle(RentModel.BillingCycle.WEEKLY)
          .firstPaymentDate(LocalDate.of(2025, 1, 6))
          .basicRent(35.0f)
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .addTenants(tenant)
          .addSiteRents(siteRent)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);

      assertNotNull(result.getId());
      assertEquals(1, result.getSiteRents().size());
      assertEquals(RentModel.BillingCycle.WEEKLY, result.getSiteRents().get(0).getBillingCycle());
      assertEquals(35.0f, result.getSiteRents().get(0).getBasicRent());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertEquals(RentModel.BillingCycle.WEEKLY, entity.getSiteRents().get(0).getBillingCycle());
    }

    @Test
    void createRentalAgreement_SUCCESS_createMultipleDifferentTenants() {
      final UUID projectId = TestData.PROJECT_ID_1;
      
      // Create a rental agreement with two different tenants
      final TenantJson tenant1 = ImmutableTenantJson.builder()
          .firstName("Max")
          .lastName("Mustermann")
          .email("max.mustermann@example.com")
          .build();

      final TenantJson tenant2 = ImmutableTenantJson.builder()
          .firstName("Erika")
          .lastName("Mustermann")
          .email("erika.mustermann@example.com")
          .build();

      final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2024, 1, 1))
          .addTenants(tenant1)
          .addTenants(tenant2)
          .build();

      RentalAgreementEntity result = controller.createRentalAgreement(projectId, agreement);
      
      assertNotNull(result.getId());
      // Should have 2 tenants (both are different)
      assertEquals(2, result.getTenants().size());

      // Verify in DB
      RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, result.getId());
      assertEquals(2, entity.getTenants().size());
    }

    @Test
    void createRentalAgreement_SUCCESS_reuseTenantAcrossAgreements() {
      final UUID projectId = TestData.PROJECT_ID_1;
      
      // Create first rental agreement with a tenant
      final TenantJson tenant1 = ImmutableTenantJson.builder()
          .firstName("Max")
          .lastName("Schmidt")
          .email("max.schmidt@example.com")
          .dateOfBirth(LocalDate.of(1990, 5, 20))
          .build();

      final RentalAgreementJson agreement1 = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2024, 1, 1))
          .endOfRental(LocalDate.of(2024, 12, 31))
          .addTenants(tenant1)
          .build();

      RentalAgreementEntity result1 = controller.createRentalAgreement(projectId, agreement1);
      assertNotNull(result1.getId());
      assertEquals(1, result1.getTenants().size());
      UUID firstTenantId = result1.getTenants().get(0).getId();
      assertNotNull(firstTenantId);

      // Create second rental agreement with the same tenant (should reuse)
      final TenantJson tenant2 = ImmutableTenantJson.builder()
          .firstName("Max")
          .lastName("Schmidt")
          .email("max.schmidt@example.com")
          .dateOfBirth(LocalDate.of(1990, 5, 20))
          .build();

      final RentalAgreementJson agreement2 = ImmutableRentalAgreementJson.builder()
          .startOfRental(LocalDate.of(2025, 1, 1))
          .endOfRental(LocalDate.of(2025, 12, 31))
          .addTenants(tenant2)
          .build();

      RentalAgreementEntity result2 = controller.createRentalAgreement(projectId, agreement2);
      assertNotNull(result2.getId());
      assertEquals(1, result2.getTenants().size());
      UUID secondTenantId = result2.getTenants().get(0).getId();
      assertNotNull(secondTenantId);

      // Verify that the same tenant entity was reused (same ID)
      assertEquals(firstTenantId, secondTenantId, 
          "Tenant should be reused across rental agreements in the same project");

      // Verify in DB that both agreements reference the same tenant
      RentalAgreementEntity entity1 = entityManager.find(RentalAgreementEntity.class, result1.getId());
      RentalAgreementEntity entity2 = entityManager.find(RentalAgreementEntity.class, result2.getId());
      assertEquals(entity1.getTenants().get(0).getId(), entity2.getTenants().get(0).getId());
    }

    private void assertRentalAgreement(RentalAgreementEntity expected, RentalAgreementEntity actual) {
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getProjectId(), actual.getProjectId());
      assertEquals(expected.getStartOfRental(), actual.getStartOfRental());
      assertEquals(expected.getEndOfRental(), actual.getEndOfRental());
    }
    
    @Test
    void deleteRentalAgreement_SUCCESS_agreementDeleted() {
        final UUID projectId = TestData.PROJECT_ID_1;

        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .startOfRental(LocalDate.now())
            .tenants(List.of())
            .build();

        RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);

        boolean deleted = controller.deleteRentalAgreement(projectId, created.getId());

        assertTrue(deleted);

        RentalAgreementEntity entity = entityManager.find( RentalAgreementEntity.class, created.getId() );
        assertNull(entity);
    }

    @Test
    void addTenant_SUCCESS_tenantAdded() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .startOfRental(LocalDate.now())
            .tenants(List.of())
            .build();
        RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);

        final TenantJson tenant = ImmutableTenantJson.builder()
            .firstName(TestData.USER_FIRST_NAME_1)
            .lastName(TestData.USER_LAST_NAME_1)
            .email(TestData.USER_EMAIL_1)
            .build();

        TenantEntity addedTenant =
            controller.addTenant(projectId, created.getId(), tenant);

        assertNotNull(addedTenant.getId());
        assertEquals(TestData.USER_EMAIL_1, addedTenant.getEmail());

        RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, created.getId());
        assertEquals(1, entity.getTenants().size());
        assertEquals(addedTenant.getId(), entity.getTenants().get(0).getId());
    }

    @Test
    void addTenant_SUCCESS_reusesExistingTenantAcrossAgreements() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final TenantJson tenant = ImmutableTenantJson.builder()
            .firstName("Max")
            .lastName("Schmidt")
            .email("max.schmidt@example.com")
            .build();

        final RentalAgreementJson agreement1 = ImmutableRentalAgreementJson.builder()
            .startOfRental(LocalDate.now())
            .addTenants(tenant)
            .build();
        RentalAgreementEntity created1 = controller.createRentalAgreement(projectId, agreement1);
        final UUID firstTenantId = created1.getTenants().get(0).getId();

        final RentalAgreementJson agreement2 = ImmutableRentalAgreementJson.builder()
            .startOfRental(LocalDate.now())
            .tenants(List.of())
            .build();
        RentalAgreementEntity created2 = controller.createRentalAgreement(projectId, agreement2);

        TenantEntity addedTenant =
            controller.addTenant(projectId, created2.getId(), tenant);

        assertEquals(firstTenantId, addedTenant.getId());
    }

    @Test
    void addTenant_FAILED_agreementNotFound() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final UUID agreementId = UUID.randomUUID();
        final TenantJson tenant = ImmutableTenantJson.builder()
            .firstName("Max")
            .lastName("Mustermann")
            .build();

        assertThrows(NotFoundException.class,
            () -> controller.addTenant(projectId, agreementId, tenant));
    }

    @Test
    void removeTenant_SUCCESS_tenantRemovedFromAgreement() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final TenantJson tenant = ImmutableTenantJson.builder()
            .firstName(TestData.USER_FIRST_NAME_1)
            .lastName(TestData.USER_LAST_NAME_1)
            .email(TestData.USER_EMAIL_1)
            .build();
        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .startOfRental(LocalDate.now())
            .addTenants(tenant)
            .build();
        RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);
        final UUID tenantId = created.getTenants().get(0).getId();

        controller.removeTenant(projectId, created.getId(), tenantId);

        RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, created.getId());
        assertEquals(0, entity.getTenants().size());

        // The tenant itself is not deleted, only detached from this agreement
        TenantEntity tenantEntity =
            entityManager.find(TenantEntity.class, tenantId);
        assertNotNull(tenantEntity);
    }

    @Test
    void removeTenant_SUCCESS_noOpWhenTenantNotInAgreement() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final RentalAgreementJson agreement = ImmutableRentalAgreementJson.builder()
            .startOfRental(LocalDate.now())
            .tenants(List.of())
            .build();
        RentalAgreementEntity created = controller.createRentalAgreement(projectId, agreement);

        controller.removeTenant(projectId, created.getId(), UUID.randomUUID());

        RentalAgreementEntity entity = entityManager.find(RentalAgreementEntity.class, created.getId());
        assertEquals(0, entity.getTenants().size());
    }

    @Test
    void removeTenant_FAILED_agreementNotFound() {
        final UUID projectId = TestData.PROJECT_ID_1;
        final UUID agreementId = UUID.randomUUID();
        final UUID tenantId = UUID.randomUUID();

        assertThrows(NotFoundException.class,
            () -> controller.removeTenant(projectId, agreementId, tenantId));
    }
}
