package de.remsfal.ticketing.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.QuotationModel;
import de.remsfal.core.model.ticketing.QuotationModel.QuotationStatus;
import de.remsfal.ticketing.entity.dao.QuotationRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.QuotationEntity;
import de.remsfal.ticketing.entity.dto.QuotationKey;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing quotations.
 * 
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class QuotationController {

    @Inject
    Logger logger;

    @Inject
    QuotationRepository repository;

    @Inject
    IssueController issueController;

    public QuotationModel createQuotation(final UserModel user, final UUID issueId, 
            final QuotationModel quotation) {
        logger.infov("Creating a quotation (issueId={0}, contractor={1})", issueId, user.getEmail());
        
        // Get the issue to extract project_id and validate existence
        final IssueEntity issue = issueController.getIssue(issueId);
        
        final QuotationEntity entity = new QuotationEntity();
        entity.generateId();
        entity.setProjectId(issue.getProjectId());
        entity.setIssueId(issueId);
        entity.setContractorId(user.getId());
        // The requesterId could be the issue reporter or the creator
        entity.setRequesterId(issue.getReporterId() != null ? issue.getReporterId() : issue.getCreatedBy());
        entity.setText(quotation.getText());
        entity.setStatus(QuotationStatus.VALID);
        
        return repository.insert(entity);
    }

    public QuotationEntity getQuotation(final UUID quotationId) {
        logger.infov("Retrieving quotation (quotationId={0})", quotationId);
        return repository.findByQuotationId(quotationId)
            .orElseThrow(() -> new NotFoundException("Quotation not found"));
    }

    public List<? extends QuotationModel> getQuotations(final UUID projectId, final UUID issueId) {
        logger.infov("Retrieving quotations for issue (projectId={0}, issueId={1})", projectId, issueId);
        return repository.findByIssue(projectId, issueId);
    }

    public void deleteQuotation(final QuotationKey key) {
        logger.infov("Deleting quotation (projectId={0}, issueId={1}, quotationId={2})", 
            key.getProjectId(), key.getIssueId(), key.getQuotationId());
        repository.delete(key);
    }

}
