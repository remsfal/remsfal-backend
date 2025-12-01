package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.MemberEndpoint;
import de.remsfal.core.json.ProjectMemberJson;
import de.remsfal.core.json.ProjectMemberListJson;
import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.service.control.ProjectController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.util.Set;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class MemberResource extends ProjectSubResource implements MemberEndpoint {

    @Inject
    ProjectController controller;

    @Override
    public ProjectMemberListJson getProjectMembers(final UUID projectId) {
        checkReadPermissions(projectId);
        final Set<? extends ProjectMemberModel> model = controller.getProjectMembers(principal, projectId);
        return ProjectMemberListJson.valueOfSet(model);
    }

    @Override
    @WithSpan("MemberResource.addProjectMember")
    public ProjectMemberJson addProjectMember(final UUID projectId, final ProjectMemberJson member) {
        checkWritePermissions(projectId);

        Span span = Span.current();
        if (span != null) {
            span.setAttribute("remsfal.project.id", projectId.toString());
            if (member.getEmail() != null) {
                span.setAttribute("remsfal.member.email", member.getEmail());
            }
            if (member.getRole() != null) {
                span.setAttribute("remsfal.member.role", member.getRole().name());
            }
        }

        final ProjectMemberModel model = controller.addProjectMember(principal, projectId, member);
        return ProjectMemberJson.valueOf(model);
    }

    @Override
    public ProjectMemberJson updateProjectMember(final UUID projectId, final UUID memberId,
            final ProjectMemberJson member) {
        checkWritePermissions(projectId);
        final ProjectMemberModel model = controller.changeProjectMemberRole(projectId,
            memberId, member.getRole());
        return ProjectMemberJson.valueOf(model);
    }

    @Override
    public void deleteProjectMember(final UUID projectId, final UUID memberId) {
        checkOwnerPermissions(projectId);
        controller.removeProjectMember(projectId, memberId);
    }

}
