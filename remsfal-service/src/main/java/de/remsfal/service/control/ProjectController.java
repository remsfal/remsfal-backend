package de.remsfal.service.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.core.model.ProjectMemberModel.UserRole;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.UserEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class ProjectController {

    @Inject
    Logger logger;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    NotificationController notificationController;
    
    public List<ProjectModel> getProjects(final UserModel user) {
        List<ProjectMembershipEntity> memberships = projectRepository.findMembershipByUserId(user.getId());
        List<ProjectModel> projects = new ArrayList<>();
        for (ProjectMembershipEntity projectMembership : memberships) {
            projects.add(projectMembership.getProject());
        }
        return projects;
    }

    @Transactional
    public ProjectModel createProject(final UserModel user, final ProjectModel project) {
        logger.infov("Creating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        UserEntity userEntity = userRepository.findById(user.getId());

        ProjectEntity entity = new ProjectEntity();
        entity.generateId();
        entity.setTitle(project.getTitle());
        entity.addMember(userEntity, UserRole.MANAGER);
        projectRepository.persistAndFlush(entity);
        return entity;
    }

    public ProjectModel getProject(final UserModel user, final String projectId) {
        logger.infov("Retrieving a project (id = {0})", projectId);
        try {
            return projectRepository.findProjectByUserId(user.getId(), projectId);
        } catch (final NoResultException e) {
            throw new NotFoundException("Project not exist or user has no membership", e);
        }
    }

    @Transactional
    public ProjectModel updateProject(final UserModel user, final String projectId, final ProjectModel project) {
        logger.infov("Updating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        try {
            final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId);
            entity.setTitle(project.getTitle());
            return projectRepository.merge(entity);
            // fetch eager project members
            //entity.getMembers().size();
            // return entity;
        } catch (final NoResultException e) {
            throw new NotFoundException("Project not exist or user has no membership", e);
        }
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

    @Transactional
    public ProjectModel addProjectMember(final UserModel user, final String projectId, final ProjectMemberModel member) {
        logger.infov("Adding a project membership (user={0}, project={1}, member={2})", user.getId(), projectId, member.getEmail());
        try {
            final ProjectEntity projectEntity = projectRepository.findProjectByUserId(user.getId(), projectId);
            UserEntity userEntity;
            if(member.getId() != null) {
                userEntity = userRepository.findById(member.getId());
            } else if (member.getEmail() != null) {
                try {
                    userEntity = userRepository.findByEmail(member.getEmail());
                } catch (final NoResultException e) {
                    userEntity = new UserEntity();
                    userEntity.generateId();
                    userEntity.setEmail(member.getEmail());
                    userRepository.persist(userEntity);
                    notificationController.informUserAboutRegistration(userEntity);
                }
            } else {
                throw new BadRequestException("Project member's email is missing");
            }
            projectEntity.addMember(userEntity, member.getRole());
            notificationController.informUserAboutProjectMembership(userEntity);
            return projectRepository.mergeAndFlush(projectEntity);
        } catch (final NoResultException e) {
            throw new NotFoundException("Project not exist or user has no membership", e);
        }
    }

    public Set<? extends ProjectMemberModel> getProjectMembers(final UserModel user, final String projectId) {
        logger.infov("Retrieving project membership (user={0}, project={1})", user.getId(), projectId);
        try {
            final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId);
            return entity.getMembers();
        } catch (final NoResultException e) {
            throw new NotFoundException("Project not exist or user has no membership", e);
        }
    }

    @Transactional
    public ProjectModel removeProjectMember(final UserModel user, final String projectId, final UserModel member) {
        logger.infov("Removing a project membership (user={0}, project={1}, member={2})", user.getId(), projectId, member.getEmail());
        try {
            final ProjectMembershipEntity membership = projectRepository.findMembershipByUserIdAndProjectId(user.getId(), projectId);
            if(!membership.isPrivileged()) {
                throw new ForbiddenException("The user is not privileged to delete this project.");
            }
            if(projectRepository.removeMembershipByUserIdAndProjectId(member.getId(), projectId)) {
                projectRepository.getEntityManager().clear();
                return projectRepository.findProjectByUserId(user.getId(), projectId);
            } else {
                return membership.getProject();
            }
        } catch (final NoResultException e) {
            throw new NotFoundException("Project not exist or user has no membership", e);
        }
    }

    @Transactional
    public ProjectModel changeProjectMemberRole(final UserModel user, final String projectId, final ProjectMemberModel member) {
        logger.infov("Updating a project membership (user={0}, project={1}, member={2})", user.getId(), projectId, member.getEmail());
        try {
            final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId);
            entity.changeMemberRole(member);
            return projectRepository.merge(entity);
        } catch (final NoResultException e) {
            throw new NotFoundException("Project not exist or user has no membership", e);
        }
    }

}
