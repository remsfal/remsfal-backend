package de.remsfal.service.entity;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.ContractorRepository;
import de.remsfal.service.entity.dto.ContractorEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ContractorRepositoryTest extends AbstractServiceTest {

    @Inject
    ContractorRepository repository;

    // Test data for contractors
    private static final UUID CONTRACTOR_ID_1 = UUID.fromString("c9440c43-b5c0-4951-9c29-000000000001");
    private static final UUID CONTRACTOR_ID_2 = UUID.fromString("c9440c43-b5c0-4951-9c29-000000000002");
    private static final String COMPANY_NAME_1 = "Test Contractor 1";
    private static final String COMPANY_NAME_2 = "Test Contractor 2";
    private static final String PHONE_1 = "+491234567890";
    private static final String PHONE_2 = "+491234567891";
    private static final String EMAIL_1 = "contractor1@example.com";
    private static final String EMAIL_2 = "contractor2@example.com";
    private static final String TRADE_1 = "Plumbing";
    private static final String TRADE_2 = "Electrical";

    @BeforeEach
    protected void setupTestData() {
        // Insert test users and projects
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO users (id, token_id, email, first_name, last_name) VALUES (?,?,?,?,?)")
                .setParameter(1, TestData.USER_ID)
                .setParameter(2, TestData.USER_TOKEN)
                .setParameter(3, TestData.USER_EMAIL)
                .setParameter(4, TestData.USER_FIRST_NAME)
                .setParameter(5, TestData.USER_LAST_NAME)
                .executeUpdate());

        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO projects (id, title) VALUES (?,?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.PROJECT_TITLE)
                .executeUpdate());

        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO project_memberships (project_id, user_id, member_role) VALUES (?,?,?)")
                .setParameter(1, TestData.PROJECT_ID)
                .setParameter(2, TestData.USER_ID)
                .setParameter(3, "MANAGER")
                .executeUpdate());

        // Insert test contractors
        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO contractors (id, project_id, company_name, phone, email, trade) VALUES (?,?,?,?,?,?)")
                .setParameter(1, CONTRACTOR_ID_1)
                .setParameter(2, TestData.PROJECT_ID)
                .setParameter(3, COMPANY_NAME_1)
                .setParameter(4, PHONE_1)
                .setParameter(5, EMAIL_1)
                .setParameter(6, TRADE_1)
                .executeUpdate());

        runInTransaction(() -> entityManager
                .createNativeQuery("INSERT INTO contractors (id, project_id, company_name, phone, email, trade) VALUES (?,?,?,?,?,?)")
                .setParameter(1, CONTRACTOR_ID_2)
                .setParameter(2, TestData.PROJECT_ID)
                .setParameter(3, COMPANY_NAME_2)
                .setParameter(4, PHONE_2)
                .setParameter(5, EMAIL_2)
                .setParameter(6, TRADE_2)
                .executeUpdate());
    }

    @Test
    void findByProjectId_SUCCESS_contractorsFound() {
        List<ContractorEntity> contractors = repository.findByProjectId(TestData.PROJECT_ID, 0, 10);
        assertNotNull(contractors);
        assertEquals(2, contractors.size());

        ContractorEntity contractor1 = contractors.stream()
                .filter(c -> c.getId().equals(CONTRACTOR_ID_1))
                .findFirst()
                .orElse(null);
        assertNotNull(contractor1);
        assertEquals(COMPANY_NAME_1, contractor1.getCompanyName());
        assertEquals(PHONE_1, contractor1.getPhone());
        assertEquals(EMAIL_1, contractor1.getEmail());
        assertEquals(TRADE_1, contractor1.getTrade());

        ContractorEntity contractor2 = contractors.stream()
                .filter(c -> c.getId().equals(CONTRACTOR_ID_2))
                .findFirst()
                .orElse(null);
        assertNotNull(contractor2);
        assertEquals(COMPANY_NAME_2, contractor2.getCompanyName());
        assertEquals(PHONE_2, contractor2.getPhone());
        assertEquals(EMAIL_2, contractor2.getEmail());
        assertEquals(TRADE_2, contractor2.getTrade());
    }

    @Test
    void countByProjectId_SUCCESS_correctCount() {
        long count = repository.countByProjectId(TestData.PROJECT_ID);
        assertEquals(2, count);
    }

    @Test
    void findByProjectIdAndContractorId_SUCCESS_contractorFound() {
        Optional<ContractorEntity> optionalContractor = repository.findByProjectIdAndContractorId(TestData.PROJECT_ID, CONTRACTOR_ID_1);
        assertTrue(optionalContractor.isPresent());

        ContractorEntity contractor = optionalContractor.get();
        assertEquals(CONTRACTOR_ID_1, contractor.getId());
        assertEquals(COMPANY_NAME_1, contractor.getCompanyName());
        assertEquals(PHONE_1, contractor.getPhone());
        assertEquals(EMAIL_1, contractor.getEmail());
        assertEquals(TRADE_1, contractor.getTrade());
    }

    @Test
    void findByProjectIdAndContractorId_FAILED_contractorNotFound() {
        Optional<ContractorEntity> optionalContractor = repository.findByProjectIdAndContractorId(TestData.PROJECT_ID, UUID.randomUUID());
        assertFalse(optionalContractor.isPresent());
    }

    @Test
    void persistAndDeleteById_SUCCESS_contractorCreatedAndDeleted() {
        // Create a new contractor
        ContractorEntity contractor = new ContractorEntity();
        contractor.generateId();

        // Get the project entity
        ProjectEntity project = entityManager.find(ProjectEntity.class, TestData.PROJECT_ID);
        contractor.setProject(project);

        contractor.setCompanyName("New Contractor");
        contractor.setPhone("+491234567892");
        contractor.setEmail("new.contractor@example.com");
        contractor.setTrade("Carpentry");

        // Persist the contractor within a transaction
        runInTransaction(() -> {
            repository.persistAndFlush(contractor);
        });

        // Verify the contractor was created
        Optional<ContractorEntity> optionalContractor = repository.findByProjectIdAndContractorId(TestData.PROJECT_ID, contractor.getId());
        assertTrue(optionalContractor.isPresent());
        assertEquals("New Contractor", optionalContractor.get().getCompanyName());

        // Delete the contractor within a transaction
        boolean deleted = runInTransaction(() -> repository.deleteById(contractor.getId()));
        assertTrue(deleted);

        // Verify the contractor was deleted
        optionalContractor = repository.findByProjectIdAndContractorId(TestData.PROJECT_ID, contractor.getId());
        assertFalse(optionalContractor.isPresent());
    }
}
