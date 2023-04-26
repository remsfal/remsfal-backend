package de.remsfal.core.dto;

import java.time.LocalDate;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.validation.constraints.Email;
import javax.validation.constraints.Null;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class UserJson implements UserModel {

    @Null
    private String id;

    @JsonbProperty("user_name")
    @Size(min = 3, max = 99, message = "The name must be between 3 and 99 characters")
    private String name;

    @JsonbProperty("user_email")
    @Size(max = 255, message = "The email cannot be longer than 255 characters")
    @Email(message = "Email should be valid")
    private String email;

    @JsonbProperty("registered_date")
    @JsonbDateFormat("dd-MM-yyyy")
    @PastOrPresent
    private LocalDate registeredDate;

    @JsonbTransient
    private Integer age;
    
    public UserJson() {
        // do nothing
    }

    public UserJson(UserModel user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(final LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

}
