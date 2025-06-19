package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for ContractorEmployeeEntity.
 */
@Embeddable
public class ContractorEmployeeKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "CONTRACTOR_ID", columnDefinition = "char", length = 36)
    private String contractorId;

    @Column(name = "USER_ID", columnDefinition = "char", length = 36)
    private String userId;

    public String getContractorId() {
        return contractorId;
    }

    public void setContractorId(String contractorId) {
        this.contractorId = contractorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContractorEmployeeKey that = (ContractorEmployeeKey) o;
        return Objects.equals(contractorId, that.contractorId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractorId, userId);
    }
}