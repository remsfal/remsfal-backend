package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.ProjectOrganizationEndpoint;
import de.remsfal.core.json.project.ProjectOrganizationJson;
import de.remsfal.core.json.project.ProjectOrganizationListJson;
import de.remsfal.core.model.project.ProjectOrganizationModel;
import jakarta.enterprise.context.RequestScoped;

import java.util.Set;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class ProjectOrganizationResource extends ProjectSubResource implements ProjectOrganizationEndpoint {

    @Override
    public ProjectOrganizationListJson getProjectOrganizations(final UUID projectId) {
        checkReadPermissions(projectId);
        final Set<? extends ProjectOrganizationModel> model = projectController.getProjectOrganizations(principal, projectId);
        return ProjectOrganizationListJson.valueOfSet(model);
    }

    @Override
    public ProjectOrganizationJson addProjectOrganization(final UUID projectId, final ProjectOrganizationJson organization) {
        checkWritePermissions(projectId);
        final ProjectOrganizationModel model = projectController.addProjectOrganization(principal, projectId, organization);
        return ProjectOrganizationJson.valueOf(model);
    }

    @Override
    public ProjectOrganizationJson updateProjectOrganization(final UUID projectId, final UUID organizationId,
            final ProjectOrganizationJson organization) {
        checkWritePermissions(projectId);
        final ProjectOrganizationModel model = projectController.changeProjectOrganizationRole(projectId,
            organizationId, organization.getRole());
        return ProjectOrganizationJson.valueOf(model);
    }

    @Override
    public void deleteProjectOrganization(final UUID projectId, final UUID organizationId) {
        checkWritePermissions(projectId);
        projectController.removeProjectOrganization(projectId, organizationId);
    }

}
