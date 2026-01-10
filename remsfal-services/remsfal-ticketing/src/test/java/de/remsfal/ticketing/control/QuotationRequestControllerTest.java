package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.quotation.QuotationRequestModel;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

@ExtendWith(MockitoExtension.class)
class QuotationRequestControllerTest {

    @Mock
    QuotationRequestRepository repository;

    @Mock
    UserModel user;

    @Mock
    Logger logger;

    @InjectMocks
    QuotationRequestController controller;

    @Test
    void testCreateQuotationRequests() {
        // Setup
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID contractorId1 = UUID.randomUUID();
        UUID contractorId2 = UUID.randomUUID();
        List<UUID> contractorIds = Arrays.asList(contractorId1, contractorId2);
        String description = "Please provide a quotation for this issue";

        when(user.getId()).thenReturn(userId);
        when(repository.insert(any(QuotationRequestEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Test
        List<? extends QuotationRequestModel> results = controller.createQuotationRequests(
            user, issueId, projectId, contractorIds, description);

        // Verify
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // Verify each request has correct values
        results.forEach(request -> {
            assertEquals(projectId, request.getProjectId());
            assertEquals(issueId, request.getIssueId());
            assertEquals(userId, request.getTriggeredBy());
            assertEquals(description, request.getDescription());
            assertEquals(QuotationRequestModel.Status.VALID, request.getStatus());
        });

        // Verify repository.insert was called twice
        verify(repository, times(2)).insert(any(QuotationRequestEntity.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetQuotationRequestsByIssueId() {
        // Setup
        UUID issueId = UUID.randomUUID();
        QuotationRequestEntity entity1 = new QuotationRequestEntity();
        QuotationRequestEntity entity2 = new QuotationRequestEntity();

        when(repository.findByIssueId(issueId))
            .thenReturn((List) Arrays.asList(entity1, entity2));

        // Test
        List<? extends QuotationRequestModel> results = controller.getQuotationRequestsByIssueId(issueId);

        // Verify
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(repository, times(1)).findByIssueId(issueId);
    }
}
