package de.remsfal.service.entity.dto;

import java.util.Locale;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@NamedQuery(name = "AddressValidationEntity.findByZip",
    query = "SELECT a FROM AddressValidationEntity a WHERE a.zip = :zip")
@NamedQuery(name = "AddressValidationEntity.findByParameters",
    query = "SELECT a FROM AddressValidationEntity a WHERE " +
            "a.city = :city AND a.province = :province AND a.zip = :zip AND a.country = :country")
@Table(name = "ADDRESS_VALIDATION")
public class AddressValidationEntity {

    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "CITY", nullable = false)
    private String city;

    @Column(name = "PROVINCE", nullable = false)
    private String province;

    @Column(name = "ZIP", nullable = false)
    private String zip;

    @Column(name = "COUNTRY", columnDefinition = "char", nullable = false)
    private String country;

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getZip() {
        return zip;
    }

    public Locale getCountry() {
        return new Locale("", country);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AddressValidationEntity e) {
            return Objects.equals(id, e.id)
                && Objects.equals(city, e.city)
                && Objects.equals(province, e.province)
                && Objects.equals(zip, e.zip)
                && Objects.equals(country, e.country);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
