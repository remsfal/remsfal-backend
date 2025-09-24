package de.remsfal.service.entity.dto;

import de.remsfal.core.model.ContractorEmployeeModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dto.superclass.MetaDataEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * Entity class for a contractor employee.
 */
@Entity
@Table(name = "CONTRACTOR_EMPLOYEE")
@NamedQuery(name = "ContractorEmployeeEntity.findByContractorId",
    query = "SELECT e FROM ContractorEmployeeEntity e WHERE e.contractor.id = :contractorId")
public class ContractorEmployeeEntity extends MetaDataEntity implements ContractorEmployeeModel {

    @EmbeddedId
    private ContractorEmployeeKey id = new ContractorEmployeeKey();

    @ManyToOne
    @MapsId("contractorId")
    @JoinColumn(name = "CONTRACTOR_ID", columnDefinition = "uuid")
    private ContractorEntity contractor;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "USER_ID", columnDefinition = "uuid")
    private UserEntity user;

    @Column(name = "RESPONSIBILITY")
    private String responsibility;

    @Override
    public String getContractorId() {
        return contractor != null ? contractor.getId() : null;
    }

    @Override
    public String getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public String getResponsibility() {
        return responsibility;
    }

    @Override
    public UserModel getUser() {
        return user;
    }
}
