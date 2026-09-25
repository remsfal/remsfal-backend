package de.remsfal.ticketing.entity.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.remsfal.common.util.UUIDv7;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.QuotationRequestModel;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity("quotation_requests")
public class QuotationRequestEntity extends AbstractEntity implements QuotationRequestModel {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Id
    private QuotationRequestKey key;

    @Column("project_id")
    private UUID projectId;

    @Column("project_owner")
    private String projectOwner;

    @Column("project_care_of")
    private String projectCareOf;

    @Column("project_billing_address_1")
    private String projectBillingAddress1;

    @Column("project_billing_address_2")
    private String projectBillingAddress2;

    @Column("project_billing_address_3")
    private String projectBillingAddress3;

    @Column("initiator_id")
    private UUID initiatorId;

    @Column("initiated_by")
    private String initiatedBy;

    @Column("contractor_id")
    private UUID contractorId;

    @Column("contractor_name")
    private String contractorName;

    @Column("organization_id")
    private UUID organizationId;

    @Column("status")
    private String status;

    @Column("scope_of_work")
    private String scopeOfWork;

    @Column("place_of_performance_address_1")
    private String placeOfPerformanceAddress1;

    @Column("place_of_performance_address_2")
    private String placeOfPerformanceAddress2;

    @Column("place_of_performance_address_3")
    private String placeOfPerformanceAddress3;

    @Column("rental_unit_type")
    private String rentalUnitType;

    @Column("rental_unit_title")
    private String rentalUnitTitle;

    @Column("rental_unit_location")
    private String rentalUnitLocation;

    @Column("tenants")
    private List<String> tenantsJson;

    @Override
    public UUID getId() {
        return getRequestId();
    }

    public QuotationRequestKey getKey() {
        return key;
    }

    public void setKey(QuotationRequestKey key) {
        this.key = key;
    }

    public UUID getIssueId() {
        return Optional.ofNullable(key)
            .map(QuotationRequestKey::getIssueId)
            .orElse(null);
    }

    public void setIssueId(UUID issueId) {
        if (this.key == null) {
            this.key = new QuotationRequestKey();
        }
        this.key.setIssueId(issueId);
    }

    public UUID getRequestId() {
        return Optional.ofNullable(key)
            .map(QuotationRequestKey::getRequestId)
            .orElse(null);
    }

    public void generateId() {
        if (this.key == null) {
            this.key = new QuotationRequestKey();
        }
        if (this.key.getRequestId() == null) {
            this.key.setRequestId(UUIDv7.randomUUID());
        }
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public String getProjectOwner() {
        return projectOwner;
    }

    public void setProjectOwner(String projectOwner) {
        this.projectOwner = projectOwner;
    }

    @Override
    public String getProjectCareOf() {
        return projectCareOf;
    }

    public void setProjectCareOf(String projectCareOf) {
        this.projectCareOf = projectCareOf;
    }

    @Override
    public String getProjectBillingAddress1() {
        return projectBillingAddress1;
    }

    public void setProjectBillingAddress1(String projectBillingAddress1) {
        this.projectBillingAddress1 = projectBillingAddress1;
    }

    @Override
    public String getProjectBillingAddress2() {
        return projectBillingAddress2;
    }

    public void setProjectBillingAddress2(String projectBillingAddress2) {
        this.projectBillingAddress2 = projectBillingAddress2;
    }

    @Override
    public String getProjectBillingAddress3() {
        return projectBillingAddress3;
    }

    public void setProjectBillingAddress3(String projectBillingAddress3) {
        this.projectBillingAddress3 = projectBillingAddress3;
    }

    @Override
    public UUID getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(UUID initiatorId) {
        this.initiatorId = initiatorId;
    }

    @Override
    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public UUID getContractorId() {
        return contractorId;
    }

    public void setContractorId(UUID contractorId) {
        this.contractorId = contractorId;
    }

    @Override
    public String getContractorName() {
        return contractorName;
    }

    public void setContractorName(final String contractorName) {
        this.contractorName = contractorName;
    }

    @Override
    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public RequestStatus getStatus() {
        return status != null ? RequestStatus.valueOf(status) : null;
    }

    public void setStatus(RequestStatus status) {
        this.status = status != null ? status.name() : null;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScopeOfWork() {
        return scopeOfWork;
    }

    public void setScopeOfWork(String scopeOfWork) {
        this.scopeOfWork = scopeOfWork;
    }

    @Override
    public String getPlaceOfPerformanceAddress1() {
        return placeOfPerformanceAddress1;
    }

    public void setPlaceOfPerformanceAddress1(String placeOfPerformanceAddress1) {
        this.placeOfPerformanceAddress1 = placeOfPerformanceAddress1;
    }

    @Override
    public String getPlaceOfPerformanceAddress2() {
        return placeOfPerformanceAddress2;
    }

    public void setPlaceOfPerformanceAddress2(String placeOfPerformanceAddress2) {
        this.placeOfPerformanceAddress2 = placeOfPerformanceAddress2;
    }

    @Override
    public String getPlaceOfPerformanceAddress3() {
        return placeOfPerformanceAddress3;
    }

    public void setPlaceOfPerformanceAddress3(String placeOfPerformanceAddress3) {
        this.placeOfPerformanceAddress3 = placeOfPerformanceAddress3;
    }

    @Override
    public UnitType getRentalUnitType() {
        return rentalUnitType != null ? UnitType.valueOf(rentalUnitType) : null;
    }

    public void setRentalUnitType(UnitType rentalUnitType) {
        this.rentalUnitType = rentalUnitType != null ? rentalUnitType.name() : null;
    }

    public void setRentalUnitType(String rentalUnitType) {
        this.rentalUnitType = rentalUnitType;
    }

    @Override
    public String getRentalUnitTitle() {
        return rentalUnitTitle;
    }

    public void setRentalUnitTitle(String rentalUnitTitle) {
        this.rentalUnitTitle = rentalUnitTitle;
    }

    @Override
    public String getRentalUnitLocation() {
        return rentalUnitLocation;
    }

    public void setRentalUnitLocation(String rentalUnitLocation) {
        this.rentalUnitLocation = rentalUnitLocation;
    }

    public List<String> getTenantsJson() {
        return tenantsJson;
    }

    public void setTenantsJson(final List<String> tenantsJson) {
        this.tenantsJson = tenantsJson;
    }

    @Override
    public List<UserJson> getTenants() {
        if (tenantsJson == null) {
            return null;
        }
        try {
            final List<UserJson> result = new ArrayList<>(tenantsJson.size());
            for (final String tenant : tenantsJson) {
                result.add(OBJECT_MAPPER.readValue(tenant, UserJson.class));
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid JSON stored in tenants column", e);
        }
    }

    public void setTenants(final List<? extends UserJson> tenants) {
        if (tenants == null) {
            this.tenantsJson = null;
            return;
        }
        try {
            final List<String> result = new ArrayList<>(tenants.size());
            for (final UserJson tenant : tenants) {
                result.add(OBJECT_MAPPER.writeValueAsString(tenant));
            }
            this.tenantsJson = result;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize tenants", e);
        }
    }

}
