package de.remsfal.service.entity.dto;

import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.OrganizationModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dto.superclass.AbstractEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

@Entity
@Table(name = "organization")
@NamedQuery(name = "findAll", query = "select o from OrganizationEntity o")
public class OrganizationEntity extends AbstractEntity implements OrganizationModel {

    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "trade")
    private String trade;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "address_id", columnDefinition = "uuid")
    private AddressEntity address;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<OrganizationEmployeeEntity> employees;

    //Getter and Setter
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getTrade() {
        return trade;
    }
    public void setTrade(String trade) {
        this.trade = trade;
    }

    public AddressEntity getAddress() {
        return address;
    }
    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    public Set<OrganizationEmployeeEntity> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<OrganizationEmployeeEntity> employees) {
        this.employees = employees;
    }

    public void addEmployee(UserEntity user, EmployeeRole role) {
        if (this.employees == null) {
            this.employees = new HashSet<>();
        }
        OrganizationEmployeeEntity organizationEmployeeEntity = new OrganizationEmployeeEntity();
        organizationEmployeeEntity.setUser(user);
        organizationEmployeeEntity.setRole(role);
        organizationEmployeeEntity.setOrganization(this);

        this.employees.add(organizationEmployeeEntity);
    }

    public boolean isEmployee(final UserModel  user) {
        for (OrganizationEmployeeEntity employee : employees) {
            if (employee.getUser().getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof OrganizationEntity e) {
            return super.equals(e)
                    && Objects.equals(name, e.name)
                    && Objects.equals(employees, e.employees);
        }
        return false;
    }
}
