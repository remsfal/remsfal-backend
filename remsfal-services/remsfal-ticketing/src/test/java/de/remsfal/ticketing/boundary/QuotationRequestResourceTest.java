package de.remsfal.ticketing.boundary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.json.quotation.CreateQuotationRequestJson;
import de.remsfal.core.json.quotation.ImmutableCreateQuotationRequestJson;
import de.remsfal.core.json.quotation.QuotationRequestListJson;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.quotation.QuotationRequestModel;
import de.remsfal.ticketing.control.IssueController;
import de.remsfal.ticketing.control.QuotationRequestController;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;

@ExtendWith(MockitoExtension.class)
class QuotationRequestResourceTest {

    @Mock
    IssueController issueController;

    @Mock
    QuotationRequestController quotationRequestController;

    @Mock
    RemsfalPrincipal principal;

    @InjectMocks
    QuotationRequestResource resource;

    @SuppressWarnings("unchecked")
    @Test
    void testCreateQuotationRequest_Success() {
        // Setup
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID contractorId1 = UUID.randomUUID();
        UUID contractorId2 = UUID.randomUUID();

        IssueEntity issue = new IssueEntity();
        issue.setProjectId(projectId);

        CreateQuotationRequestJson request = ImmutableCreateQuotationRequestJson.builder()
            .contractorIds(Arrays.asList(contractorId1, contractorId2))
            .description("Test request")
            .build();

        QuotationRequestEntity entity1 = new QuotationRequestEntity();
        QuotationRequestEntity entity2 = new QuotationRequestEntity();

        when(issueController.getIssue(issueId)).thenReturn(issue);
        when(principal.getProjectRoles()).thenReturn(java.util.Map.of(projectId, MemberRole.MANAGER));
        when(quotationRequestController.createQuotationRequests(
            eq(principal), eq(issueId), eq(projectId), any(), any()))
            .thenReturn((List) Arrays.asList(entity1, entity2));

        // Test
        Response response = resource.createQuotationRequest(issueId, request);

        // Verify
        assertNotNull(response);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(quotationRequestController, times(1)).createQuotationRequests(
            eq(principal), eq(issueId), eq(projectId), any(), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetQuotationRequests_ByIssueId() {
        // Setup
        UUID issueId = UUID.randomUUID();
        QuotationRequestEntity entity = new QuotationRequestEntity();
        when(quotationRequestController.getQuotationRequestsByIssueId(issueId))
            .thenReturn((List) Arrays.asList(entity));

        // Test
        QuotationRequestListJson result = resource.getQuotationRequests(issueId, null, null);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getQuotationRequests().size());
    }

    @Test
    void testInvalidateQuotationRequest_Success() {
        // Setup
        UUID requestId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        QuotationRequestEntity entity = new QuotationRequestEntity();
        entity.setProjectId(projectId);

        when(quotationRequestController.getQuotationRequest(requestId)).thenReturn(entity);
        when(principal.getProjectRoles()).thenReturn(java.util.Map.of(projectId, MemberRole.MANAGER));

        // Test
        resource.invalidateQuotationRequest(requestId);

        // Verify
        verify(quotationRequestController, times(1)).invalidateQuotationRequest(requestId);
    }

}
