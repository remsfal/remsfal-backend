package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.ProjectOrganizationEndpoint;
import de.remsfal.core.json.project.ProjectOrganizationJson;
import de.remsfal.core.json.project.ProjectOrganizationListJson;
import de.remsfal.core.model.project.ProjectOrganizationModel;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.service.control.ProjectController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import java.util.Set;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class ProjectOrganizationResource extends AbstractProjectResource implements ProjectOrganizationEndpoint {

    @Inject
    ProjectController controller;

    @Override
    public ProjectOrganizationListJson getProjectOrganizations(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        final Set<? extends ProjectOrganizationModel> model =
            controller.getProjectOrganizations(principal, projectId);
        return ProjectOrganizationListJson.valueOfSet(model);
    }

    @Override
    public ProjectOrganizationJson addProjectOrganization(final UUID projectId,
            final ProjectOrganizationJson organization) {
        checkPropertyWritePermissions(projectId);
        final ProjectOrganizationModel model =
            controller.addProjectOrganization(principal, projectId, organization);
        return ProjectOrganizationJson.valueOf(model);
    }

    @Override
    public ProjectOrganizationJson updateProjectOrganization(final UUID projectId, final UUID organizationId,
            final ProjectOrganizationJson organization) {
        checkPropertyWritePermissions(projectId);
        if(MemberRole.PROPRIETOR == organization.getRole()
            && principal.getOrganizationRole(organizationId) != null) {
            throw new ForbiddenException("Nobody can upgrade their own role");
        }
        final ProjectOrganizationModel model = controller.changeProjectOrganizationRole(projectId,
            organizationId, organization.getRole());
        return ProjectOrganizationJson.valueOf(model);
    }

    @Override
    public void deleteProjectOrganization(final UUID projectId, final UUID organizationId) {
        checkPropertyWritePermissions(projectId);
        if(controller.getProjectOrganizationRole(organizationId, projectId) == MemberRole.PROPRIETOR
            && principal.getProjectRole(projectId) != MemberRole.PROPRIETOR) {
            throw new ForbiddenException("Only a proprietor can remove a proprietor from a project");
        }
        controller.removeProjectOrganization(projectId, organizationId);
    }

}
