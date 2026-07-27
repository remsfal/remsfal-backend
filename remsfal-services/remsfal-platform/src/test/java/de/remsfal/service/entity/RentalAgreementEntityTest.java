package de.remsfal.service.entity;

import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.service.entity.dto.embeddable.RentalAgreementKeysEntity;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class RentalAgreementEntityTest {

    private static RentalAgreementKeysEntity key(final int amountOfKeys) {
        RentalAgreementKeysEntity key = new RentalAgreementKeysEntity();
        key.setAmountOfKeys(amountOfKeys);
        return key;
    }

    @Test
    @DisplayName("Tests setting and getting keys")
    void testSetAndGetKeys() {
        RentalAgreementEntity entity = new RentalAgreementEntity();

        entity.setKeys(List.of(key(5)));

        assertEquals(1, entity.getKeys().size());
        assertEquals(5, entity.getKeys().get(0).getAmountOfKeys());
    }

    @Test
    @DisplayName("Tests setting keys to an empty list")
    void testSetKeysEmpty() {
        RentalAgreementEntity entity = new RentalAgreementEntity();

        entity.setKeys(List.of());

        assertTrue(entity.getKeys().isEmpty());
    }

    @Test
    @DisplayName("Tests updating keys")
    void testUpdateKeys() {
        RentalAgreementEntity entity = new RentalAgreementEntity();

        entity.setKeys(List.of(key(3)));
        assertEquals(1, entity.getKeys().size());
        assertEquals(3, entity.getKeys().get(0).getAmountOfKeys());

        entity.setKeys(List.of(key(10), key(2)));
        assertEquals(2, entity.getKeys().size());
        assertEquals(10, entity.getKeys().get(0).getAmountOfKeys());
        assertEquals(2, entity.getKeys().get(1).getAmountOfKeys());
    }
}
