package de.remsfal.core.json;

import de.remsfal.core.json.project.RentalAgreementKeyJson;
import de.remsfal.core.model.project.RentalAgreementKeyModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RentalAgreementKeyJsonTest {

    @Test
    void testValueOf() {
        RentalAgreementKeyModel model = new RentalAgreementKeyModelEntity(
            2, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), "Haustürschlüssel");

        RentalAgreementKeyJson json = RentalAgreementKeyJson.valueOf(model);

        assertNotNull(json);
        assertEquals(model.getAmountOfKeys(), json.getAmountOfKeys());
        assertEquals(model.getIssuedAt(), json.getIssuedAt());
        assertEquals(model.getReturnedAt(), json.getReturnedAt());
        assertEquals(model.getKeyType(), json.getKeyType());
    }

    @Test
    void testValueOf_returnsNull_whenModelIsNull() {
        assertNull(RentalAgreementKeyJson.valueOf(null));
    }

    static class RentalAgreementKeyModelEntity implements RentalAgreementKeyModel {
        private final Integer amountOfKeys;
        private final LocalDate issuedAt;
        private final LocalDate returnedAt;
        private final String keyType;

        RentalAgreementKeyModelEntity(final Integer amountOfKeys, final LocalDate issuedAt,
            final LocalDate returnedAt, final String keyType) {
            this.amountOfKeys = amountOfKeys;
            this.issuedAt = issuedAt;
            this.returnedAt = returnedAt;
            this.keyType = keyType;
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
        public String getKeyType() {
            return keyType;
        }
    }

}
