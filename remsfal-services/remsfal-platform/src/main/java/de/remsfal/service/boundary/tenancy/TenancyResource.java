package de.remsfal.service.boundary.tenancy;

import java.util.List;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;

import de.remsfal.core.api.tenancy.TenancyEndpoint;
import de.remsfal.core.json.tenancy.TenancyJson;
import de.remsfal.core.json.tenancy.TenancyListJson;
import de.remsfal.core.model.project.TenancyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class TenancyResource extends AbstractTenancyResource implements TenancyEndpoint {

    @Context
    ResourceContext resourceContext;

    @Inject
    Instance<TaskResource> taskResource;

    @Override
    public TenancyListJson getTenancies() {
        final List<? extends TenancyModel> tenancies = tenancyController.getTenancies(principal);
        return TenancyListJson.valueOf(tenancies);
    }

    @Override
    public TenancyJson getTenancy(final String tenancyId) {
        final TenancyModel model = tenancyController.getTenancy(principal, tenancyId);
        return TenancyJson.valueOf(model);
    }

    @Override
    public TaskResource getTaskResource() {
        return resourceContext.initResource(taskResource.get());
    }

}