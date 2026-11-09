package de.remsfal.service.control;

import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.service.entity.dto.TenantEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TenantControllerTest extends AbstractServiceTest {

    @Inject
    TenantController tenantController;

    @BeforeEach
    void setupTestData() {
        insertProject(TestData.PROJECT_ID_1, TestData.PROJECT_TITLE_1);
        insertTestUser(TestData.USER_ID_1, TestData.USER_EMAIL_1);
        insertTestUser(TestData.USER_ID_2, TestData.USER_EMAIL_2);
        insertRentalAgreement(TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1);
    }

    private void insertTestUser(UUID id, String email) {
        insertUser(id, UUID.randomUUID().toString(), email, "First", "Last", null);
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
    void getTenants_SUCCESS_returnsTenantsInProject() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, "tenant@example.org");

        final List<TenantModel> tenants = tenantController.getTenants(TestData.PROJECT_ID_1);

        assertEquals(1, tenants.size());
        assertEquals(TestData.TENANT_ID_1, tenants.get(0).getId());
    }

    @Test
    void getTenant_FAILED_tenantNotFound() {
        assertThrows(NotFoundException.class,
            () -> tenantController.getTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_1));
    }

    @Test
    void resolveOrCreateTenant_SUCCESS_withoutEmailCreatesNewTenant() {
        final TenantModel input = ImmutableTenantJson.builder()
            .firstName(TestData.TENANT_FIRST_NAME_1)
            .lastName(TestData.TENANT_LAST_NAME_1)
            .mobilePhoneNumber("+491701234567")
            .build();

        final TenantEntity tenant = tenantController.resolveOrCreateTenant(TestData.PROJECT_ID_1, input);

        assertEquals(TestData.PROJECT_ID_1, tenant.getProjectId());
        assertNotNull(tenant.getId());
        assertEquals(TestData.TENANT_FIRST_NAME_1, tenant.getFirstName());
        assertEquals("+491701234567", tenant.getMobilePhoneNumber());

        final TenantModel blankEmailInput = ImmutableTenantJson.builder()
            .firstName(TestData.TENANT_FIRST_NAME_2)
            .lastName(TestData.TENANT_LAST_NAME_2)
            .email("  ")
            .build();
        final TenantEntity tenantWithoutEmail = tenantController.resolveOrCreateTenant(
            TestData.PROJECT_ID_1, blankEmailInput);
        assertNotNull(tenantWithoutEmail.getId());
        assertNull(tenantWithoutEmail.getEmail());
    }

    @Test
    void resolveOrCreateTenant_SUCCESS_newEmailCreatesAndLinksTenant() {
        final TenantModel input = ImmutableTenantJson.builder()
            .firstName(TestData.TENANT_FIRST_NAME_2)
            .lastName(TestData.TENANT_LAST_NAME_2)
            .email("  " + TestData.USER_EMAIL_2.toUpperCase() + "  ")
            .build();

        final TenantEntity tenant = tenantController.resolveOrCreateTenant(TestData.PROJECT_ID_1, input);

        assertEquals(TestData.PROJECT_ID_1, tenant.getProjectId());
        assertEquals(TestData.USER_EMAIL_2, tenant.getEmail());
        assertEquals(TestData.USER_ID_2, tenant.getUser().getId());
    }

    @Test
    void resolveOrCreateTenant_SUCCESS_reusesSameNamedTenant() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, "tenant@example.org");
        final TenantModel input = ImmutableTenantJson.builder()
            .firstName(TestData.TENANT_FIRST_NAME_1.toLowerCase())
            .lastName(TestData.TENANT_LAST_NAME_1.toLowerCase())
            .email(" TENANT@EXAMPLE.ORG ")
            .build();

        final TenantEntity tenant = tenantController.resolveOrCreateTenant(TestData.PROJECT_ID_1, input);

        assertEquals(TestData.TENANT_ID_1, tenant.getId());
    }

    @Test
    void resolveOrCreateTenant_FAILED_emailBelongsToDifferentName() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, "tenant@example.org");
        final TenantModel input = ImmutableTenantJson.builder()
            .firstName(TestData.TENANT_FIRST_NAME_2)
            .lastName(TestData.TENANT_LAST_NAME_2)
            .email("tenant@example.org")
            .build();

        assertThrows(AlreadyExistsException.class,
            () -> tenantController.resolveOrCreateTenant(TestData.PROJECT_ID_1, input));
    }

    @Test
    void resolveOrCreateTenants_SUCCESS_resolvesEachTenant() {
        final TenantModel first = ImmutableTenantJson.builder()
            .firstName(TestData.TENANT_FIRST_NAME_1)
            .lastName(TestData.TENANT_LAST_NAME_1)
            .build();
        final TenantModel second = ImmutableTenantJson.builder()
            .firstName(TestData.TENANT_FIRST_NAME_2)
            .lastName(TestData.TENANT_LAST_NAME_2)
            .build();

        final List<TenantEntity> tenants = tenantController.resolveOrCreateTenants(
            TestData.PROJECT_ID_1, List.of(first, second));

        assertEquals(2, tenants.size());
        assertTrue(tenants.stream().allMatch(tenant -> TestData.PROJECT_ID_1.equals(tenant.getProjectId())));
    }

    @Test
    void updateTenant_SUCCESS_phoneNumbersAreUpdatedAndEmptyNumbersArePersistedAsNull() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, "tenant@example.org");

        final TenantModel updated = tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_1,
            ImmutableTenantJson.builder()
                .mobilePhoneNumber("+491701234567")
                .businessPhoneNumber("+493012345678")
                .privatePhoneNumber("+491709876543")
                .build());

        assertEquals("+491701234567", updated.getMobilePhoneNumber());
        assertEquals("+493012345678", updated.getBusinessPhoneNumber());
        assertEquals("+491709876543", updated.getPrivatePhoneNumber());

        final TenantModel cleared = tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_1,
            ImmutableTenantJson.builder()
                .mobilePhoneNumber("")
                .businessPhoneNumber("")
                .privatePhoneNumber("")
                .build());

        assertNull(cleared.getMobilePhoneNumber());
        assertNull(cleared.getBusinessPhoneNumber());
        assertNull(cleared.getPrivatePhoneNumber());

        final Object[] persistedPhones = (Object[]) entityManager
            .createNativeQuery("SELECT mobile_phone_number, business_phone_number, private_phone_number "
                + "FROM tenants WHERE id = ?")
            .setParameter(1, TestData.TENANT_ID_1)
            .getSingleResult();
        assertArrayEquals(new Object[] { null, null, null }, persistedPhones);
    }

    @Test
    void updateTenant_SUCCESS_omittedPhoneNumbersRemainUnchanged() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, "tenant@example.org");
        tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_1,
            ImmutableTenantJson.builder()
                .mobilePhoneNumber("+491701234567")
                .businessPhoneNumber("+493012345678")
                .privatePhoneNumber("+491709876543")
                .build());

        final TenantModel updated = tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_1,
            ImmutableTenantJson.builder().firstName("Updated").build());

        assertEquals("+491701234567", updated.getMobilePhoneNumber());
        assertEquals("+493012345678", updated.getBusinessPhoneNumber());
        assertEquals("+491709876543", updated.getPrivatePhoneNumber());
    }

    @Test
    void updateTenant_SUCCESS_birthAndAddressAreUpdated() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, "tenant@example.org");

        final TenantModel updated = tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_1,
            ImmutableTenantJson.builder()
                .placeOfBirth("Berlin")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address(TestData.addressBuilder().build())
                .build());

        assertEquals("Berlin", updated.getPlaceOfBirth());
        assertEquals(LocalDate.of(1990, 1, 1), updated.getDateOfBirth());
        assertEquals(TestData.ADDRESS_STREET, updated.getAddress().getStreet());
    }

    @Test
    void updateTenant_SUCCESS_emailChangeClearsStaleLink() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.USER_ID_1, TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, TestData.USER_EMAIL_1);

        TenantJson patch = ImmutableTenantJson.builder()
                .firstName(TestData.TENANT_FIRST_NAME_1)
                .lastName(TestData.TENANT_LAST_NAME_1)
                .email("unlinked@example.org")
                .build();

        TenantModel result = tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_1, patch);

        assertEquals("unlinked@example.org", result.getEmail());
        assertNull(result.getUserId(), "Stale user link must be cleared once the email no longer matches");
    }

    @Test
    void updateTenant_SUCCESS_emailChangeRelinksToNewUser() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.USER_ID_1, TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, TestData.USER_EMAIL_1);

        TenantJson patch = ImmutableTenantJson.builder()
                .firstName(TestData.TENANT_FIRST_NAME_1)
                .lastName(TestData.TENANT_LAST_NAME_1)
                .email(TestData.USER_EMAIL_2.toUpperCase())
                .build();

        TenantModel result = tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_1, patch);

        assertEquals(TestData.USER_ID_2, result.getUserId());
        assertEquals(TestData.USER_EMAIL_2, result.getEmail(), "Email must be stored lowercase regardless of PATCH casing");
    }

    @Test
    void updateTenant_FAILED_emailMatchesUserAlreadyLinkedInSameProject() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.USER_ID_1, TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, TestData.USER_EMAIL_1);
        insertTenant(TestData.TENANT_ID_2, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_2, TestData.TENANT_LAST_NAME_2, "unrelated@example.org");

        TenantJson patch = ImmutableTenantJson.builder()
                .firstName(TestData.TENANT_FIRST_NAME_2)
                .lastName(TestData.TENANT_LAST_NAME_2)
                .email(TestData.USER_EMAIL_1)
                .build();

        assertThrows(AlreadyExistsException.class,
                () -> tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_2, patch),
                "Should fail because the matching user is already linked to another tenant in this project.");
    }

    @Test
    void updateTenant_FAILED_emailAlreadyUsedByAnotherTenantInProject() {
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, "first@example.org");
        insertTenant(TestData.TENANT_ID_2, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_2, TestData.TENANT_LAST_NAME_2, "second@example.org");

        TenantJson patch = ImmutableTenantJson.builder()
                .firstName(TestData.TENANT_FIRST_NAME_2)
                .lastName(TestData.TENANT_LAST_NAME_2)
                .email("first@example.org")
                .build();

        assertThrows(AlreadyExistsException.class,
                () -> tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_2, patch),
                "Should fail because the email is already used by another tenant in this project.");
    }

    @Test
    void updateTenant_SUCCESS_mergesWithSameNamedTenantOnEmailConflict() {
        insertRentalAgreement(TestData.AGREEMENT_ID_2, TestData.PROJECT_ID_1);

        // Loser: same name as the tenant being edited, already holds the target email, linked to AGREEMENT_ID_1
        insertTenant(TestData.TENANT_ID_1, TestData.AGREEMENT_ID_1, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, "shared@example.org");
        // Survivor: the tenant being edited via PATCH, linked to AGREEMENT_ID_2
        insertTenant(TestData.TENANT_ID_2, TestData.AGREEMENT_ID_2, TestData.PROJECT_ID_1,
            TestData.TENANT_FIRST_NAME_1, TestData.TENANT_LAST_NAME_1, "own@example.org");

        TenantJson patch = ImmutableTenantJson.builder()
                .email("shared@example.org")
                .build();

        TenantModel result = tenantController.updateTenant(TestData.PROJECT_ID_1, TestData.TENANT_ID_2, patch);

        assertEquals(TestData.TENANT_ID_2, result.getId(), "The edited tenant (URL tenantId) must remain the survivor");
        assertEquals("shared@example.org", result.getEmail());
        assertNull(entityManager.find(TenantEntity.class, TestData.TENANT_ID_1),
            "The duplicate (loser) tenant must be deleted after the merge");

        RentalAgreementEntity agreement1 = entityManager.find(RentalAgreementEntity.class, TestData.AGREEMENT_ID_1);
        assertTrue(agreement1.getTenants().stream().anyMatch(t -> t.getId().equals(TestData.TENANT_ID_2)),
            "The loser's rental agreement must be relinked to the surviving tenant");
        assertTrue(agreement1.getTenants().stream().noneMatch(t -> t.getId().equals(TestData.TENANT_ID_1)));
    }
}
