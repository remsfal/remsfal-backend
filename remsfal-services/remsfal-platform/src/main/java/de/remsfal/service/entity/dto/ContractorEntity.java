package de.remsfal.service.entity.dto;

import de.remsfal.core.model.ContractorModel;
import de.remsfal.service.entity.dto.superclass.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity class for a contractor. Contractors are independent entities with optional linkage to
 * organizations. Contractor data takes precedence over organization data (fallback pattern) —
 * except for {@code email}, which has no fallback since it is the key used to establish/validate
 * the organization link.
 */
@Entity
@Table(name = "contractors")
@NamedQuery(name = "ContractorEntity.findByProjectId",
    query = "SELECT c FROM ContractorEntity c WHERE c.project.id = :projectId")
@NamedQuery(name = "ContractorEntity.countByProjectId",
    query = "SELECT count(c) FROM ContractorEntity c WHERE c.project.id = :projectId")
public class ContractorEntity extends AbstractEntity implements ContractorModel {

    @ManyToOne
    @JoinColumn(name = "project_id", columnDefinition = "uuid")
    private ProjectEntity project;

    @ManyToOne
    @JoinColumn(name = "organization_id", columnDefinition = "uuid")
    private OrganizationEntity organization;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "trade")
    private String trade;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "address_id", columnDefinition = "uuid")
    private AddressEntity address;

    @Override
    public UUID getProjectId() {
        return project != null ? project.getId() : null;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    @Override
    public UUID getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
    }

    /**
     * Returns the name with fallback to the linked organization's name.
     */
    @Override
    public String getName() {
        if (name != null) {
            return name;
        }
        return organization != null ? organization.getName() : null;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the phone number with fallback to the linked organization's phone number.
     */
    @Override
    public String getPhone() {
        if (phone != null) {
            return phone;
        }
        return organization != null ? organization.getPhone() : null;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the contractor's own email. Unlike the other fields, this has no fallback to the linked
     * organization's email: the email is the key used to establish/validate the organization link, so
     * it must always reflect the contractor's own stored value.
     */
    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    /**
     * Returns the trade with fallback to the linked organization's trade.
     */
    @Override
    public String getTrade() {
        if (trade != null) {
            return trade;
        }
        return organization != null ? organization.getTrade() : null;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    @Override
    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    @Override
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Returns the address with fallback to the linked organization's address.
     */
    @Override
    public AddressEntity getAddress() {
        if (address != null) {
            return address;
        }
        return organization != null ? organization.getAddress() : null;
    }

    public void setAddress(final AddressEntity address) {
        this.address = address;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ContractorEntity e) {
            return super.equals(e)
                && Objects.equals(project, e.project)
                && Objects.equals(organization, e.organization)
                && Objects.equals(name, e.name)
                && Objects.equals(phone, e.phone)
                && Objects.equals(email, e.email)
                && Objects.equals(trade, e.trade)
                && Objects.equals(contactPerson, e.contactPerson)
                && Objects.equals(remarks, e.remarks)
                && Objects.equals(address, e.address);
        }
        return false;
    }
}
