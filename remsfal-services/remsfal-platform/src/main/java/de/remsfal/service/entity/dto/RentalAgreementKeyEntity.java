package de.remsfal.service.entity.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import de.remsfal.core.model.project.RentalAgreementKeyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Embeddable
public class RentalAgreementKeyEntity implements RentalAgreementKeyModel, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "amount_of_keys", nullable = false)
    private int amountOfKeys;

    @Column(name = "issued_at", columnDefinition = "date")
    private LocalDate issuedAt;

    @Column(name = "returned_at", columnDefinition = "date")
    private LocalDate returnedAt;

    @Column(name = "key_type")
    private String keyType;

    @Override
    public Integer getAmountOfKeys() {
        return amountOfKeys;
    }

    public void setAmountOfKeys(final int amountOfKeys) {
        this.amountOfKeys = amountOfKeys;
    }

    @Override
    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(final LocalDate issuedAt) {
        this.issuedAt = issuedAt;
    }

    @Override
    public LocalDate getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(final LocalDate returnedAt) {
        this.returnedAt = returnedAt;
    }

    @Override
    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(final String keyType) {
        this.keyType = keyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amountOfKeys, issuedAt, returnedAt, keyType);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof RentalAgreementKeyEntity e) {
            return amountOfKeys == e.amountOfKeys
                && Objects.equals(issuedAt, e.issuedAt)
                && Objects.equals(returnedAt, e.returnedAt)
                && Objects.equals(keyType, e.keyType);
        }
        return false;
    }

}
