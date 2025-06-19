package de.remsfal.service.entity.dto;

import de.remsfal.core.model.ContractorModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity class for a contractor.
 */
@Entity
@Table(name = "CONTRACTOR")
@NamedQuery(name = "ContractorEntity.findByProjectId",
        query = "SELECT c FROM ContractorEntity c WHERE c.project.id = :projectId")
@NamedQuery(name = "ContractorEntity.countByProjectId",
        query = "SELECT count(c) FROM ContractorEntity c WHERE c.project.id = :projectId")
public class ContractorEntity extends AbstractEntity implements ContractorModel {

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", columnDefinition = "char")
    private ProjectEntity project;

    @Column(name = "COMPANY_NAME", nullable = false)
    private String companyName;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TRADE")
    private String trade;

    @OneToMany(mappedBy = "contractor", fetch = FetchType.EAGER)
    private Set<ContractorEmployeeEntity> employees;

    @Override
    public String getProjectId() {
        return project != null ? project.getId() : null;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    @Override
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public Set<ContractorEmployeeEntity> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<ContractorEmployeeEntity> employees) {
        this.employees = employees;
    }

    public void addEmployee(UserEntity userEntity, String responsibility) {
        if (employees == null) {
            employees = new HashSet<>();
        }
        ContractorEmployeeEntity employee = new ContractorEmployeeEntity();
        employee.setContractor(this);
        employee.setUser(userEntity);
        employee.setResponsibility(responsibility);
        this.employees.add(employee);
    }
}