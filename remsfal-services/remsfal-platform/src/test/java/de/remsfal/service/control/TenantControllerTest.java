package de.remsfal.service.control;

import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
