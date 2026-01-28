package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.MemberEndpoint;
import de.remsfal.core.json.project.ProjectMemberJson;
import de.remsfal.core.json.project.ProjectMemberListJson;
import de.remsfal.core.model.project.ProjectMemberModel;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.service.control.ProjectController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.Set;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class MemberResource extends AbstractProjectResource implements MemberEndpoint {

    @Inject
    ProjectController controller;

    @Override
    public ProjectMemberListJson getProjectMembers(final UUID projectId) {
        checkProjectReadPermissions(projectId);
        final Set<? extends ProjectMemberModel> model = controller.getProjectMembers(principal, projectId);
        return ProjectMemberListJson.valueOfSet(model);
    }

    @WithSpan("MemberResource.addProjectMember")
    @Override
    public ProjectMemberJson addProjectMember(final UUID projectId, final ProjectMemberJson member) {
        checkPropertyWritePermissions(projectId);
        final ProjectMemberModel model = controller.addProjectMember(principal, projectId, member);
        return ProjectMemberJson.valueOf(model);
    }

    @Override
    public ProjectMemberJson updateProjectMember(final UUID projectId, final UUID memberId,
            final ProjectMemberJson member) {
        checkPropertyWritePermissions(projectId);
        if(MemberRole.PROPRIETOR == member.getRole() && principal.getId().equals(memberId)) {
            throw new ForbiddenException("Nobody can upgrade their own role");
        }
        final ProjectMemberModel model = controller.changeProjectMemberRole(projectId,
            memberId, member.getRole());
        return ProjectMemberJson.valueOf(model);
    }

    @Override
    public void deleteProjectMember(final UUID projectId, final UUID memberId) {
        checkPropertyWritePermissions(projectId);
        if(controller.getProjectMemberRole(memberId, projectId) == MemberRole.PROPRIETOR
            && principal.getProjectRole(projectId) != MemberRole.PROPRIETOR) {
            throw new ForbiddenException("Only a proprietor can remove a proprietor from a project");
        }
        controller.removeProjectMember(projectId, memberId);
    }

}
