package de.remsfal.ticketing.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.ticketing.OrderPlacementJson;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.json.ticketing.QuotationRequestJson;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.OrderPlacementModel.OrderPlacementStatus;
import de.remsfal.core.model.AddressModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.QuotationModel.QuotationStatus;
import de.remsfal.core.model.ticketing.QuotationRequestModel.RequestStatus;
import de.remsfal.ticketing.boundary.eventing.IssueEventProducer;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequestScoped
public class OrderManagementController {

    static final String QUOTATION_REQUEST_NOT_FOUND = "Quotation request not found";

    private static final Set<RequestStatus> OPEN_REQUEST_STATUSES = Set.of(
        RequestStatus.REQUESTED,
        RequestStatus.VIEWING_REQUIRED,
        RequestStatus.CONSULTATION_REQUIRED
    );

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

    @Inject
    IssueEventProducer issueEventProducer;

    private IssueModel findIssue(final UUID issueId) {
        return issueRepository.findByIssueId(issueId).orElse(null);
    }

    public List<QuotationRequestEntity> createRequestsForQuotation(final UserModel user, final UUID issueId,
        final List<ContractorJson> contractors, final String scopeOfWork,
        final String projectOwner, final String projectCareOf, final AddressModel billingAddress) {
        IssueEntity issue = issueRepository.findByIssueId(issueId)
            .orElseThrow(() -> new NotFoundException("Issue not found"));
        final List<QuotationRequestEntity> existingRequests = quotationRequestRepository.findByIssueId(issueId);
        return contractors.stream().distinct()
            .map(contractor -> createRequestForQuotation(issue, existingRequests, contractor, user,
                scopeOfWork, projectOwner, projectCareOf, billingAddress))
            .toList();
    }

    private QuotationRequestEntity createRequestForQuotation(final IssueEntity issue,
        final List<QuotationRequestEntity> existingRequests, final ContractorJson contractor, final UserModel user,
        final String scopeOfWork, final String projectOwner, final String projectCareOf,
        final AddressModel billingAddress) {
        withdrawOpenRequests(issue, existingRequests, contractor, user);
        QuotationRequestEntity request = new QuotationRequestEntity();
        request.generateId();
        request.setIssueId(issue.getId());
        request.setProjectId(issue.getProjectId());
        request.setInitiatorId(user.getId());
        request.setInitiatedBy(user.getName());
        request.setContractorId(contractor.getId());
        request.setOrganizationId(contractor.getOrganizationId());
        request.setContractorName(contractor.getName());
        request.setScopeOfWork(scopeOfWork);
        request.setProjectOwner(projectOwner);
        request.setProjectCareOf(projectCareOf);
        if (billingAddress != null) {
            request.setProjectBillingAddress1(billingAddress.getAddressLine1());
            request.setProjectBillingAddress2(billingAddress.getAddressLine2());
            request.setProjectBillingAddress3(billingAddress.getAddressLine3());
        }
        request.setStatus(RequestStatus.REQUESTED);
        final QuotationRequestEntity inserted = quotationRequestRepository.insert(request);
        issueEventProducer.sendQuotationRequestCreated(issue, QuotationRequestJson.valueOf(inserted), user);
        return inserted;
    }

    public List<QuotationRequestEntity> getRequestsForQuotation(final UUID issueId) {
        logger.infov("Retrieving quotation requests for issue (issueId={0})", issueId);
        return quotationRequestRepository.findByIssueId(issueId);
    }

    public QuotationRequestEntity getRequestForQuotation(final UUID issueId, final UUID requestId) {
        logger.infov("Retrieving quotation request (issueId={0}, requestId={1})", issueId, requestId);
        return quotationRequestRepository.findById(requestKey(issueId, requestId))
            .orElseThrow(() -> new NotFoundException(QUOTATION_REQUEST_NOT_FOUND));
    }

    public QuotationRequestEntity updateRequestForQuotation(final UUID issueId, final UUID requestId,
        final QuotationRequestJson body) {
        QuotationRequestEntity entity = mergeQuotationRequestFields(body, getRequestForQuotation(issueId, requestId));
        final QuotationRequestEntity updated = quotationRequestRepository.update(entity);
        issueEventProducer.sendQuotationRequestStatusChanged(findIssue(entity.getIssueId()),
            QuotationRequestJson.valueOf(updated), principal);
        return updated;
    }

