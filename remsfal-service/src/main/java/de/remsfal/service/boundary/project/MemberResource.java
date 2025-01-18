package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.MemberEndpoint;
import de.remsfal.core.json.ProjectMemberJson;
import de.remsfal.core.json.ProjectMemberListJson;
import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.service.control.ProjectController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.Set;

@RequestScoped
public class MemberResource extends ProjectSubResource implements MemberEndpoint {

    @Inject
    ProjectController controller;

    @Override
    public ProjectMemberListJson getProjectMembers(final String projectId) {
        checkReadPermissions(projectId);
        final Set<? extends ProjectMemberModel> model = controller.getProjectMembers(principal, projectId);
        return ProjectMemberListJson.valueOfSet(model);
    }

    @Override
    public ProjectMemberJson addProjectMember(final String projectId, final ProjectMemberJson member) {
        checkWritePermissions(projectId);
        final ProjectMemberModel model =  controller.addProjectMember(principal, projectId, member);
        return ProjectMemberJson.valueOf(model);
    }

    @Override
    public ProjectMemberJson updateProjectMember(final String projectId, final String memberId,
            final ProjectMemberJson member) {
        checkWritePermissions(projectId);
        final ProjectMemberModel model = controller.changeProjectMemberRole(projectId,
            memberId, member.getRole());
        return ProjectMemberJson.valueOf(model);
    }

    @Override
    public void deleteProjectMember(final String projectId, final String memberId) {
        checkOwnerPermissions(projectId);
        controller.removeProjectMember(projectId, memberId);
    }

}
