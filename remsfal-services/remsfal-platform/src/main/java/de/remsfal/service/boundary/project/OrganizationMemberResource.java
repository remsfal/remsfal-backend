package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.OrganizationMemberEndpoint;
import de.remsfal.core.json.project.ProjectMemberJson;
import de.remsfal.core.json.project.OrganizationMemberJson;
import de.remsfal.core.json.project.OrganizationMemberListJson;
import de.remsfal.core.model.project.OrganizationMemberModel;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.service.control.ProjectController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class OrganizationMemberResource extends AbstractProjectResource implements OrganizationMemberEndpoint {

    @Inject
    ProjectController controller;

    @Override
    public OrganizationMemberListJson getProjectOrganizations(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        final Set<? extends OrganizationMemberModel> organizations =
            controller.getProjectOrganizations(principal, projectId);
        final List<OrganizationMemberJson> result = organizations.stream()
            .map(org -> OrganizationMemberJson.valueOf(org,
                controller.getOrganizationMembers(org.getOrganizationId(), org.getRole()).stream()
                    .map(ProjectMemberJson::valueOf)
                    .collect(Collectors.toList())))
            .collect(Collectors.toList());
        return OrganizationMemberListJson.valueOf(result);
    }

    @Override
    public OrganizationMemberJson addProjectOrganization(final UUID projectId,
            final OrganizationMemberJson organization) {
        checkPropertyWritePermissions(projectId);
        final OrganizationMemberModel model =
            controller.addProjectOrganization(principal, projectId, organization);
        return OrganizationMemberJson.valueOf(model);
    }

    @Override
    public OrganizationMemberJson updateProjectOrganization(final UUID projectId, final UUID organizationId,
            final OrganizationMemberJson organization) {
        checkPropertyWritePermissions(projectId);
        if (MemberRole.PROPRIETOR == organization.getRole()
            && principal.getOrganizationRole(organizationId) != null) {
            throw new ForbiddenException("Nobody can upgrade their own role");
        }
        final OrganizationMemberModel model = controller.changeProjectOrganizationRole(projectId,
            organizationId, organization.getRole());
        return OrganizationMemberJson.valueOf(model);
    }

    @Override
    public void deleteProjectOrganization(final UUID projectId, final UUID organizationId) {
        checkPropertyWritePermissions(projectId);
        if (controller.getProjectOrganizationRole(organizationId, projectId) == MemberRole.PROPRIETOR
            && principal.getProjectRole(projectId) != MemberRole.PROPRIETOR) {
            throw new ForbiddenException("Only a proprietor can remove a proprietor from a project");
        }
        controller.removeProjectOrganization(projectId, organizationId);
    }

}