    public QuotationRequestEntity updateRequestForQuotationByContractor(
        final Set<UUID> organizationIds, final UUID requestId, final QuotationRequestJson body) {
        if (body.getStatus() == null) {
            throw new BadRequestException("Status must be provided");
        }
        validateAllowedStatus(body.getStatus(), Set.of(
            RequestStatus.VIEWING_REQUIRED,
            RequestStatus.CONSULTATION_REQUIRED,
            RequestStatus.REJECTED,
            RequestStatus.SUBMITTED
        ), "Contractor can only set status to VIEWING_REQUIRED,"
            + " CONSULTATION_REQUIRED, REJECTED, or SUBMITTED");
        final QuotationRequestEntity entity = findByOrganizationIds(organizationIds,
            quotationRequestRepository::findByOrganizationId,
            r -> requestId.equals(r.getRequestId()),
            () -> new NotFoundException(QUOTATION_REQUEST_NOT_FOUND));
        entity.setStatus(body.getStatus());
        final QuotationRequestEntity updated = quotationRequestRepository.update(entity);
        issueEventProducer.sendQuotationRequestStatusChanged(findIssue(entity.getIssueId()),
            QuotationRequestJson.valueOf(updated), principal);
        return updated;
    }

    public List<QuotationRequestEntity> getRequestsForQuotationByOrganizationIds(
        final Set<UUID> organizationIds) {
        logger.infov("Retrieving quotation requests for organizations (count={0})", organizationIds.size());
        return findAllByOrganizationIds(organizationIds, quotationRequestRepository::findByOrganizationId);
    }

    public QuotationRequestEntity getRequestForQuotationByOrganizationIds(final Set<UUID> organizationIds,
        final UUID requestId) {
        return findByOrganizationIds(organizationIds, quotationRequestRepository::findByOrganizationId,
            r -> requestId.equals(r.getRequestId()),
            () -> new NotFoundException(QUOTATION_REQUEST_NOT_FOUND));
    }

