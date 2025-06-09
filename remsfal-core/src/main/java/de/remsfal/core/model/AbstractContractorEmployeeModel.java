package de.remsfal.core.model;

/**
 * Abstract base class for ContractorEmployeeModel implementations.
 * This class provides common fields and methods to reduce code duplication.
 */
public abstract class AbstractContractorEmployeeModel implements ContractorEmployeeModel {

    protected String contractorId;
    protected String userId;
    protected String responsibility;

    @Override
    public String getContractorId() {
        return contractorId;
    }

    public void setContractorId(String contractorId) {
        this.contractorId = contractorId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getResponsibility() {
        return responsibility;
    }

    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }
}