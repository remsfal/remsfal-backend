package de.remsfal.service.boundary;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.jboss.logging.Logger;

import de.remsfal.core.api.ProjectEndpoint;
import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.json.ProjectJson;
import de.remsfal.core.json.ProjectListJson;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.service.boundary.project.ApartmentResource;
import de.remsfal.service.boundary.project.BuildingResource;
import de.remsfal.service.boundary.project.CommercialResource;
import de.remsfal.service.boundary.project.ContractorResource;
import de.remsfal.service.boundary.project.StorageResource;
import de.remsfal.service.boundary.project.MemberResource;
import de.remsfal.service.boundary.project.PropertyResource;
import de.remsfal.service.boundary.project.SiteResource;

import de.remsfal.service.control.ProjectController;

import org.eclipse.microprofile.metrics.annotation.Timed;
/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
public class ProjectResource implements ProjectEndpoint {

    @Context
    UriInfo uri;

    @Context
    ResourceContext resourceContext;

    @Inject
    RemsfalPrincipal principal;

    @Inject
    Logger logger;

    @Inject
    ProjectController controller;

    @Inject
    Instance<MemberResource> memberResource;

    @Inject
    Instance<PropertyResource> propertyResource;

    @Inject
    Instance<SiteResource> siteResource;

    @Inject
    Instance<BuildingResource> buildingResource;

    @Inject
    Instance<ApartmentResource> apartmentResource;

    @Inject
    Instance<CommercialResource> commercialResource;

    @Inject
    Instance<StorageResource> storageResource;

    @Inject
    Instance<ContractorResource> contractorResource;

    @Override
    @Timed(name = "GetProjectsListTimer", unit = MetricUnits.MILLISECONDS)
    public ProjectListJson getProjects(final Integer offset, final Integer limit) {
        List<ProjectModel> projects = controller.getProjects(principal, offset, limit);
        return ProjectListJson.valueOf(projects, offset, controller.countProjects(principal), principal);
    }

    @Override
    @Timed(name = "CreateProjectTimer", unit = MetricUnits.MILLISECONDS)
    public Response createProject(final ProjectJson project) {
        final ProjectModel model = controller.createProject(principal, project);
        final URI location = uri.getAbsolutePathBuilder().path(model.getId().toString()).build();
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(ProjectJson.valueOf(model))
            .build();
    }

    @Override
    @Timed(name = "GetSingleProjectTimer", unit = MetricUnits.MILLISECONDS)
    public ProjectJson getProject(final UUID projectId) {
        final ProjectModel model = controller.getProject(principal, projectId);
        return ProjectJson.valueOf(model);
    }

    @Override
    @Timed(name = "UpdateProjectTimer", unit = MetricUnits.MILLISECONDS)
    public ProjectJson updateProject(final UUID projectId, final ProjectJson project) {
        final ProjectModel model = controller.updateProject(principal, projectId, project);
        return ProjectJson.valueOf(model);
    }

    @Override
    @Timed(name = "deleteProjectTimer", unit = MetricUnits.MILLISECONDS)
    public void deleteProject(final UUID projectId) {
        controller.deleteProject(principal, projectId);
    }

    @Override
    public MemberResource getMemberResource() {
        return resourceContext.initResource(memberResource.get());
    }

    @Override
    public PropertyResource getPropertyResource() {
        return resourceContext.initResource(propertyResource.get());
    }

    @Override
    public SiteResource getSiteResource() {
        return resourceContext.initResource(siteResource.get());
    }

    @Override
    public BuildingResource getBuildingResource() {
        return resourceContext.initResource(buildingResource.get());
    }

    private <T> T init(Instance<T> resource) {
        return resourceContext.initResource(resource.get());
    }

    @Override
    public ContractorResource getContractorResource() {
        return resourceContext.initResource(contractorResource.get());
    }

}
