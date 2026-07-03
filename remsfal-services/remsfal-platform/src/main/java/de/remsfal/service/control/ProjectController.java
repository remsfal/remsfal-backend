package de.remsfal.service.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import de.remsfal.service.control.exception.AlreadyExistsException;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.jboss.logging.Logger;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.ProjectMemberModel;
import de.remsfal.core.model.project.ProjectModel;
import de.remsfal.core.model.project.ProjectOrganizationModel;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.service.entity.dao.OrganizationRepository;
import de.remsfal.service.entity.dao.ProjectOrganizationRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dto.OrganizationEmployeeEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import de.remsfal.service.entity.dto.ProjectOrganizationEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.service.entity.dto.AddressEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Inject
    ProjectOrganizationRepository projectOrganizationRepository;

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    AddressController addressController;

    @Inject
    AuthorizationController authorizationController;

    @WithSpan("ProjectController.getProjects")
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
        if (!userEntity.getName().equals(userEntity.getEmail())) {
            entity.setOwner(userEntity.getName());
        }
        if (userEntity.getAddress() != null) {
            entity.setAddress(userEntity.getAddress());
        }
        entity.addMember(userEntity, MemberRole.PROPRIETOR);
        projectRepository.persistAndFlush(entity);
        return entity;
    }

    public ProjectModel getProject(final UserModel user, final UUID projectId) {
        logger.infov("Retrieving a project (id = {0})", projectId);
        return projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project does not exist or user has no membership"));
    }

    @Transactional
    public ProjectModel updateProject(final UserModel user, final UUID projectId, final ProjectModel project) {
        logger.infov("Updating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        entity.setTitle(project.getTitle());
        if (project.getOwner() != null) {
            entity.setOwner(project.getOwner());
        }
        if (project.getCareOf() != null) {
            entity.setCareOf(project.getCareOf());
        }
        if (project.getAddress() != null) {
            final UserEntity userEntity = userController.getUser(user.getId());
            entity.setAddress(resolveBillingAddress(userEntity, project));
        }
        return projectRepository.merge(entity);
        // fetch eager project members
        // entity.getMembers().size();
        // return entity;
    }

    private AddressEntity resolveBillingAddress(final UserEntity userEntity, final ProjectModel project) {
        final List<AddressEntity> addresses = new ArrayList<>();
        if (userEntity.getAddress() != null) {
            addresses.add(userEntity.getAddress());
        }
        organizationRepository.findOrganizationEmployeesByUserId(userEntity.getId())
            .stream()
            .map(employment -> employment.getOrganization().getAddress())
            .filter(Objects::nonNull)
            .forEach(addresses::add);
        for (AddressEntity candidate : addresses) {
            if (candidate.equalsIgnoreCase(project.getAddress())) {
                return candidate;
            }
        }
        return addressController.updateAddress(project.getAddress(), null);
    }

    @Transactional
    public boolean deleteProject(final UserModel user, final UUID projectId) {
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

    public String getProjectTitle(final UUID projectId) {
        ProjectEntity entity = projectRepository.findById(projectId);
        return entity != null ? entity.getTitle() : null;
    }

    public MemberRole getProjectMemberRole(final UUID userId, final UUID projectId) {
        logger.infov("Retrieving project member role (user={0}, project={1})", userId, projectId);
        return projectRepository.findMembershipByUserIdAndProjectId(userId, projectId)
            .map(ProjectMembershipEntity::getRole)
            .orElseThrow(() -> new ForbiddenException("Project not exist or user has no membership"));
    }

    public Set<? extends ProjectMemberModel> getProjectMembers(final UserModel user, final UUID projectId) {
        logger.infov("Retrieving project membership (user={0}, project={1})", user.getId(), projectId);
        final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        return entity.getMembers();
    }

    @WithSpan("ProjectController.addProjectMember")
    @Transactional
    public ProjectMemberModel addProjectMember(final UserModel user, final UUID projectId,
        final ProjectMemberModel member) {
        logger.infov("Adding a project membership (user={0}, project={1}, memberEmail={2}, memberRole={3})",
            user.getId(), projectId, member.getEmail(), member.getRole());
        final ProjectEntity projectEntity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));

        UserEntity userEntity = userController.findOrCreateUser(member);
        if (projectRepository.findMembershipByUserIdAndProjectId(userEntity.getId(), projectId).isPresent()) {
            throw new AlreadyExistsException("User is already a member of this project");
        }
        projectEntity.addMember(userEntity, member.getRole());
        notificationController.informUserAboutProjectMembership(userEntity, projectId);
        projectRepository.mergeAndFlush(projectEntity);
        return projectRepository.findMembershipByUserIdAndProjectId(userEntity.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
    }

    @Transactional
    public ProjectMemberModel changeProjectMemberRole(final UUID projectId, final UUID memberId,
        final MemberRole memberRole) {
        logger.infov("Updating a project membership (projectId={0}, memberId={1}, memberRole={2})",
            projectId, memberId, memberRole);
        final ProjectMembershipEntity entity = projectRepository.findMembershipByUserIdAndProjectId(memberId, projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        entity.setRole(memberRole);
        return projectRepository.merge(entity);
    }

    @Transactional
    public boolean removeProjectMember(final UUID projectId, final UUID memberId) {
        logger.infov("Removing a project membership (projectId={0}, memberId={1})",
            projectId, memberId);
        return projectRepository.removeMembershipByUserIdAndProjectId(memberId, projectId);
    }

    public MemberRole getProjectOrganizationRole(final UUID organizationId, final UUID projectId) {
        logger.infov("Retrieving project organization role (organizationId={0}, project={1})",
            organizationId, projectId);
        return projectOrganizationRepository.findByProjectIdAndOrganizationId(projectId, organizationId)
            .map(ProjectOrganizationEntity::getRole)
            .orElseThrow(() -> new ForbiddenException("Project not exist or user has no membership"));
    }

    public Set<? extends ProjectOrganizationModel> getProjectOrganizations(final UserModel user, final UUID projectId) {
        logger.infov("Retrieving project organizations (user={0}, project={1})", user.getId(), projectId);
        final ProjectEntity entity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));
        return entity.getOrganizations();
    }

    public List<ProjectMemberModel> getOrganizationMembers(final UUID organizationId,
        final MemberRole organizationRoleInProject) {
        logger.infov("Retrieving organization members with derived project role (organizationId={0}, role={1})",
            organizationId, organizationRoleInProject);
        final OrganizationEntity organization = organizationRepository.findById(organizationId);
        if (organization == null || organization.getEmployees() == null) {
            return List.of();
        }
        return organization.getEmployees().stream()
            .map(employee -> (ProjectMemberModel) new OrganizationMemberAdapter(employee,
                authorizationController.calculateProjectRole(employee.getRole(), organizationRoleInProject)))
            .collect(Collectors.toList());
    }

    private static final class OrganizationMemberAdapter implements ProjectMemberModel {

        private final OrganizationEmployeeEntity employee;
        private final MemberRole role;

        private OrganizationMemberAdapter(final OrganizationEmployeeEntity employee, final MemberRole role) {
            this.employee = employee;
            this.role = role;
        }

        @Override
        public UUID getId() {
            return employee.getId();
        }

        @Override
        public String getName() {
            return employee.getName();
        }

        @Override
        public String getEmail() {
            return employee.getEmail();
        }

        @Override
        public Boolean isActive() {
            return employee.isActive();
        }

        @Override
        public MemberRole getRole() {
            return role;
        }
    }

    @Transactional
    public ProjectOrganizationModel addProjectOrganization(final UserModel user, final UUID projectId,
        final ProjectOrganizationModel organization) {
        logger.infov("Adding an organization to project (user={0}, project={1}, organizationId={2}, role={3})",
            user.getId(), projectId, organization.getOrganizationId(), organization.getRole());
        final ProjectEntity projectEntity = projectRepository.findProjectByUserId(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException("Project not exist or user has no membership"));

        final OrganizationEntity organizationEntity = organizationRepository.findById(organization.getOrganizationId());
        if (organizationEntity == null) {
            throw new NotFoundException("Organization does not exist");
        }
        if (projectOrganizationRepository
            .findByProjectIdAndOrganizationId(projectId, organization.getOrganizationId())
            .isPresent()) {
            throw new AlreadyExistsException("Organization is already assigned to this project");
        }

        projectEntity.addOrganization(organizationEntity, organization.getRole());
        projectRepository.mergeAndFlush(projectEntity);
        return projectOrganizationRepository
            .findByProjectIdAndOrganizationId(projectId, organization.getOrganizationId())
            .orElseThrow(() -> new NotFoundException("Failed to add organization to project"));
    }

    @Transactional
    public ProjectOrganizationModel changeProjectOrganizationRole(final UUID projectId, final UUID organizationId,
        final MemberRole role) {
        logger.infov("Updating a project organization role (projectId={0}, organizationId={1}, role={2})",
            projectId, organizationId, role);
        final ProjectOrganizationEntity entity = projectOrganizationRepository
            .findByProjectIdAndOrganizationId(projectId, organizationId)
            .orElseThrow(() -> new NotFoundException("Organization is not assigned to this project"));
        entity.setRole(role);
        return projectOrganizationRepository.merge(entity);
    }

    @Transactional
    public boolean removeProjectOrganization(final UUID projectId, final UUID organizationId) {
        logger.infov("Removing an organization from project (projectId={0}, organizationId={1})",
            projectId, organizationId);
        return projectOrganizationRepository.removeByProjectIdAndOrganizationId(projectId, organizationId);
    }

}
