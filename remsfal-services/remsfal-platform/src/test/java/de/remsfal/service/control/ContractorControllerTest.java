package de.remsfal.service.control;

import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.model.ContractorModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dto.ContractorEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ContractorControllerTest extends AbstractTest {

    @Inject
    UserController userController;

    @Inject
    ProjectController projectController;

    @Inject
    ContractorController contractorController;

    // Test data for contractors
    private static final String COMPANY_NAME_1 = "Test Contractor 1";
    private static final String COMPANY_NAME_2 = "Test Contractor 2";
    private static final String PHONE_1 = "+491234567890";
    private static final String PHONE_2 = "+491234567891";
    private static final String EMAIL_1 = "contractor1@example.com";
    private static final String EMAIL_2 = "contractor2@example.com";
    private static final String TRADE_1 = "Plumbing";
    private static final String TRADE_2 = "Electrical";

    private UserModel user;
    private String projectId;

    @BeforeEach
    void setupTestData() {
        // Create a test user
        user = userController.createUser(TestData.USER_TOKEN, TestData.USER_EMAIL);
        
        // Create a test project
        ProjectEntity project = (ProjectEntity) projectController.createProject(user,
                de.remsfal.core.json.ImmutableProjectJson.builder().title(TestData.PROJECT_TITLE).build());
        projectId = project.getId();
    }

    @Test
    void getContractors_SUCCESS_emptyList() {
        List<ContractorEntity> contractors = contractorController.getContractors(projectId, 0, 10);
        assertNotNull(contractors);
        assertTrue(contractors.isEmpty());
        
        long count = contractorController.countContractors(user, projectId);
        assertEquals(0, count);
    }

    @Test
    void createContractor_SUCCESS_contractorCreated() {
        // Create a contractor JSON
        ContractorJson contractorJson = new ContractorJson();
        contractorJson.setCompanyName(COMPANY_NAME_1);
        contractorJson.setPhone(PHONE_1);
        contractorJson.setEmail(EMAIL_1);
        contractorJson.setTrade(TRADE_1);
        
        // Create the contractor
        ContractorModel contractor = contractorController.createContractor(user, projectId, contractorJson);
        
        // Verify the contractor was created
        assertNotNull(contractor);
        assertNotNull(contractor.getId());
        assertEquals(COMPANY_NAME_1, contractor.getCompanyName());
        assertEquals(PHONE_1, contractor.getPhone());
        assertEquals(EMAIL_1, contractor.getEmail());
        assertEquals(TRADE_1, contractor.getTrade());
        assertEquals(projectId, contractor.getProjectId());
        
        // Verify the contractor is in the database
        List<ContractorEntity> contractors = contractorController.getContractors(projectId, 0, 10);
        assertEquals(1, contractors.size());
        assertEquals(contractor.getId(), contractors.get(0).getId());
        
        long count = contractorController.countContractors(user, projectId);
        assertEquals(1, count);
    }

    @Test
    void getContractor_SUCCESS_contractorRetrieved() {
        // Create a contractor
        ContractorJson contractorJson = new ContractorJson();
        contractorJson.setCompanyName(COMPANY_NAME_1);
        contractorJson.setPhone(PHONE_1);
        contractorJson.setEmail(EMAIL_1);
        contractorJson.setTrade(TRADE_1);
        
        ContractorModel createdContractor = contractorController.createContractor(user, projectId, contractorJson);
        
        // Get the contractor
        ContractorModel retrievedContractor = contractorController.getContractor(user, projectId, createdContractor.getId());
        
        // Verify the contractor
        assertNotNull(retrievedContractor);
        assertEquals(createdContractor.getId(), retrievedContractor.getId());
        assertEquals(COMPANY_NAME_1, retrievedContractor.getCompanyName());
        assertEquals(PHONE_1, retrievedContractor.getPhone());
        assertEquals(EMAIL_1, retrievedContractor.getEmail());
        assertEquals(TRADE_1, retrievedContractor.getTrade());
        assertEquals(projectId, retrievedContractor.getProjectId());
    }

    @Test
    void getContractor_FAILED_contractorNotFound() {
        // Try to get a non-existent contractor
        assertThrows(NotFoundException.class, () -> 
            contractorController.getContractor(user, projectId, "non-existent-id"));
    }

    @Test
    void updateContractor_SUCCESS_contractorUpdated() {
        // Create a contractor
        ContractorJson contractorJson = new ContractorJson();
        contractorJson.setCompanyName(COMPANY_NAME_1);
        contractorJson.setPhone(PHONE_1);
        contractorJson.setEmail(EMAIL_1);
        contractorJson.setTrade(TRADE_1);
        
        ContractorModel createdContractor = contractorController.createContractor(user, projectId, contractorJson);
        
        // Update the contractor
        ContractorJson updateJson = new ContractorJson();
        updateJson.setId(createdContractor.getId());
        updateJson.setCompanyName(COMPANY_NAME_2);
        updateJson.setPhone(PHONE_2);
        updateJson.setEmail(EMAIL_2);
        updateJson.setTrade(TRADE_2);
        
        ContractorModel updatedContractor = contractorController.updateContractor(user, projectId, createdContractor.getId(), updateJson);
        
        // Verify the contractor was updated
        assertNotNull(updatedContractor);
        assertEquals(createdContractor.getId(), updatedContractor.getId());
        assertEquals(COMPANY_NAME_2, updatedContractor.getCompanyName());
        assertEquals(PHONE_2, updatedContractor.getPhone());
        assertEquals(EMAIL_2, updatedContractor.getEmail());
        assertEquals(TRADE_2, updatedContractor.getTrade());
        assertEquals(projectId, updatedContractor.getProjectId());
        
        // Verify the contractor is updated in the database
        ContractorModel retrievedContractor = contractorController.getContractor(user, projectId, createdContractor.getId());
        assertEquals(COMPANY_NAME_2, retrievedContractor.getCompanyName());
    }

    @Test
    void updateContractor_FAILED_contractorNotFound() {
        // Try to update a non-existent contractor
        ContractorJson updateJson = new ContractorJson();
        updateJson.setCompanyName(COMPANY_NAME_2);
        
        assertThrows(NotFoundException.class, () -> 
            contractorController.updateContractor(user, projectId, "non-existent-id", updateJson));
    }

    @Test
    void deleteContractor_SUCCESS_contractorDeleted() {
        // Create a contractor
        ContractorJson contractorJson = new ContractorJson();
        contractorJson.setCompanyName(COMPANY_NAME_1);
        contractorJson.setPhone(PHONE_1);
        contractorJson.setEmail(EMAIL_1);
        contractorJson.setTrade(TRADE_1);
        
        ContractorModel createdContractor = contractorController.createContractor(user, projectId, contractorJson);
        
        // Delete the contractor
        boolean deleted = contractorController.deleteContractor(user, projectId, createdContractor.getId());
        assertTrue(deleted);
        
        // Verify the contractor is deleted
        assertThrows(NotFoundException.class, () -> 
            contractorController.getContractor(user, projectId, createdContractor.getId()));
        
        List<ContractorEntity> contractors = contractorController.getContractors(projectId, 0, 10);
        assertTrue(contractors.isEmpty());
        
        long count = contractorController.countContractors(user, projectId);
        assertEquals(0, count);
    }

    @Test
    void deleteContractor_FAILED_contractorNotFound() {
        // In a real scenario, authorization would be checked at the boundary layer
        // Try to delete a non-existent contractor
        assertThrows(NotFoundException.class, () -> 
            contractorController.deleteContractor(user, projectId, "non-existent-id"));
    }
}