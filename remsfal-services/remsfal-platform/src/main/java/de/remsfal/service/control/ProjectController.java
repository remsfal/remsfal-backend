package de.remsfal.service.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class ProjectController {

    @Inject
    Logger logger;

    @Inject
    UserController userController;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    NotificationController notificationController;

    public List<ProjectModel> getProjects(final UserModel user, final Integer offset, final Integer limit) {
        List<ProjectMembershipEntity> memberships = projectRepository.findMembershipByUserId(user.getId(),
            offset, limit);
        List<ProjectModel> projects = new ArrayList<>();
        for (ProjectMembershipEntity projectMembership : memberships) {
            projects.add(projectMembership.getProject());
        }
        return projects;
    }

    public long countProjects(final UserModel user) {
        return projectRepository.countMembershipByUserId(user.getId());
    }

    @Transactional
    public ProjectModel createProject(final UserModel user, final ProjectModel project) {
        logger.infov("Creating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        UserEntity userEntity = userController.getUser(user.getId());

        ProjectEntity entity = new ProjectEntity();
        entity.generateId();
        entity.setTitle(project.getTitle());
        entity.addMember(userEntity, MemberRole.MANAGER);
        projectRepository.persistAndFlush(entity);
        return entity;
    }

    public ProjectModel getProject(final UserModel user, final String projectId) {
        logger.infov("Retrieving a project (id = {0})", projectId);
        // First check if the project exists
        ProjectEntity project = projectRepository.findById(projectId);
        if (project == null) {
            throw new NotFoundException("Project not found");
        }

        // Then check if the user is a member of the project
        return projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
    }

    @Transactional
    public ProjectModel updateProject(final UserModel user, final String projectId, final ProjectModel project) {
        logger.infov("Updating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        entity.setTitle(project.getTitle());
        return projectRepository.merge(entity);
        // fetch eager project members
        // entity.getMembers().size();
        // return entity;
    }

    @Transactional
    public boolean deleteProject(final UserModel user, final String projectId) {
        logger.infov("Deleting a project (id = {0})", projectId);
        final ProjectEntity entity = projectRepository.findById(projectId);
        if (entity == null) {
            return false;
        } else if (entity.isMember(user)) {
            return projectRepository.deleteById(projectId);
        } else {
            throw new ForbiddenException("User is not a member of this project");
        }
    }

    public MemberRole getProjectMemberRole(final UserModel user, final String projectId) {
        logger.infov("Retrieving project member role (user={0}, project={1})", user.getId(), projectId);
        return projectRepository.findMembershipByUserIdAndProjectId(user.getId(), projectId)
            .map(ProjectMembershipEntity::getRole)
            .orElseThrow(() -> new ForbiddenException("Project not exist or user has no membership"));
    }

    public Set<? extends ProjectMemberModel> getProjectMembers(final UserModel user, final String projectId) {
        logger.infov("Retrieving project membership (user={0}, project={1})", user.getId(), projectId);
        final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        return entity.getMembers();
    }

    @Transactional
    public ProjectMemberModel addProjectMember(final UserModel user, final String projectId,
            final ProjectMemberModel member) {
        logger.infov("Adding a project membership (user={0}, project={1}, memberEmail={2}, memberRole={3})",
            user.getId(), projectId, member.getEmail(), member.getRole());
        final ProjectEntity projectEntity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));

        UserEntity userEntity = userController.findOrCreateUser(member);
        projectEntity.addMember(userEntity, member.getRole());
        notificationController.informUserAboutProjectMembership(userEntity);
        projectRepository.mergeAndFlush(projectEntity);
        return projectRepository.findMembershipByUserIdAndProjectId(userEntity.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
    }

    @Transactional
    public ProjectMemberModel changeProjectMemberRole(final String projectId,
            final String memberId, final MemberRole memberRole) {
        logger.infov("Updating a project membership (projectId={0}, memberId={1}, memberRole={2})",
            projectId, memberId, memberRole);
        final ProjectMembershipEntity entity = projectRepository.findMembershipByUserIdAndProjectId(memberId, projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        entity.setRole(memberRole);
        return projectRepository.merge(entity);
    }

    @Transactional
    public boolean removeProjectMember(final String projectId, final String memberId) {
        logger.infov("Removing a project membership (projectId={0}, memberId={1})",
            projectId, memberId);
        return projectRepository.removeMembershipByUserIdAndProjectId(memberId, projectId);
    }

}
