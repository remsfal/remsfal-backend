package de.remsfal.service.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

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
    public ProjectModel updateProject(final UserModel user, final ProjectModel project) {
        logger.infov("Updating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        try {
            final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), project.getId());
            entity.setTitle(project.getTitle());
            return projectRepository.merge(entity);
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
    public ProjectModel addProjectMember(final UserModel user, final ProjectModel project, final UserModel member) {
        logger.infov("Adding a project membership (title={0}, email={1})", project.getTitle(), user.getEmail());
        return null;
    }

    public Set<ProjectModel> getProjectMembers(final UserModel user, final ProjectModel project, final UserModel member) {
        logger.infov("Retrieving project membership (title={0}, email={1})", project.getTitle(), user.getEmail());
        return null;
    }

    @Transactional
    public ProjectModel removeProjectMember(final UserModel user, final ProjectModel project, final UserModel member) {
        logger.infov("Removing a project membership (title={0}, email={1})", project.getTitle(), user.getEmail());
        return null;
    }

    @Transactional
    public ProjectModel changeProjectMemberRole(final UserModel user, final ProjectModel project, final UserModel member) {
        logger.infov("Updating a project membership (title={0}, email={1})", project.getTitle(), user.getEmail());
        return null;
    }

}
