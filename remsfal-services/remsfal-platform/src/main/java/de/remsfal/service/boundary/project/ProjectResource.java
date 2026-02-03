package de.remsfal.service.boundary.project;

import java.util.List;
import java.util.UUID;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.MetricUnits;

import de.remsfal.core.api.project.ProjectEndpoint;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.json.project.ProjectListJson;
import de.remsfal.core.model.project.ProjectModel;
import de.remsfal.service.control.ProjectController;

import org.eclipse.microprofile.metrics.annotation.Timed;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
public class ProjectResource extends AbstractProjectResource implements ProjectEndpoint {

    @Inject
    ProjectController controller;

    @Inject
    Instance<MemberResource> memberResource;

    @Inject
    Instance<ProjectOrganizationResource> projectOrganizationResource;

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

    @Inject
    Instance<TenantResource> tenantResource;
  
    @Inject
    Instance<ProjectTenancyResource> tenancyResource;

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
        return getCreatedResponseBuilder(model.getId())
            .type(MediaType.APPLICATION_JSON)
            .entity(ProjectJson.valueOf(model))
            .build();
    }

    @Override
    @Timed(name = "GetSingleProjectTimer", unit = MetricUnits.MILLISECONDS)
    public ProjectJson getProject(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        final ProjectModel model = controller.getProject(principal, projectId);
        return ProjectJson.valueOf(model);
    }

    @Override
    @Timed(name = "UpdateProjectTimer", unit = MetricUnits.MILLISECONDS)
    public ProjectJson updateProject(final UUID projectId, final ProjectJson project) {
        checkOwnerPermissions(projectId);
        final ProjectModel model = controller.updateProject(principal, projectId, project);
        return ProjectJson.valueOf(model);
    }

    @Override
    @Timed(name = "deleteProjectTimer", unit = MetricUnits.MILLISECONDS)
    public void deleteProject(final UUID projectId) {
        checkOwnerPermissions(projectId);
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

    @Override
    public ApartmentResource getApartmentResource() {
        return resourceContext.initResource(apartmentResource.get());
    }

    @Override
    public CommercialResource getCommercialResource() {
        return resourceContext.initResource(commercialResource.get());
    }

    @Override
    public StorageResource getStorageResource() {
        return resourceContext.initResource(storageResource.get());
    }

    @Override
    public ContractorResource getContractorResource() {
        return resourceContext.initResource(contractorResource.get());
    }

    @Override
    public TenantResource getTenantResource() {
        return resourceContext.initResource(tenantResource.get());
    }

    @Override
    public ProjectTenancyResource getTenancyResource() {
        return resourceContext.initResource(tenancyResource.get());
    }

    @Override
    public ProjectOrganizationResource getProjectOrganizationResource() {
        return resourceContext.initResource(projectOrganizationResource.get());
    }
}
