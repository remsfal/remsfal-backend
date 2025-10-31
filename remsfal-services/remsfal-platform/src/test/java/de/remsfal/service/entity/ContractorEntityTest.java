package de.remsfal.service.entity;

import de.remsfal.service.entity.dto.AddressEntity;
import de.remsfal.service.entity.dto.ContractorEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static de.remsfal.test.TestData.ADDRESS_CITY_1;
import static de.remsfal.test.TestData.ADDRESS_CITY_2;
import static de.remsfal.test.TestData.ADDRESS_COUNTRY_1;
import static de.remsfal.test.TestData.ADDRESS_COUNTRY_2;
import static de.remsfal.test.TestData.ADDRESS_ID_1;
import static de.remsfal.test.TestData.ADDRESS_ID_2;
import static de.remsfal.test.TestData.ADDRESS_PROVINCE_1;
import static de.remsfal.test.TestData.ADDRESS_PROVINCE_2;
import static de.remsfal.test.TestData.ADDRESS_STREET_1;
import static de.remsfal.test.TestData.ADDRESS_STREET_2;
import static de.remsfal.test.TestData.ADDRESS_ZIP_1;
import static de.remsfal.test.TestData.ADDRESS_ZIP_2;
import static de.remsfal.test.TestData.PROJECT_ID_1;
import static de.remsfal.test.TestData.PROJECT_ID_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class ContractorEntityTest {

    private ContractorEntity entity1;
    private ContractorEntity entity2;
    private ProjectEntity project1;
    private ProjectEntity project2;
    private AddressEntity address1;
    private AddressEntity address2;

    @BeforeEach
    public void setUp() {
        project1 = new ProjectEntity();
        project1.setId(PROJECT_ID_1);

        project2 = new ProjectEntity();
        project2.setId(PROJECT_ID_2);

        address1 = new AddressEntity();
        address1.setId(ADDRESS_ID_1);
        address1.setStreet(ADDRESS_STREET_1);
        address1.setCity(ADDRESS_CITY_1);
        address1.setProvince(ADDRESS_PROVINCE_1);
        address1.setZip(ADDRESS_ZIP_1);
        address1.setCountry(new Locale("", ADDRESS_COUNTRY_1));

        address2 = new AddressEntity();
        address2.setId(ADDRESS_ID_2);
        address2.setStreet(ADDRESS_STREET_2);
        address2.setCity(ADDRESS_CITY_2);
        address2.setProvince(ADDRESS_PROVINCE_2);
        address2.setZip(ADDRESS_ZIP_2);
        address2.setCountry(new Locale("", ADDRESS_COUNTRY_2));

        entity1 = new ContractorEntity();
        entity1.setProject(project1);
        entity1.setCompanyName("Test Company");
        entity1.setPhone("+49123456789");
        entity1.setEmail("test@company.com");
        entity1.setTrade("Plumbing");
        entity1.setAddress(address1);

        entity2 = new ContractorEntity();
        entity2.setProject(project1);
        entity2.setCompanyName("Test Company");
        entity2.setPhone("+49123456789");
        entity2.setEmail("test@company.com");
        entity2.setTrade("Plumbing");
        entity2.setAddress(address1);
    }

    @Test
    @DisplayName("Tests two equal objects")
    void testEqualsSameValues() {
        assertEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests same object reference")
    void testEqualsSameObject() {
        assertEquals(entity1, entity1);
    }

    @Test
    @DisplayName("Tests equality with null")
    void testEqualsNull() {
        assertNotEquals(null, entity1);
    }

    @Test
    @DisplayName("Tests equality with different class")
    void testEqualsDifferentClass() {
        assertNotEquals(entity1, new Object());
    }

    @Test
    @DisplayName("Tests two unequal objects (different project)")
    void testEqualsDifferentProjects() {
        entity2.setProject(project2);
        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests two unequal objects (different company name)")
    void testEqualsDifferentCompanyNames() {
        entity2.setCompanyName("Other Company");
        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests two unequal objects (different phone)")
    void testEqualsDifferentPhones() {
        entity2.setPhone("+49987654321");
        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests two unequal objects (different email)")
    void testEqualsDifferentEmails() {
        entity2.setEmail("other@company.com");
        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests two unequal objects (different trade)")
    void testEqualsDifferentTrades() {
        entity2.setTrade("Electrical");
        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests two unequal objects (different address)")
    void testEqualsDifferentAddresses() {
        entity2.setAddress(address2);
        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests getProjectId with non-null project")
    void testGetProjectIdWithProject() {
        assertEquals(PROJECT_ID_1, entity1.getProjectId());
    }

    @Test
    @DisplayName("Tests getProjectId with null project")
    void testGetProjectIdWithoutProject() {
        ContractorEntity entity = new ContractorEntity();
        entity.setProject(null);
        assertNull(entity.getProjectId());
    }

    @Test
    @DisplayName("Tests getProject method")
    void testGetProject() {
        assertEquals(project1, entity1.getProject());
    }
}