package de.remsfal.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.remsfal.core.model.ContractorModel;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * JSON representation of a contractor.
 */
@JsonInclude(Include.NON_NULL)
public class ContractorJson implements ContractorModel {

    @Null(groups = PostValidation.class)
    @NotNull(groups = PatchValidation.class)
    @UUID
    private String id;

    @Null
    private String projectId;

    @NotBlank(groups = PostValidation.class)
    @Size(max = 255)
    private String companyName;

    @Pattern(regexp = "^\\+?[0-9]{10,14}$", message = "Phone number must be in E.164 format")
    @Size(max = 15)
    private String phone;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String trade;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Override
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    /**
     * Create a JSON representation from a model.
     *
     * @param model the model
     * @return the JSON representation
     */
    public static ContractorJson valueOf(ContractorModel model) {
        if (model == null) {
            return null;
        }

        ContractorJson json = new ContractorJson();
        json.setId(model.getId());
        json.setProjectId(model.getProjectId());
        json.setCompanyName(model.getCompanyName());
        json.setPhone(model.getPhone());
        json.setEmail(model.getEmail());
        json.setTrade(model.getTrade());
        return json;
    }
}