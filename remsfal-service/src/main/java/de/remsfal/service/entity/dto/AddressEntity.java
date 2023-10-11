package de.remsfal.service.entity.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

import de.remsfal.core.model.AddressModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Entity
@Table(name = "ADDRESS")
public class AddressEntity extends AbstractEntity implements AddressModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    private String id;

    @Column(name = "STREET", nullable = false)
    private String street;

    @Column(name = "CITY", nullable = false)
    private String city;

    @Column(name = "PROVINCE", nullable = false)
    private String province;

    @Column(name = "ZIP", nullable = false)
    private String zip;

    @Column(name = "COUNTRY", nullable = false)
    private String country;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AddressEntity e) {
            return super.equals(e)
                && Objects.equals(id, e.id)
                && Objects.equals(street, e.street)
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
