package de.remsfal.core.model.project;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RentalAgreementModelTest {

    @Test
    void getTotalAmountOfKeys_returnsNull_whenKeysIsNull() {
        RentalAgreementModel model = new RentalAgreementModelEntity(null);

        assertNull(model.getTotalAmountOfKeys());
    }

    @Test
    void getTotalAmountOfKeys_returnsNull_whenKeysIsEmpty() {
        RentalAgreementModel model = new RentalAgreementModelEntity(List.of());

        assertNull(model.getTotalAmountOfKeys());
    }

    @Test
    void getTotalAmountOfKeys_returnsSum_whenKeysPresent() {
        RentalAgreementModel model = new RentalAgreementModelEntity(List.of(key(2), key(3)));

        assertEquals(5, model.getTotalAmountOfKeys());
    }

    private static RentalAgreementKeyModel key(final int amountOfKeys) {
        return new RentalAgreementKeyModel() {
            @Override
            public Integer getAmountOfKeys() {
                return amountOfKeys;
            }

            @Override
            public LocalDate getIssuedAt() {
                return null;
            }

            @Override
            public LocalDate getReturnedAt() {
                return null;
            }

            @Override
            public String getKeyType() {
                return null;
            }
        };
    }

    static class RentalAgreementModelEntity implements RentalAgreementModel {
        private final List<? extends RentalAgreementKeyModel> keys;

        RentalAgreementModelEntity(final List<? extends RentalAgreementKeyModel> keys) {
            this.keys = keys;
        }

        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public UUID getProjectId() {
            return null;
        }

        @Override
        public List<? extends TenantModel> getTenants() {
            return List.of();
        }

        @Override
        public LocalDate getStartOfRental() {
            return null;
        }

        @Override
        public LocalDate getEndOfRental() {
            return null;
        }

        @Override
        public List<? extends RentalAgreementKeyModel> getKeys() {
            return keys;
        }

        @Override
        public List<? extends RentModel> getPropertyRents() {
            return List.of();
        }

        @Override
        public List<? extends RentModel> getSiteRents() {
            return List.of();
        }

        @Override
        public List<? extends RentModel> getBuildingRents() {
            return List.of();
        }

        @Override
        public List<? extends RentModel> getApartmentRents() {
            return List.of();
        }

        @Override
        public List<? extends RentModel> getStorageRents() {
            return List.of();
        }

        @Override
        public List<? extends RentModel> getCommercialRents() {
            return List.of();
        }
    }

}
