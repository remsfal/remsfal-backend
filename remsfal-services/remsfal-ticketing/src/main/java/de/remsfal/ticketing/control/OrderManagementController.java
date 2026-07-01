package de.remsfal.ticketing.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.model.ticketing.OrderPlacementModel.OrderPlacementStatus;
import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.QuotationModel.QuotationStatus;
import de.remsfal.core.model.ticketing.QuotationRequestModel.RequestStatus;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dao.OrderPlacementRepository;
import de.remsfal.ticketing.entity.dao.QuotationRepository;
import de.remsfal.ticketing.entity.dao.QuotationRequestRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.OrderPlacementEntity;
import de.remsfal.ticketing.entity.dto.QuotationEntity;
import de.remsfal.ticketing.entity.dto.QuotationKey;
import de.remsfal.ticketing.entity.dto.QuotationRequestEntity;
import de.remsfal.ticketing.entity.dto.QuotationRequestKey;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequestScoped
public class OrderManagementController {

    static final String QUOTATION_REQUEST_NOT_FOUND = "Quotation request not found";

    @Inject
    Logger logger;

    @Inject
    RemsfalPrincipal principal;

    @Inject
    IssueRepository issueRepository;

    @Inject
    QuotationRequestRepository quotationRequestRepository;

    @Inject
    QuotationRepository quotationRepository;

    @Inject
    OrderPlacementRepository orderPlacementRepository;

