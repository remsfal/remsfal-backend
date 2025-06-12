package de.remsfal.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.remsfal.core.model.ContractorEmployeeModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

/**
 * JSON representation of a contractor employee.
 */
@JsonInclude(Include.NON_NULL)
public class ContractorEmployeeJson implements ContractorEmployeeModel {

    protected String contractorId;
    protected String userId;
    protected String responsibility;

    public void setContractorId(String contractorId) {
        this.contractorId = contractorId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }

    @NotBlank(groups = PostValidation.class)
    @Email
    @Size(max = 255)
    private String email;

    private String name;

    private Boolean active;

    @Null
    @Override
    public String getContractorId() {
        return contractorId;
    }

    @NotNull(groups = PostValidation.class)
    @Null(groups = PatchValidation.class)
    @UUID
    @Override
    public String getUserId() {
        return userId;
    }

    @Size(max = 255)
    @Override
    public String getResponsibility() {
        return responsibility;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public UserModel getUser() {
        // Return a simple implementation of UserModel with the available user information
        return new UserModel() {
            @Override
            public String getId() {
                return userId;
            }

            @Override
            public String getEmail() {
                return email;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Boolean isActive() {
                return active;
            }
        };
    }

    /**
     * Create a JSON representation from a model.
     *
     * @param model the model
     * @return the JSON representation
     */
    public static ContractorEmployeeJson valueOf(ContractorEmployeeModel model) {
        if (model == null) {
            return null;
        }

        ContractorEmployeeJson json = new ContractorEmployeeJson();
        json.setContractorId(model.getContractorId());
        json.setUserId(model.getUserId());
        json.setResponsibility(model.getResponsibility());

        if (model.getUser() != null) {
            UserModel user = model.getUser();
            json.setEmail(user.getEmail());
            json.setName(user.getName());
            json.setActive(user.isActive());
        }

        return json;
    }
}
