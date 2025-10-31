package de.remsfal.core.json;

import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.ContractorModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;
import java.util.UUID;

class ContractorJsonTest {

    @Test
    void testValueOf() {
        AddressModel address = createTestAddress();
        ContractorModel model = new ContractorModelEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ACME Construction",
                "+12345678901",
                "info@acme.com",
                "Construction",
                address
        );

        ContractorJson json = ContractorJson.valueOf(model);

        assertNotNull(json);
        assertEquals(model.getId(), json.getId());
        assertEquals(model.getProjectId(), json.getProjectId());
        assertEquals(model.getCompanyName(), json.getCompanyName());
        assertEquals(model.getPhone(), json.getPhone());
        assertEquals(model.getEmail(), json.getEmail());
        assertEquals(model.getTrade(), json.getTrade());
        assertNotNull(json.getAddress());
        assertEquals(address.getStreet(), json.getAddress().getStreet());
        assertEquals(address.getCity(), json.getAddress().getCity());
    }

    @Test
    void testValueOfWithoutAddress() {
        ContractorModel model = new ContractorModelEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ACME Construction",
                "+12345678901",
                "info@acme.com",
                "Construction",
                null
        );

        ContractorJson json = ContractorJson.valueOf(model);

        assertNotNull(json);
        assertEquals(model.getId(), json.getId());
        assertEquals(model.getProjectId(), json.getProjectId());
        assertEquals(model.getCompanyName(), json.getCompanyName());
        assertEquals(model.getPhone(), json.getPhone());
        assertEquals(model.getEmail(), json.getEmail());
        assertEquals(model.getTrade(), json.getTrade());
        assertNull(json.getAddress());
    }

    @Test
    void testValueOfWithNullModel() {
        ContractorJson json = ContractorJson.valueOf(null);
        assertNull(json);
    }

    private AddressModel createTestAddress() {
        return new AddressModel() {
            @Override
            public String getStreet() {
                return "Test Street 123";
            }

            @Override
            public String getCity() {
                return "Berlin";
            }

            @Override
            public String getProvince() {
                return "Berlin";
            }

            @Override
            public String getZip() {
                return "13357";
            }

            @Override
            public Locale getCountry() {
                return Locale.GERMANY;
            }
        };
    }

    static class ContractorModelEntity implements ContractorModel {
        private final UUID id;
        private final UUID projectId;
        private final String companyName;
        private final String phone;
        private final String email;
        private final String trade;
        private final AddressModel address;

        public ContractorModelEntity(UUID id, UUID projectId, String companyName, String phone, String email, String trade, AddressModel address) {
            this.id = id;
            this.projectId = projectId;
            this.companyName = companyName;
            this.phone = phone;
            this.email = email;
            this.trade = trade;
            this.address = address;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public UUID getProjectId() {
            return projectId;
        }

        @Override
        public String getCompanyName() {
            return companyName;
        }

        @Override
        public String getPhone() {
            return phone;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public String getTrade() {
            return trade;
        }

        @Override
        public AddressModel getAddress() {
            return address;
        }
    }
}