    public void createRequestsForQuotation(final UserModel user, final UUID issueId,
        final List<ContractorJson> contractors, final String scopeOfWork,
        final String projectOwner, final String projectCareOf, final AddressModel billingAddress) {
        IssueEntity issue = issueRepository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException("Issue not found"));
        contractors.stream().distinct().forEach(contractor -> {
            QuotationRequestEntity request = new QuotationRequestEntity();
            request.generateId();
            request.setIssueId(issueId);
            request.setProjectId(issue.getProjectId());
            request.setInitiatorId(user.getId());
            request.setInitiatedBy(user.getName());
            request.setContractorId(contractor.getId());
            request.setOrganizationId(contractor.getOrganizationId());
            request.setContractorName(contractor.getCompanyName());
            request.setScopeOfWork(scopeOfWork);
            request.setProjectOwner(projectOwner);
            request.setProjectCareOf(projectCareOf);
            if (billingAddress != null) {
                request.setProjectBillingAddress1(billingAddress.getAddressLine1());
                request.setProjectBillingAddress2(billingAddress.getAddressLine2());
                request.setProjectBillingAddress3(billingAddress.getAddressLine3());
            }
            request.setStatus(RequestStatus.REQUESTED);
            quotationRequestRepository.insert(request);
        });
    }

    public List<QuotationRequestEntity> getRequestsForQuotation(final UUID issueId) {
        logger.infov("Retrieving quotation requests for issue (issueId={0})", issueId);
        return quotationRequestRepository.findByIssueId(issueId);
    }

    public QuotationRequestEntity getRequestForQuotation(final UUID issueId, final UUID requestId) {
        logger.infov("Retrieving quotation request (issueId={0}, requestId={1})", issueId, requestId);
        QuotationRequestKey key = new QuotationRequestKey();
        key.setIssueId(issueId);
        key.setRequestId(requestId);
        return quotationRequestRepository.findById(key)
            .orElseThrow(() -> new NotFoundException(QUOTATION_REQUEST_NOT_FOUND));
    }

    public QuotationRequestEntity updateRequestForQuotation(final UUID issueId, final UUID requestId,
        final QuotationRequestJson body) {
        QuotationRequestEntity entity = getRequestForQuotation(issueId, requestId);
        if (body.getScopeOfWork() != null) {
            entity.setScopeOfWork(body.getScopeOfWork());
        }
        if (body.getStatus() != null) {
            if (body.getStatus() != RequestStatus.WITHDRAWN) {
                throw new BadRequestException("Manager can only set status to WITHDRAWN");
            }
            entity.setStatus(body.getStatus());
        }
        return quotationRequestRepository.update(entity);
    }

    public QuotationRequestEntity updateRequestForQuotationByContractor(
        final Set<UUID> organizationIds, final UUID requestId, final QuotationRequestJson body) {
        if (body.getStatus() == null) {
            throw new BadRequestException("Status must be provided");
        }
        final Set<RequestStatus> allowedStatuses = Set.of(
            RequestStatus.VIEWING_REQUIRED,
            RequestStatus.CONSULTATION_REQUIRED,
            RequestStatus.REJECTED,
            RequestStatus.SUBMITTED
        );
        if (!allowedStatuses.contains(body.getStatus())) {
            throw new BadRequestException("Contractor can only set status to VIEWING_REQUIRED,"
                + " CONSULTATION_REQUIRED, REJECTED, or SUBMITTED");
        }
        final QuotationRequestEntity entity = organizationIds.stream()
            .flatMap(orgId -> quotationRequestRepository.findByOrganizationId(orgId).stream())
            .filter(r -> requestId.equals(r.getRequestId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(QUOTATION_REQUEST_NOT_FOUND));
        entity.setStatus(body.getStatus());
        return quotationRequestRepository.update(entity);
    }

    public List<QuotationRequestEntity> getRequestsForQuotationByOrganizationIds(
        final Set<UUID> organizationIds) {
        logger.infov("Retrieving quotation requests for organizations (count={0})", organizationIds.size());
        return organizationIds.stream()
            .flatMap(orgId -> quotationRequestRepository.findByOrganizationId(orgId).stream())
            .toList();
    }

    public QuotationEntity createQuotationByContractor(final Set<UUID> organizationIds, final UUID requestId,
        final QuotationJson body) {
        final QuotationRequestEntity request = organizationIds.stream()
            .flatMap(orgId -> quotationRequestRepository.findByOrganizationId(orgId).stream())
            .filter(r -> requestId.equals(r.getRequestId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(QUOTATION_REQUEST_NOT_FOUND));

        QuotationEntity quotation = new QuotationEntity();
        quotation.generateId();
        quotation.setIssueId(request.getIssueId());
        quotation.setRequestId(request.getRequestId());
        quotation.setProjectId(request.getProjectId());
        quotation.setOffererId(principal.getId());
        quotation.setOfferedBy(principal.getName());
        quotation.setContractorId(request.getContractorId());
        quotation.setOrganizationId(request.getOrganizationId());
        quotation.setAttachments(body.getAttachments());
        quotation.setValidUntil(body.getValidUntil());
        quotation.setStatus(body.getStatus() != null ? body.getStatus() : QuotationStatus.VALID);
        return quotationRepository.insert(quotation);
    }

    public void placeOrder(final UUID issueId, final UUID quotationId) {
        QuotationKey key = new QuotationKey();
        key.setIssueId(issueId);
        key.setQuotationId(quotationId);
        QuotationEntity quotation = quotationRepository.findById(key)
            .orElseThrow(() -> new NotFoundException("Quotation not found"));

        QuotationRequestKey requestKey = new QuotationRequestKey();
        requestKey.setIssueId(quotation.getIssueId());
        requestKey.setRequestId(quotation.getRequestId());
        QuotationRequestEntity request = quotationRequestRepository.findById(requestKey)
            .orElseThrow(() -> new NotFoundException("Quotation request not found"));

        OrderPlacementEntity orderPlacement = new OrderPlacementEntity();
        orderPlacement.generateId();
        orderPlacement.setIssueId(issueId);
        orderPlacement.setQuotationId(quotationId);
        orderPlacement.setProjectId(quotation.getProjectId());
        orderPlacement.setOrdererId(principal.getId());
        orderPlacement.setOrderedBy(principal.getName());
        orderPlacement.setContractorId(quotation.getContractorId());
        orderPlacement.setOrganizationId(request.getOrganizationId());
        orderPlacement.setStatus(OrderPlacementStatus.PLACED);
        orderPlacementRepository.insert(orderPlacement);
    }

    public List<QuotationEntity> getQuotationsByIssue(final UUID issueId) {
        logger.infov("Retrieving quotations for issue (issueId={0})", issueId);
        return quotationRepository.findByIssueId(issueId);
    }

    public QuotationEntity getQuotation(final UUID issueId, final UUID quotationId) {
        logger.infov("Retrieving quotation (issueId={0}, quotationId={1})", issueId, quotationId);
        QuotationKey key = new QuotationKey();
        key.setIssueId(issueId);
        key.setQuotationId(quotationId);
        return quotationRepository.findById(key)
            .orElseThrow(() -> new NotFoundException("Quotation not found"));
    }

    public OrderPlacementEntity getOrderPlacementByQuotation(final UUID issueId, final UUID quotationId) {
        logger.infov("Retrieving order placement (issueId={0}, quotationId={1})", issueId, quotationId);
        return orderPlacementRepository.findByIssueIdAndQuotationId(issueId, quotationId)
            .orElseThrow(() -> new NotFoundException("Order placement not found"));
    }

    public void withdrawOrderPlacement(final UUID issueId, final UUID quotationId) {
        OrderPlacementEntity placement = getOrderPlacementByQuotation(issueId, quotationId);
        placement.setStatus(OrderPlacementStatus.WITHDRAWN);
        orderPlacementRepository.update(placement);
    }

    public List<QuotationEntity> getQuotationsByOrganizationIds(final Set<UUID> organizationIds) {
        logger.infov("Retrieving quotations for organizations (count={0})", organizationIds.size());
        return organizationIds.stream()
            .flatMap(orgId -> quotationRepository.findByOrganizationId(orgId).stream())
            .toList();
    }

    public QuotationEntity getQuotationForOrganization(final Set<UUID> organizationIds,
        final UUID quotationId) {
        return organizationIds.stream()
            .flatMap(orgId -> quotationRepository.findByOrganizationId(orgId).stream())
            .filter(q -> quotationId.equals(q.getId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Quotation not found"));
    }

    public List<OrderPlacementEntity> getOrderPlacementsByOrganizationIds(final Set<UUID> organizationIds) {
        logger.infov("Retrieving order placements for organizations (count={0})", organizationIds.size());
        return organizationIds.stream()
            .flatMap(orgId -> orderPlacementRepository.findByOrganizationId(orgId).stream())
            .toList();
    }

    public OrderPlacementEntity getOrderPlacementForOrganization(final Set<UUID> organizationIds,
        final UUID placementId) {
        return organizationIds.stream()
            .flatMap(orgId -> orderPlacementRepository.findByOrganizationId(orgId).stream())
            .filter(p -> placementId.equals(p.getId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Order placement not found"));
    }

    public OrderPlacementEntity updateOrderPlacementStatus(final Set<UUID> organizationIds,
        final UUID placementId, final OrderPlacementStatus status) {
        final Set<OrderPlacementStatus> allowedStatuses = Set.of(
            OrderPlacementStatus.CONFIRMED,
            OrderPlacementStatus.REJECTED
        );
        if (!allowedStatuses.contains(status)) {
            throw new BadRequestException("Contractor can only set status to CONFIRMED or REJECTED");
        }
        OrderPlacementEntity placement = getOrderPlacementForOrganization(organizationIds, placementId);
        placement.setStatus(status);
        placement.setConfirmorId(principal.getId());
        placement.setConfirmedBy(principal.getName());
        return orderPlacementRepository.update(placement);
    }

}
