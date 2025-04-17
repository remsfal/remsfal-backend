package de.remsfal.chat.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import org.jboss.logging.Logger;

import de.remsfal.chat.entity.dao.ProjectMembershipRepository;
import de.remsfal.chat.entity.dto.ProjectMembershipEntity;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class AuthorizationController {

    @Inject
    Logger logger;

    @Inject
    ProjectMembershipRepository projectRepository;

    public MemberRole getProjectMemberRole(final UserModel user, final String projectId) {
        logger.infov("Retrieving project member role (user={0}, project={1})", user.getId(), projectId);
        return projectRepository.findMembershipByUserIdAndProjectId(user.getId(), projectId)
            .map(ProjectMembershipEntity::getRole)
            .orElseThrow(() -> new ForbiddenException("Project not exist or user has no membership"));
    }

}