    public QuotationRequestEntity getRequestForIssueByOrganizationIds(final Set<UUID> organizationIds,
        final UUID issueId) {
        return quotationRequestRepository.findByIssueId(issueId).stream()
            .filter(r -> organizationIds.contains(r.getOrganizationId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(QUOTATION_REQUEST_NOT_FOUND));
    }

    public QuotationEntity createQuotationByContractor(final Set<UUID> organizationIds, final UUID requestId,
        final QuotationJson body) {
        final QuotationRequestEntity request = findByOrganizationIds(organizationIds,
            quotationRequestRepository::findByOrganizationId,
            r -> requestId.equals(r.getRequestId()),
            () -> new NotFoundException(QUOTATION_REQUEST_NOT_FOUND));

        final QuotationEntity inserted = quotationRepository.insert(buildQuotation(request, body));
        issueEventProducer.sendQuotationCreated(findIssue(request.getIssueId()), QuotationJson.valueOf(inserted),
            principal);
        return inserted;
    }

    public OrderPlacementEntity placeOrder(final UUID issueId, final UUID quotationId) {
        QuotationEntity quotation = quotationRepository.findById(quotationKey(issueId, quotationId))
            .orElseThrow(() -> new NotFoundException("Quotation not found"));

        final OrderPlacementEntity inserted = orderPlacementRepository.insert(
            buildOrderPlacement(issueId, quotationId, quotation));
        issueEventProducer.sendOrderPlaced(findIssue(issueId), OrderPlacementJson.valueOf(inserted),
            principal);
        return inserted;
    }

    public List<QuotationEntity> getQuotationsByIssue(final UUID issueId) {
        logger.infov("Retrieving quotations for issue (issueId={0})", issueId);
        return quotationRepository.findByIssueId(issueId);
    }

    public QuotationEntity getQuotation(final UUID issueId, final UUID quotationId) {
        logger.infov("Retrieving quotation (issueId={0}, quotationId={1})", issueId, quotationId);
        return quotationRepository.findById(quotationKey(issueId, quotationId))
            .orElseThrow(() -> new NotFoundException("Quotation not found"));
    }

    public List<OrderPlacementEntity> getOrderPlacementsByIssue(final UUID issueId) {
        logger.infov("Retrieving order placements for issue (issueId={0})", issueId);
        return orderPlacementRepository.findByIssueId(issueId);
    }

    public OrderPlacementEntity getOrderPlacementForIssue(final UUID issueId, final UUID orderId) {
        logger.infov("Retrieving order placement (issueId={0}, orderId={1})", issueId, orderId);
        return orderPlacementRepository.findByIssueIdAndId(issueId, orderId)
            .orElseThrow(() -> new NotFoundException("Order placement not found"));
    }

    public void withdrawOrderPlacement(final UUID issueId, final UUID orderId) {
        OrderPlacementEntity placement = getOrderPlacementForIssue(issueId, orderId);
        placement.setStatus(OrderPlacementStatus.WITHDRAWN);
        final OrderPlacementEntity updated = orderPlacementRepository.update(placement);
        issueEventProducer.sendOrderPlacementWithdrawn(findIssue(issueId), OrderPlacementJson.valueOf(updated),
            principal);
    }

    public List<QuotationEntity> getQuotationsByOrganizationIds(final Set<UUID> organizationIds) {
        logger.infov("Retrieving quotations for organizations (count={0})", organizationIds.size());
        return findAllByOrganizationIds(organizationIds, quotationRepository::findByOrganizationId);
    }

    public QuotationEntity getQuotationForOrganization(final Set<UUID> organizationIds,
        final UUID quotationId) {
        return findByOrganizationIds(organizationIds, quotationRepository::findByOrganizationId,
            q -> quotationId.equals(q.getId()),
            () -> new NotFoundException("Quotation not found"));
    }

    public List<OrderPlacementEntity> getOrderPlacementsByOrganizationIds(final Set<UUID> organizationIds) {
        logger.infov("Retrieving order placements for organizations (count={0})", organizationIds.size());
        return findAllByOrganizationIds(organizationIds, orderPlacementRepository::findByOrganizationId);
    }

    public OrderPlacementEntity getOrderPlacementForOrganization(final Set<UUID> organizationIds,
        final UUID placementId) {
        return findByOrganizationIds(organizationIds, orderPlacementRepository::findByOrganizationId,
            p -> placementId.equals(p.getId()),
            () -> new NotFoundException("Order placement not found"));
    }

    public OrderPlacementEntity updateOrderPlacementStatus(final Set<UUID> organizationIds,
        final UUID placementId, final OrderPlacementStatus status) {
        validateAllowedStatus(status, Set.of(
            OrderPlacementStatus.CONFIRMED,
            OrderPlacementStatus.REJECTED
        ), "Contractor can only set status to CONFIRMED or REJECTED");
        OrderPlacementEntity placement = getOrderPlacementForOrganization(organizationIds, placementId);
        placement.setStatus(status);
        placement.setConfirmorId(principal.getId());
        placement.setConfirmedBy(principal.getName());
        final OrderPlacementEntity updated = orderPlacementRepository.update(placement);
        issueEventProducer.sendOrderPlacementStatusChangedByContractor(findIssue(placement.getIssueId()),
            OrderPlacementJson.valueOf(updated), principal);
        return updated;
    }

    private void withdrawOpenRequests(final IssueEntity issue, final List<QuotationRequestEntity> existingRequests,
        final ContractorJson contractor, final UserModel user) {
        existingRequests.stream()
            .filter(r -> contractor.getId().equals(r.getContractorId()))
            .filter(r -> OPEN_REQUEST_STATUSES.contains(r.getStatus()))
            .forEach(oldRequest -> {
                oldRequest.setStatus(RequestStatus.WITHDRAWN);
                quotationRequestRepository.update(oldRequest);
                issueEventProducer.sendQuotationRequestStatusChanged(issue,
                    QuotationRequestJson.valueOf(oldRequest), user);
            });
    }

    private QuotationRequestEntity mergeQuotationRequestFields(final QuotationRequestJson body,
        final QuotationRequestEntity entity) {
        if (body.getScopeOfWork() != null) {
            entity.setScopeOfWork(body.getScopeOfWork());
        }
        if (body.getStatus() != null) {
            if (body.getStatus() != RequestStatus.WITHDRAWN) {
                throw new BadRequestException("Manager can only set status to WITHDRAWN");
            }
            entity.setStatus(body.getStatus());
        }
        return entity;
    }

    private QuotationRequestKey requestKey(final UUID issueId, final UUID requestId) {
        QuotationRequestKey key = new QuotationRequestKey();
        key.setIssueId(issueId);
        key.setRequestId(requestId);
        return key;
    }

    private QuotationKey quotationKey(final UUID issueId, final UUID quotationId) {
        QuotationKey key = new QuotationKey();
        key.setIssueId(issueId);
        key.setQuotationId(quotationId);
        return key;
    }

    private QuotationEntity buildQuotation(final QuotationRequestEntity request, final QuotationJson body) {
        QuotationEntity quotation = new QuotationEntity();
        quotation.generateId();
        quotation.setIssueId(request.getIssueId());
        quotation.setRequestId(request.getRequestId());
        quotation.setProjectId(request.getProjectId());
        quotation.setProjectOwner(request.getProjectOwner());
        quotation.setProjectCareOf(request.getProjectCareOf());
        quotation.setProjectBillingAddress1(request.getProjectBillingAddress1());
        quotation.setProjectBillingAddress2(request.getProjectBillingAddress2());
        quotation.setProjectBillingAddress3(request.getProjectBillingAddress3());
        quotation.setOffererId(principal.getId());
        quotation.setOfferedBy(principal.getName());
        quotation.setContractorId(request.getContractorId());
        quotation.setContractorName(request.getContractorName());
        quotation.setOrganizationId(request.getOrganizationId());
        quotation.setValidUntil(body.getValidUntil());
        quotation.setStatus(body.getStatus() != null ? body.getStatus() : QuotationStatus.VALID);
        return quotation;
    }

    private OrderPlacementEntity buildOrderPlacement(final UUID issueId, final UUID quotationId,
        final QuotationEntity quotation) {
        OrderPlacementEntity orderPlacement = new OrderPlacementEntity();
        orderPlacement.generateId();
        orderPlacement.setIssueId(issueId);
        orderPlacement.setQuotationId(quotationId);
        orderPlacement.setProjectId(quotation.getProjectId());
        orderPlacement.setProjectOwner(quotation.getProjectOwner());
        orderPlacement.setProjectCareOf(quotation.getProjectCareOf());
        orderPlacement.setProjectBillingAddress1(quotation.getProjectBillingAddress1());
        orderPlacement.setProjectBillingAddress2(quotation.getProjectBillingAddress2());
        orderPlacement.setProjectBillingAddress3(quotation.getProjectBillingAddress3());
        orderPlacement.setOrdererId(principal.getId());
        orderPlacement.setOrderedBy(principal.getName());
        orderPlacement.setContractorId(quotation.getContractorId());
        orderPlacement.setContractorName(quotation.getContractorName());
        orderPlacement.setOrganizationId(quotation.getOrganizationId());
        orderPlacement.setStatus(OrderPlacementStatus.PLACED);
        return orderPlacement;
    }

    private <T> List<T> findAllByOrganizationIds(final Set<UUID> organizationIds,
        final Function<UUID, List<T>> finder) {
        return organizationIds.stream()
            .flatMap(orgId -> finder.apply(orgId).stream())
            .toList();
    }

    private <T> T findByOrganizationIds(final Set<UUID> organizationIds, final Function<UUID, List<T>> finder,
        final Predicate<T> matcher, final Supplier<? extends RuntimeException> notFound) {
        return findAllByOrganizationIds(organizationIds, finder).stream()
            .filter(matcher)
            .findFirst()
            .orElseThrow(notFound);
    }

    private <T> void validateAllowedStatus(final T status, final Set<T> allowedStatuses, final String message) {
        if (!allowedStatuses.contains(status)) {
            throw new BadRequestException(message);
        }
    }

}
