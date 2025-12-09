package de.remsfal.ticketing.control;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.quotation.QuotationRequestModel;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class QuotationRequestController {

    @Inject
    Logger logger;

    @Inject
    QuotationRequestRepository repository;

    public List<? extends QuotationRequestModel> createQuotationRequests(
        final UserModel user,
        final UUID issueId,
        final UUID projectId,
        final List<UUID> contractorIds,
        final String description) {
        logger.infov("Creating quotation requests (issueId={0}, projectId={1}, contractors={2})",
            issueId, projectId, contractorIds.size());

        List<QuotationRequestEntity> requests = new ArrayList<>();

        for (UUID contractorId : contractorIds) {
            QuotationRequestEntity entity = new QuotationRequestEntity();
            entity.generateId();
            entity.setProjectId(projectId);
            entity.setIssueId(issueId);
            entity.setContractorId(contractorId);
            entity.setTriggeredBy(user.getId());
            entity.setDescription(description);
            entity.setStatus(QuotationRequestModel.Status.VALID);

            requests.add(repository.insert(entity));
        }

        return requests;
    }

    public List<? extends QuotationRequestModel> getQuotationRequestsByIssueId(final UUID issueId) {
        logger.infov("Retrieving quotation requests for issue (issueId={0})", issueId);
        return repository.findByIssueId(issueId);
    }

    public List<? extends QuotationRequestModel> getQuotationRequestsByContractorId(final UUID contractorId) {
        logger.infov("Retrieving quotation requests for contractor (contractorId={0})", contractorId);
        return repository.findByContractorId(contractorId);
    }

    public List<? extends QuotationRequestModel> getQuotationRequestsByProjectId(final UUID projectId) {
        logger.infov("Retrieving quotation requests for project (projectId={0})", projectId);
        return repository.findByProjectId(projectId);
    }

    public List<? extends QuotationRequestModel> getAllQuotationRequests() {
        logger.info("Retrieving all quotation requests");
        return repository.findAll();
    }

    public QuotationRequestEntity getQuotationRequest(final UUID requestId) {
        logger.infov("Retrieving quotation request (requestId={0})", requestId);
        return repository.findByRequestId(requestId)
            .orElseThrow(() -> new jakarta.ws.rs.NotFoundException("Quotation request not found"));
    }

    public void invalidateQuotationRequest(final UUID requestId) {
        logger.infov("Invalidating quotation request (requestId={0})", requestId);
        QuotationRequestEntity entity = getQuotationRequest(requestId);
        entity.setStatus(QuotationRequestModel.Status.INVALID);
        repository.update(entity);
    }

}
