package de.remsfal.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.remsfal.core.model.OrganizationModel;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class OrganizationListJson {

    List<OrganizationJson> organizations;
    Integer offset;
    Long total;

    //Getter and Setter

    public List<OrganizationJson> getOrganizations() {
        return organizations;
    }
    public void setOrganizations(List<OrganizationJson> organizations) {
        this.organizations = organizations;
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

    public static OrganizationListJson valueOf(List<? extends OrganizationModel> organizations, Integer offset,
        Long total) {
        OrganizationListJson organizationListJson = new OrganizationListJson();

        organizationListJson.setOffset(offset);
        organizationListJson.setTotal(total);

        List<OrganizationJson> organizationList = new ArrayList<>();
        for (OrganizationModel organization : organizations) {
            organizationList.add(OrganizationJson.valueOf(organization));
        }
        organizationListJson.setOrganizations(organizationList);

        return organizationListJson;
    }

    public static OrganizationListJson valueOf(List<? extends OrganizationModel> organizations) {
        OrganizationListJson organizationListJson = new OrganizationListJson();

        organizationListJson.setOffset(0);
        organizationListJson.setTotal((long) organizations.size());

        List<OrganizationJson> organizationList = new ArrayList<>();
        for (OrganizationModel organization : organizations) {
            organizationList.add(OrganizationJson.valueOf(organization));
        }
        organizationListJson.setOrganizations(organizationList);

        return organizationListJson;
    }
}
