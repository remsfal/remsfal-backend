package de.remsfal.service.control;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.entity.dao.ProjectRepository;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class ProjectController {
    
    @Inject
    Logger logger;
    
    @Inject
    ProjectRepository repository;

    public List<? extends ProjectModel> getProjects(final UserModel user) {
        return null;
    }

    @Transactional
    public ProjectModel createProject(final UserModel user, final ProjectModel project) {
        logger.infov("Creating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        return null;
    }

    public ProjectModel getProject(final UserModel user, final String projectId) {
        logger.infov("Retrieving a project (id = {0})", projectId);
        final ProjectModel project = repository.find(user.getId());
        if(project == null) {
            throw new NotFoundException("Project not exist");
        }
        return project;
    }

    @Transactional
    public ProjectModel updateProject(final UserModel user, final ProjectModel project) {
        logger.infov("Updating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        return null;
    }

    @Transactional
    public boolean deleteProject(final String projectId) {
        logger.infov("Deleting a project (id = {0})", projectId);
        return repository.delete(projectId);
    }

    @Transactional
    public ProjectModel addProjectMember(final UserModel user, final ProjectModel project, final UserModel member) {
        logger.infov("Updating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        return null;
    }

    @Transactional
    public ProjectModel removeProjectMember(final UserModel user, final ProjectModel project, final UserModel member) {
        logger.infov("Updating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        return null;
    }

}
