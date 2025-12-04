package de.remsfal.service.entity.dto;

import de.remsfal.core.model.OrganizationModel;
import de.remsfal.service.entity.dto.superclass.AbstractEntity;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "organization")
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
}
