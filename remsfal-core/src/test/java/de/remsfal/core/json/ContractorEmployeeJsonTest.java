package de.remsfal.core.json;

import de.remsfal.core.model.ContractorEmployeeModel;
import de.remsfal.core.model.UserModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContractorEmployeeJsonTest {

    @Test
    void testValueOf() {
        ContractorEmployeeModel model = new ContractorEmployeeModelEntity(
                "c8330c43-b5c0-4951-9c24-000000000001",
                "u7220c43-b5c0-4951-9c24-000000000002",
                "Project Manager",
                new UserModelEntity(
                        "u7220c43-b5c0-4951-9c24-000000000002",
                        "john.doe@example.com",
                        "John Doe",
                        true
                )
        );

        ContractorEmployeeJson json = ContractorEmployeeJson.valueOf(model);
        
        assertNotNull(json);
        assertEquals(model.getContractorId(), json.getContractorId());
        assertEquals(model.getUserId(), json.getUserId());
        assertEquals(model.getResponsibility(), json.getResponsibility());
        
        // Test user information
        assertNotNull(json.getUser());
        assertEquals(model.getUser().getId(), json.getUser().getId());
        assertEquals(model.getUser().getEmail(), json.getEmail());
        assertEquals(model.getUser().getName(), json.getName());
        assertEquals(model.getUser().isActive(), json.isActive());
    }

    @Test
    void testValueOfWithNullModel() {
        ContractorEmployeeJson json = ContractorEmployeeJson.valueOf(null);
        assertNull(json);
    }

    @Test
    void testValueOfWithNullUser() {
        ContractorEmployeeModel model = new ContractorEmployeeModelEntity(
                "c8330c43-b5c0-4951-9c24-000000000001",
                "u7220c43-b5c0-4951-9c24-000000000002",
                "Project Manager",
                null
        );

        ContractorEmployeeJson json = ContractorEmployeeJson.valueOf(model);
        
        assertNotNull(json);
        assertEquals(model.getContractorId(), json.getContractorId());
        assertEquals(model.getUserId(), json.getUserId());
        assertEquals(model.getResponsibility(), json.getResponsibility());
        
        // User information should be null
        assertNull(json.getEmail());
        assertNull(json.getName());
        assertNull(json.isActive());
    }

    static class ContractorEmployeeModelEntity implements ContractorEmployeeModel {
        private final String contractorId;
        private final String userId;
        private final String responsibility;
        private final UserModel user;

        public ContractorEmployeeModelEntity(String contractorId, String userId, String responsibility, UserModel user) {
            this.contractorId = contractorId;
            this.userId = userId;
            this.responsibility = responsibility;
            this.user = user;
        }

        @Override
        public String getContractorId() {
            return contractorId;
        }

        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public String getResponsibility() {
            return responsibility;
        }

        @Override
        public UserModel getUser() {
            return user;
        }
    }

    static class UserModelEntity implements UserModel {
        private final String id;
        private final String email;
        private final String name;
        private final Boolean active;

        public UserModelEntity(String id, String email, String name, Boolean active) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.active = active;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Boolean isActive() {
            return active;
        }
    }
}