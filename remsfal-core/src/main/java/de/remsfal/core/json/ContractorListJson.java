package de.remsfal.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.remsfal.core.model.ContractorModel;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON representation of a list of contractors.
 */
@JsonInclude(Include.NON_NULL)
public class ContractorListJson {

    private List<ContractorJson> contractors;
    private Integer offset;
    private Long total;

    public List<ContractorJson> getContractors() {
        return contractors;
    }

    public void setContractors(List<ContractorJson> contractors) {
        this.contractors = contractors;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * Create a JSON representation from a list of models.
     *
     * @param models the list of models
     * @param offset the offset
     * @param total the total number of models
     * @return the JSON representation
     */
    public static ContractorListJson valueOf(List<? extends ContractorModel> models, Integer offset, Long total) {
        ContractorListJson json = new ContractorListJson();
        json.setOffset(offset);
        json.setTotal(total);

        List<ContractorJson> contractors = new ArrayList<>();
        for (ContractorModel model : models) {
            contractors.add(ContractorJson.valueOf(model));
        }
        json.setContractors(contractors);

        return json;
    }
}