package de.remsfal.service.entity.dto;

import jakarta.persistence.*;

import java.util.Locale;
import java.util.Objects;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@NamedQuery(name = "AddressValidationEntity.findByZip",
        query = "SELECT a FROM AddressValidationEntity a WHERE a.zip = :zip")
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

    @Column(name = "COUNTRY", nullable = false)
    private Locale country;

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(final String zip) {
        this.zip = zip;
    }

    public Locale getCountry() {
        return country;
    }

    public void setCountry(final Locale country) {
        this.country = country;
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
