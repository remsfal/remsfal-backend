package de.remsfal.service.entity;

import de.remsfal.core.model.OrganizationEmployeeModel;
import de.remsfal.service.entity.dto.OrganizationEmployeeEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class OrganizationEmployeeEntityTest {

    private UserEntity user1;
    private UserEntity user2;
    private OrganizationEntity organization1;
    private OrganizationEntity organization2;
    private OrganizationEmployeeEntity employee1;
    private OrganizationEmployeeEntity employee2;

    @BeforeEach
    public void setUp() {
        user1 = new UserEntity();
        user1.setId(TestData.USER_ID_1);

        user2 = new UserEntity();
        user2.setId(TestData.USER_ID_2);

        organization1 = new OrganizationEntity();
        organization1.setId(UUID.randomUUID());
        organization1.setName(TestData.ORGANIZATION_NAME);
        organization1.setEmail(TestData.ORGANIZATION_EMAIL);
        organization1.setPhone(TestData.ORGANIZATION_PHONE);
        organization1.setTrade(TestData.ORGANIZATION_TRADE);

        organization2 = new OrganizationEntity();
        organization2.setId(UUID.randomUUID());
        organization2.setName(TestData.ORGANIZATION_NAME);
        organization2.setEmail(TestData.ORGANIZATION_EMAIL);
        organization2.setPhone(TestData.ORGANIZATION_PHONE);
        organization2.setTrade(TestData.ORGANIZATION_TRADE);

        employee1 = new OrganizationEmployeeEntity();
        employee1.setUser(user1);
        employee1.setOrganization(organization1);
        employee1.setRole(OrganizationEmployeeModel.EmployeeRole.STAFF);

        employee2 = new OrganizationEmployeeEntity();
        employee2.setUser(user1);
        employee2.setOrganization(organization1);
        employee2.setRole(OrganizationEmployeeModel.EmployeeRole.STAFF);
    }

    @Test
    void getUser_SUCCESS_singleUserReturned() {
        assertNotNull(employee1.getUser());
        assertEquals(user1, employee1.getUser());
    }

    @Test
    void isEqual_SUCCESS_sameInstance() {
        assertTrue(employee1.equals(employee1));
    }

//    @Test
//    void isUnequal_SUCCESS_differentInstancesSameValues() {
//        assertFalse(employee1.equals(employee2));
//    }

    @Test
    void isUnequal_SUCCESS_differentInstancesDifferentValues() {
        employee2.setRole(OrganizationEmployeeModel.EmployeeRole.MANAGER);
        assertFalse(employee1.equals(employee2));
    }


}
