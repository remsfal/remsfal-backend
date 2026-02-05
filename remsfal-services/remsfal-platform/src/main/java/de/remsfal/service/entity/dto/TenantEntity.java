package de.remsfal.service.entity.dto;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import de.remsfal.core.model.project.TenantModel;
import de.remsfal.service.entity.dto.superclass.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Entity representing a tenant in a rental agreement.
 * Tenants are independent entities with optional linkage to user accounts.
 * Tenant data takes precedence over user data (fallback pattern).
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "tenants")
public class TenantEntity extends AbstractEntity implements TenantModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id", nullable = false, columnDefinition = "uuid")
    private RentalAgreementEntity agreement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "uuid")
    private UserEntity user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile_phone_number", length = 15)
    private String mobilePhoneNumber;

    @Column(name = "business_phone_number", length = 15)
    private String businessPhoneNumber;

    @Column(name = "private_phone_number", length = 15)
    private String privatePhoneNumber;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", columnDefinition = "uuid")
    private AddressEntity address;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "date_of_birth", columnDefinition = "date")
    private LocalDate dateOfBirth;

    public RentalAgreementEntity getAgreement() {
        return agreement;
    }

    public void setAgreement(RentalAgreementEntity agreement) {
        this.agreement = agreement;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * Returns first name with fallback to user's first name.
     */
    @Override
    public String getFirstName() {
        if (firstName != null) {
            return firstName;
        }
        return user != null ? user.getFirstName() : null;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns last name with fallback to user's last name.
     */
    @Override
    public String getLastName() {
        if (lastName != null) {
            return lastName;
        }
        return user != null ? user.getLastName() : null;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns email with fallback to user's email.
     */
    @Override
    public String getEmail() {
        if (email != null) {
            return email;
        }
        return user != null ? user.getEmail() : null;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns mobile phone number with fallback to user's mobile phone.
     */
    @Override
    public String getMobilePhoneNumber() {
        if (mobilePhoneNumber != null) {
            return mobilePhoneNumber;
        }
        return user != null ? user.getMobilePhoneNumber() : null;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    /**
     * Returns business phone number with fallback to user's business phone.
     */
    @Override
    public String getBusinessPhoneNumber() {
        if (businessPhoneNumber != null) {
            return businessPhoneNumber;
        }
        return user != null ? user.getBusinessPhoneNumber() : null;
    }

    public void setBusinessPhoneNumber(String businessPhoneNumber) {
        this.businessPhoneNumber = businessPhoneNumber;
    }

    /**
     * Returns private phone number with fallback to user's private phone.
     */
    @Override
    public String getPrivatePhoneNumber() {
        if (privatePhoneNumber != null) {
            return privatePhoneNumber;
        }
        return user != null ? user.getPrivatePhoneNumber() : null;
    }

    public void setPrivatePhoneNumber(String privatePhoneNumber) {
        this.privatePhoneNumber = privatePhoneNumber;
    }

    /**
     * Returns address with fallback to user's address.
     */
    @Override
    public AddressEntity getAddress() {
        if (address != null) {
            return address;
        }
        return user != null ? user.getAddress() : null;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    @Override
    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    @Override
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TenantEntity e) {
            return super.equals(e)
                && Objects.equals(agreement != null ? agreement.getId() : null,
                                  e.agreement != null ? e.agreement.getId() : null)
                && Objects.equals(user != null ? user.getId() : null,
                                  e.user != null ? e.user.getId() : null);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                           agreement != null ? agreement.getId() : null,
                           user != null ? user.getId() : null);
    }
}
