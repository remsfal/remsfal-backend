package de.remsfal.service.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.core.model.ProjectMemberModel.UserRole;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        entity.addMember(userEntity, UserRole.MANAGER);
        projectRepository.persistAndFlush(entity);
        return entity;
    }

    public ProjectModel getProject(final UserModel user, final String projectId) {
        logger.infov("Retrieving a project (id = {0})", projectId);
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

    public UserRole getProjectMemberRole(final UserModel user, final String projectId) {
        logger.infov("Retrieving project member role (user={0}, project={1})", user.getId(), projectId);
        return projectRepository.findMembershipByUserIdAndProjectId(user.getId(), projectId)
            .map(ProjectMembershipEntity::getRole)
            .orElseThrow(() -> new ForbiddenException("Project not exist or user has no membership"));
    }

    @Transactional
    public ProjectModel addProjectMember(final UserModel user, final String projectId, final ProjectMemberModel member) {
        logger.infov("Adding a project membership (user={0}, project={1}, member={2})", user.getId(), projectId, member.getEmail());
        final ProjectEntity projectEntity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));

        UserEntity userEntity = userController.findOrCreateUser(member);
        projectEntity.addMember(userEntity, member.getRole());
        notificationController.informUserAboutProjectMembership(userEntity);
        return projectRepository.mergeAndFlush(projectEntity);
    }

    public Set<? extends ProjectMemberModel> getProjectMembers(final UserModel user, final String projectId) {
        logger.infov("Retrieving project membership (user={0}, project={1})", user.getId(), projectId);
        final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        return entity.getMembers();
    }

    @Transactional
    public ProjectModel removeProjectMember(final UserModel user, final String projectId, final String member) {
        logger.infov("Removing a project membership (user={0}, project={1}, member={2})", user.getId(), projectId, member);
        final ProjectMembershipEntity membership = projectRepository.findMembershipByUserIdAndProjectId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));

        if (!membership.isPrivileged()) {
            throw new ForbiddenException("The user is not privileged to delete this project.");
        }

        if(projectRepository.removeMembershipByUserIdAndProjectId(member, projectId)) {
            projectRepository.getEntityManager().clear();
            Optional<ProjectEntity> projectByUserId = projectRepository.findProjectByUserId(user.getId(), projectId);
            if (projectByUserId.isEmpty()) {
                throw new NotFoundException("Project not exist or user has no membership");
            }

            return projectByUserId.get();
        } else {
            return membership.getProject();
        }
    }

    @Transactional
    public ProjectModel changeProjectMemberRole(final UserModel user, final String projectId, final ProjectMemberModel member) {
        logger.infov("Updating a project membership (user={0}, project={1}, memberId={2}, memberRole={3})",
                user.getId(), projectId, member.getId(), member.getRole());
        final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        entity.changeMemberRole(member);
        return projectRepository.merge(entity);
    }
}
