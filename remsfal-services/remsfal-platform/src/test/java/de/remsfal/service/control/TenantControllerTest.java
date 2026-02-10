package de.remsfal.service.control;

import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.service.AbstractServiceTest;
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
}
