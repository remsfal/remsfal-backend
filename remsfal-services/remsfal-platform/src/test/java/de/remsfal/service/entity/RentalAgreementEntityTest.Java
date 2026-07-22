package de.remsfal.service.entity;

import de.remsfal.service.entity.dto.RentalAgreementEntity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class RentalAgreementEntityTest {

    @Test
    @DisplayName("Tests setting and getting amount of keys")
    void testSetAndGetAmountOfKeys() {
        RentalAgreementEntity entity = new RentalAgreementEntity();

        entity.setAmountOfKeys(5);

        assertEquals(5, entity.getAmountOfKeys());
    }

    @Test
    @DisplayName("Tests setting amount of keys to zero")
    void testSetAmountOfKeysZero() {
        RentalAgreementEntity entity = new RentalAgreementEntity();

        entity.setAmountOfKeys(0);

        assertEquals(0, entity.getAmountOfKeys());
    }

    @Test
    @DisplayName("Tests updating amount of keys")
    void testUpdateAmountOfKeys() {
        RentalAgreementEntity entity = new RentalAgreementEntity();

        entity.setAmountOfKeys(3);
        assertEquals(3, entity.getAmountOfKeys());

        entity.setAmountOfKeys(10);
        assertEquals(10, entity.getAmountOfKeys());
    }
}