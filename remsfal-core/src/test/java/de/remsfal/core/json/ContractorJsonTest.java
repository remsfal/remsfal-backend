package de.remsfal.core.json;

import de.remsfal.core.model.ContractorModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContractorJsonTest {

    @Test
    void testValueOf() {
        ContractorModel model = new ContractorModelEntity(
                "c8330c43-b5c0-4951-9c24-000000000001",
                "p7220c43-b5c0-4951-9c24-000000000002",
                "ACME Construction",
                "+12345678901",
                "info@acme.com",
                "Construction"
        );

        ContractorJson json = ContractorJson.valueOf(model);
        
        assertNotNull(json);
        assertEquals(model.getId(), json.getId());
        assertEquals(model.getProjectId(), json.getProjectId());
        assertEquals(model.getCompanyName(), json.getCompanyName());
        assertEquals(model.getPhone(), json.getPhone());
        assertEquals(model.getEmail(), json.getEmail());
        assertEquals(model.getTrade(), json.getTrade());
    }

    @Test
    void testValueOfWithNullModel() {
        ContractorJson json = ContractorJson.valueOf(null);
        assertNull(json);
    }

    static class ContractorModelEntity implements ContractorModel {
        private final String id;
        private final String projectId;
        private final String companyName;
        private final String phone;
        private final String email;
        private final String trade;

        public ContractorModelEntity(String id, String projectId, String companyName, String phone, String email, String trade) {
            this.id = id;
            this.projectId = projectId;
            this.companyName = companyName;
            this.phone = phone;
            this.email = email;
            this.trade = trade;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getProjectId() {
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
    }
}