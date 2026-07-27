package de.remsfal.core.json;

import de.remsfal.core.json.project.RentalAgreementKeysJson;
import de.remsfal.core.model.project.RentalAgreementKeysModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RentalAgreementKeysJsonTest {

    @Test
    void testValueOf() {
        RentalAgreementKeysModel model = new RentalAgreementKeysModelEntity(
            2, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), "Haustürschlüssel");

        RentalAgreementKeysJson json = RentalAgreementKeysJson.valueOf(model);

        assertNotNull(json);
        assertEquals(model.getAmountOfKeys(), json.getAmountOfKeys());
        assertEquals(model.getIssuedAt(), json.getIssuedAt());
        assertEquals(model.getReturnedAt(), json.getReturnedAt());
        assertEquals(model.getKeyDescription(), json.getKeyDescription());
    }

    @Test
    void testValueOf_returnsNull_whenModelIsNull() {
        assertNull(RentalAgreementKeysJson.valueOf(null));
    }

    static class RentalAgreementKeysModelEntity implements RentalAgreementKeysModel {
        private final Integer amountOfKeys;
        private final LocalDate issuedAt;
        private final LocalDate returnedAt;
        private final String keyDescription;

        RentalAgreementKeysModelEntity(final Integer amountOfKeys, final LocalDate issuedAt,
            final LocalDate returnedAt, final String keyDescription) {
            this.amountOfKeys = amountOfKeys;
            this.issuedAt = issuedAt;
            this.returnedAt = returnedAt;
            this.keyDescription = keyDescription;
        }

        @Override
        public Integer getAmountOfKeys() {
            return amountOfKeys;
        }

        @Override
        public LocalDate getIssuedAt() {
            return issuedAt;
        }

        @Override
        public LocalDate getReturnedAt() {
            return returnedAt;
        }

        @Override
        public String getKeyDescription() {
            return keyDescription;
        }
    }

}
