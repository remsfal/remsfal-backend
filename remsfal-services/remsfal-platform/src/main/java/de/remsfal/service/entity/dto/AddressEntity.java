package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Locale;
import java.util.Objects;

import de.remsfal.core.model.AddressModel;
import de.remsfal.service.entity.dto.superclass.AbstractEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "ADDRESS")
public class AddressEntity extends AbstractEntity implements AddressModel {

    @Column(name = "STREET", nullable = false)
    private String street;

    @Column(name = "CITY", nullable = false)
    private String city;

    @Column(name = "PROVINCE", nullable = false)
    private String province;

    @Column(name = "ZIP", nullable = false)
    private String zip;

    @Column(name = "COUNTRY", columnDefinition = "char", nullable = false)
    private String country;

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

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
        return new Locale("", country);
    }

    public void setCountry(final Locale country) {
        this.country = country.getCountry();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AddressEntity e) {
            return super.equals(e)
                && Objects.equals(street, e.street)
                && Objects.equals(city, e.city)
                && Objects.equals(province, e.province)
                && Objects.equals(zip, e.zip)
                && Objects.equals(country, e.country);
        }
        return false;
    }

    public String toString() {
        return String.format("%s, %s %s, %s %s", street, zip, city, country, province);
    }

}
