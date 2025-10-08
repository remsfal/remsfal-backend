package de.remsfal.service.entity.dto;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static de.remsfal.test.TestData.USER_ID_1;
import static de.remsfal.test.TestData.USER_ID_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class ContractorEmployeeKeyTest {

    private static final String CONTRACTOR_ID_1 = "c9440c43-b5c0-4951-9c29-000000000001";
    private static final String CONTRACTOR_ID_2 = "c9440c43-b5c0-4951-9c29-000000000002";

    private ContractorEmployeeKey key1;
    private ContractorEmployeeKey key2;

    @BeforeEach
    public void setUp() {
        // Initialize key1
        key1 = new ContractorEmployeeKey();
        key1.setContractorId(CONTRACTOR_ID_1);
        key1.setUserId(USER_ID_1.toString());

        // Initialize key2 with same values as key1
        key2 = new ContractorEmployeeKey();
        key2.setContractorId(CONTRACTOR_ID_1);
        key2.setUserId(USER_ID_1.toString());
    }

    @Test
    @DisplayName("Test getters and setters")
    void testGettersAndSetters() {
        // Test getters
        assertEquals(CONTRACTOR_ID_1, key1.getContractorId());
        assertEquals(USER_ID_1.toString(), key1.getUserId());

        // Test setters by changing values
        key1.setContractorId(CONTRACTOR_ID_2);
        key1.setUserId(USER_ID_2.toString());

        // Verify changes
        assertEquals(CONTRACTOR_ID_2, key1.getContractorId());
        assertEquals(USER_ID_2.toString(), key1.getUserId());
    }

    @Test
    @DisplayName("Test equals with same values")
    void testEqualsSameValues() {
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Test equals with different contractorId")
    void testEqualsDifferentContractorId() {
        key2.setContractorId(CONTRACTOR_ID_2);
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Test equals with different userId")
    void testEqualsDifferentUserId() {
        key2.setUserId(USER_ID_2.toString());
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Test equals with null")
    void testEqualsWithNull() {
        assertNotEquals(key1, null);
    }

    @Test
    @DisplayName("Test equals with different object type")
    void testEqualsWithDifferentType() {
        assertNotEquals(key1, "Not a ContractorEmployeeKey");
    }

    @Test
    @DisplayName("Test equals with same object")
    void testEqualsWithSameObject() {
        assertEquals(key1, key1);
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void testHashCodeConsistency() {
        int initialHashCode = key1.hashCode();
        assertEquals(initialHashCode, key1.hashCode());
    }
}