package de.remsfal.ticketing.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.model.quotation.QuotationRequestModel;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestKey;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class QuotationRequestRepositoryTest {

    @Inject
    QuotationRequestRepository repository;

    @Test
    void testInsertAndFind() {
        // Setup: Create a quotation request
        UUID projectId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID contractorId = UUID.randomUUID();
        UUID triggeredBy = UUID.randomUUID();
        
        QuotationRequestEntity entity = createQuotationRequest(projectId, issueId, contractorId, triggeredBy);
        repository.insert(entity);

        // Test: Find by key
        QuotationRequestEntity found = repository.find(entity.getKey()).orElse(null);

        // Verify: Entity was found with correct values
        assertNotNull(found);
        assertEquals(entity.getId(), found.getId());
        assertEquals(projectId, found.getProjectId());
        assertEquals(issueId, found.getIssueId());
        assertEquals(contractorId, found.getContractorId());
        assertEquals(triggeredBy, found.getTriggeredBy());
        assertEquals(QuotationRequestModel.Status.VALID, found.getStatus());
        
        // Cleanup
        repository.delete(entity.getKey());
    }

    @Test
    void testFindByIssueId() {
        // Setup: Create quotation requests for different issues
        UUID projectId = UUID.randomUUID();
        UUID issueId1 = UUID.randomUUID();
        UUID issueId2 = UUID.randomUUID();
        UUID contractorId = UUID.randomUUID();
        UUID triggeredBy = UUID.randomUUID();
        
        QuotationRequestEntity entity1 = createQuotationRequest(projectId, issueId1, contractorId, triggeredBy);
        repository.insert(entity1);
        
        QuotationRequestEntity entity2 = createQuotationRequest(projectId, issueId1, UUID.randomUUID(), triggeredBy);
        repository.insert(entity2);
        
        QuotationRequestEntity entity3 = createQuotationRequest(projectId, issueId2, contractorId, triggeredBy);
        repository.insert(entity3);

        // Test: Find by issueId1
        List<? extends QuotationRequestModel> requests = repository.findByIssueId(issueId1);

        // Verify: Should return 2 requests for issueId1
        assertNotNull(requests);
        assertEquals(2, requests.size());
        requests.forEach(request -> assertEquals(issueId1, request.getIssueId()));
        
        // Cleanup
        repository.delete(entity1.getKey());
        repository.delete(entity2.getKey());
        repository.delete(entity3.getKey());
    }

    @Test
    void testFindByContractorId() {
        // Setup: Create quotation requests for different contractors
        UUID projectId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID contractorId1 = UUID.randomUUID();
        UUID contractorId2 = UUID.randomUUID();
        UUID triggeredBy = UUID.randomUUID();
        
        QuotationRequestEntity entity1 = createQuotationRequest(projectId, issueId, contractorId1, triggeredBy);
        repository.insert(entity1);
        
        QuotationRequestEntity entity2 = createQuotationRequest(projectId, issueId, contractorId1, triggeredBy);
        repository.insert(entity2);
        
        QuotationRequestEntity entity3 = createQuotationRequest(projectId, issueId, contractorId2, triggeredBy);
        repository.insert(entity3);

        // Test: Find by contractorId1
        List<? extends QuotationRequestModel> requests = repository.findByContractorId(contractorId1);

        // Verify: Should return 2 requests for contractorId1
        assertNotNull(requests);
        assertEquals(2, requests.size());
        requests.forEach(request -> assertEquals(contractorId1, request.getContractorId()));
        
        // Cleanup
        repository.delete(entity1.getKey());
        repository.delete(entity2.getKey());
        repository.delete(entity3.getKey());
    }

    @Test
    void testUpdate() {
        // Setup: Create and insert a quotation request
        UUID projectId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID contractorId = UUID.randomUUID();
        UUID triggeredBy = UUID.randomUUID();
        
        QuotationRequestEntity entity = createQuotationRequest(projectId, issueId, contractorId, triggeredBy);
        repository.insert(entity);

        // Test: Update status to INVALID
        entity.setStatus(QuotationRequestModel.Status.INVALID);
        entity.setDescription("Updated description");
        repository.update(entity);

        // Verify: Status and description were updated
        QuotationRequestEntity updated = repository.find(entity.getKey()).orElse(null);
        assertNotNull(updated);
        assertEquals(QuotationRequestModel.Status.INVALID, updated.getStatus());
        assertEquals("Updated description", updated.getDescription());
        
        // Cleanup
        repository.delete(entity.getKey());
    }

    @Test
    void testDelete() {
        // Setup: Create and insert a quotation request
        UUID projectId = UUID.randomUUID();
        UUID issueId = UUID.randomUUID();
        UUID contractorId = UUID.randomUUID();
        UUID triggeredBy = UUID.randomUUID();
        
        QuotationRequestEntity entity = createQuotationRequest(projectId, issueId, contractorId, triggeredBy);
        repository.insert(entity);

        // Test: Delete the entity
        repository.delete(entity.getKey());

        // Verify: Entity was deleted
        assertTrue(repository.find(entity.getKey()).isEmpty());
    }

    private QuotationRequestEntity createQuotationRequest(UUID projectId, UUID issueId, 
                                                          UUID contractorId, UUID triggeredBy) {
        QuotationRequestEntity entity = new QuotationRequestEntity();
        QuotationRequestKey key = new QuotationRequestKey();
        key.setProjectId(projectId);
        key.setRequestId(UUID.randomUUID());
        entity.setKey(key);
        entity.setIssueId(issueId);
        entity.setContractorId(contractorId);
        entity.setTriggeredBy(triggeredBy);
        entity.setDescription("Test quotation request");
        entity.setStatus(QuotationRequestModel.Status.VALID);
        return entity;
    }
}
