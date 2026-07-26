package de.remsfal.service.entity.dto;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
class RentalAgreementKeyEntityTest {

    private static final LocalDate ISSUED_AT_1 = LocalDate.of(2025, 1, 1);
    private static final LocalDate ISSUED_AT_2 = LocalDate.of(2025, 2, 1);
    private static final LocalDate RETURNED_AT_1 = LocalDate.of(2025, 12, 1);
    private static final LocalDate RETURNED_AT_2 = LocalDate.of(2025, 12, 15);
    private static final String KEY_TYPE_1 = "Haustürschlüssel";
    private static final String KEY_TYPE_2 = "Briefkastenschlüssel";

    private RentalAgreementKeyEntity key1;
    private RentalAgreementKeyEntity key2;

    @BeforeEach
    void setUp() {
        key1 = new RentalAgreementKeyEntity();
        key1.setAmountOfKeys(2);
        key1.setIssuedAt(ISSUED_AT_1);
        key1.setReturnedAt(RETURNED_AT_1);
        key1.setKeyType(KEY_TYPE_1);

        key2 = new RentalAgreementKeyEntity();
        key2.setAmountOfKeys(2);
        key2.setIssuedAt(ISSUED_AT_1);
        key2.setReturnedAt(RETURNED_AT_1);
        key2.setKeyType(KEY_TYPE_1);
    }

    @Test
    @DisplayName("Test getters and setters")
    void testGettersAndSetters() {
        assertEquals(2, key1.getAmountOfKeys());
        assertEquals(ISSUED_AT_1, key1.getIssuedAt());
        assertEquals(RETURNED_AT_1, key1.getReturnedAt());
        assertEquals(KEY_TYPE_1, key1.getKeyType());

        key1.setAmountOfKeys(5);
        key1.setIssuedAt(ISSUED_AT_2);
        key1.setReturnedAt(RETURNED_AT_2);
        key1.setKeyType(KEY_TYPE_2);

        assertEquals(5, key1.getAmountOfKeys());
        assertEquals(ISSUED_AT_2, key1.getIssuedAt());
        assertEquals(RETURNED_AT_2, key1.getReturnedAt());
        assertEquals(KEY_TYPE_2, key1.getKeyType());
    }

    @Test
    @DisplayName("Test equals with same values")
    void testEqualsSameValues() {
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Test equals with different amountOfKeys")
    void testEqualsDifferentAmountOfKeys() {
        key2.setAmountOfKeys(3);
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Test equals with different issuedAt")
    void testEqualsDifferentIssuedAt() {
        key2.setIssuedAt(ISSUED_AT_2);
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Test equals with different returnedAt")
    void testEqualsDifferentReturnedAt() {
        key2.setReturnedAt(RETURNED_AT_2);
        assertNotEquals(key1, key2);
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Test equals with different keyType")
    void testEqualsDifferentKeyType() {
        key2.setKeyType(KEY_TYPE_2);
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
        assertNotEquals(key1, "Not a RentalAgreementKeyEntity");
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
