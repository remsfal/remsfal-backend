package de.remsfal.service.control;

import de.remsfal.core.json.OrganizationJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.AbstractResourceTest;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class OrganizationControllerTest extends AbstractResourceTest {

    @Inject
    OrganizationController organizationController;

    @Inject
    UserController userController;

    @BeforeEach
    protected void setupTestData() {
        super.setupTestUsers();
        super.setupTestOrganizations();
    }

    @AfterEach
    @Transactional
    protected void cleanupTestData() {
        entityManager.createNativeQuery("DELETE FROM organization").executeUpdate();
    }

    @Test
    void updateOrganization_SUCCESS_NothingChangedIfNull() {
        final UserModel user = userController.getUser(TestData.USER_ID_1);

        OrganizationEntity entity = new OrganizationEntity();

        entity.setName(null);
        entity.setEmail(null);
        entity.setPhone(null);
        entity.setTrade(null);
        entity.setAddress(null);

        OrganizationJson json = OrganizationJson.valueOf(entity);

        OrganizationEntity updatedEntity = organizationController.updateOrganization(user, TestData.ORGANIZATION_ID, json);

        assertEquals(TestData.ORGANIZATION_NAME, updatedEntity.getName());
        assertEquals(TestData.ORGANIZATION_EMAIL, updatedEntity.getEmail());
        assertEquals(TestData.ORGANIZATION_PHONE, updatedEntity.getPhone());
        assertEquals(TestData.ORGANIZATION_TRADE, updatedEntity.getTrade());
        assertEquals(TestData.ADDRESS_ID, updatedEntity.getAddress().getId());
    }
}
