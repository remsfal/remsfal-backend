package de.remsfal.service.entity;

import de.remsfal.core.model.OrganizationEmployeeModel;
import de.remsfal.service.entity.dto.OrganizationEmployeeEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.AbstractTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class OrganizationEntityTest extends AbstractTest {

    private UserEntity user1;
    private UserEntity user2;
    private OrganizationEntity organization1;
    private OrganizationEntity organization2;
    private OrganizationEmployeeEntity employee1;

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
    }

    @Test
    void getEmployees_SUCCESS_employeesReturned() {
        Set<OrganizationEmployeeEntity> employees = new HashSet<>();

        employees.add(employee1);

        organization1.setEmployees(employees);

        assertEquals(employees, organization1.getEmployees());
    }

    @Test
    void addEmployee_SUCCESS_employeesIsNull() {
        organization1.addEmployee(user1, OrganizationEmployeeModel.EmployeeRole.STAFF);

        assertEquals(1, organization1.getEmployees().size());
    }

    @Test
    void isEmployee_SUCCESS_userNotEmployee() {
        organization1.addEmployee(user1, OrganizationEmployeeModel.EmployeeRole.STAFF);

        assertFalse(organization1.isEmployee(user2));
    }

    @Test
    void isEqual_SUCCESS_sameInstance() {
        assertTrue(organization1.equals(organization1));
    }

//    @Test
//    void isUnequal_SUCCESS_differentInstancesSameValues() {
//        assertFalse(organization1.equals(organization2));
//    }

    @Test
    void isUnequal_SUCCESS_differentInstancesDifferentValues() {
        organization2.setName(TestData.ORGANIZATION_NAME_2);
        assertFalse(organization1.equals(organization2));
    }
}
