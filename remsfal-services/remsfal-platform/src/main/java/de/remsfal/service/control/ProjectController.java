package de.remsfal.service.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.service.control.exception.ProprietorRequiredException;
import de.remsfal.service.control.event.ManagingOrganizationChangedEvent;
import de.remsfal.service.control.event.ProjectCreatedEvent;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.jboss.logging.Logger;

import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.OrganizationEmployeeModel.PermissionType;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.ProjectMemberModel;
import de.remsfal.core.model.project.ProjectModel;
import de.remsfal.core.model.project.OrganizationMemberModel;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;
import de.remsfal.service.boundary.eventing.ProjectEventProducer;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class ProjectController {

    private static final String NO_MEMBERSHIP_MESSAGE = "Project does not exist or user has no membership";

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

    @Inject
    ProjectEventProducer projectEventProducer;

    @Inject
    Event<ProjectCreatedEvent> projectCreatedEvent;

    @Inject
    Event<ManagingOrganizationChangedEvent> managingOrganizationChangedEvent;

    @WithSpan("ProjectController.getProjects")
    public List<ProjectModel> getProjects(final UserModel user, final Integer offset, final Integer limit) {
        return new ArrayList<>(projectRepository.findAccessibleProjects(user.getId(), offset, limit));
    }

    public long countProjects(final UserModel user) {
        return projectRepository.countAccessibleProjects(user.getId());
    }

    /**
     * Effective role of the user per project, either from a direct membership or derived from an organization.
     */
    public Map<UUID, MemberRole> getProjectRoles(final UserModel user) {
        return authorizationController.getProjectAuthorization(user.getId()).entrySet().stream()
            .collect(Collectors.toMap(e -> UUID.fromString(e.getKey()), e -> MemberRole.valueOf(e.getValue())));
    }

    /**
     * Creates a project. Without a managing organization the user becomes the proprietor of the project.
     * With a managing organization the project is created for that organization: the organization becomes the
     * proprietor and the managing organization of the project, and the user gets no direct membership.
     * Fires a {@link ProjectCreatedEvent} within the transaction after the project has been persisted.
     */
    @Transactional
    public ProjectModel createProject(final UserModel user, final ProjectModel project) {
        logger.infov("Creating a project (title={0}, email={1}, managingOrganizationId={2})",
            project.getTitle(), user.getEmail(), project.getManagingOrganizationId());
        UserEntity userEntity = userController.getUser(user.getId());

        ProjectEntity entity = new ProjectEntity();
        entity.generateId();
        entity.setTitle(project.getTitle());
        if (project.getManagingOrganizationId() != null) {
            final OrganizationEntity organization = getOrganizationForProjectCreation(
                project.getManagingOrganizationId(), user);
            entity.setOwner(organization.getName());
            if (organization.getAddress() != null) {
                entity.setBillingAddress(organization.getAddress());
            }
            entity.addOrganization(organization, MemberRole.PROPRIETOR);
            entity.setManagingOrganization(organization);
        } else {
            if (!userEntity.getName().equals(userEntity.getEmail())) {
                entity.setOwner(userEntity.getName());
            }
            if (userEntity.getAddress() != null) {
                entity.setBillingAddress(userEntity.getAddress());
            }
            entity.addMember(userEntity, MemberRole.PROPRIETOR);
        }
        projectRepository.persistAndFlush(entity);
        projectCreatedEvent.fire(
            new ProjectCreatedEvent(entity.getId(), user.getId(), entity.getManagingOrganizationId()));
        return entity;
    }

    private OrganizationEntity getOrganizationForProjectCreation(final UUID organizationId, final UserModel user) {
        final OrganizationEntity organization = organizationRepository.findById(organizationId);
        if (organization == null) {
            throw new NotFoundException("Organization does not exist");
        }
        final EmployeeRole role = organizationRepository
            .findOrganizationEmployeeByOrganizationIdAndUserId(organizationId, user.getId())
            .map(OrganizationEmployeeEntity::getRole)
            .orElseThrow(() -> new ForbiddenException("User is not an employee of the organization"));
        if (!role.isPrivileged(PermissionType.WRITE)) {
            throw new ForbiddenException("Only owners and managers can create projects for the organization");
        }
        return organization;
    }

    public ProjectModel getProject(final UserModel user, final UUID projectId) {
        logger.infov("Retrieving a project (id = {0})", projectId);
        return findAccessibleProject(user, projectId);
    }

    /**
     * Retrieves a project without checking the membership of a user, e.g. for read models of other perspectives.
     */
    public ProjectModel findProject(final UUID projectId) {
        return projectRepository.findById(projectId);
    }

    private ProjectEntity findAccessibleProject(final UserModel user, final UUID projectId) {
        return projectRepository.findAccessibleProject(user.getId(), projectId)
            .orElseThrow(() -> new NotFoundException(NO_MEMBERSHIP_MESSAGE));
    }

    @Transactional
    public ProjectModel updateProject(final UserModel user, final UUID projectId, final ProjectModel project) {
        logger.infov("Updating a project (title={0}, email={1})", project.getTitle(), user.getEmail());
        final ProjectEntity entity = findAccessibleProject(user, projectId);
        if (project.getManagingOrganizationId() != null
            && !project.getManagingOrganizationId().equals(entity.getManagingOrganizationId())) {
            final UUID previousOrganizationId = entity.getManagingOrganizationId();
            entity.setManagingOrganization(getProprietorOrganization(entity, project.getManagingOrganizationId()));
            managingOrganizationChangedEvent.fire(new ManagingOrganizationChangedEvent(projectId,
                previousOrganizationId, entity.getManagingOrganizationId(), user.getId()));
        }
        entity.setTitle(project.getTitle());
        if (project.getOwner() != null) {
            entity.setOwner(project.getOwner());
        }
        if (project.getCareOf() != null) {
            entity.setCareOf(project.getCareOf());
        }
        if (project.getBillingAddress() != null) {
            final UserEntity userEntity = userController.getUser(user.getId());
            entity.setBillingAddress(resolveBillingAddress(userEntity, project));
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
            if (candidate.equalsIgnoreCase(project.getBillingAddress())) {
                return candidate;
            }
        }
        return addressController.updateAddress(project.getBillingAddress(), null);
    }

    @Transactional
    public boolean deleteProject(final UserModel user, final UUID projectId) {
        logger.infov("Deleting a project (id = {0})", projectId);
        final ProjectEntity entity = projectRepository.findById(projectId);
        if (entity == null) {
            return false;
        } else if (projectRepository.findAccessibleProject(user.getId(), projectId).isPresent()) {
            final boolean deleted = projectRepository.deleteById(projectId);
            if (deleted) {
                projectEventProducer.sendProjectDeleted(projectId);
            }
            return deleted;
        } else {
            throw new ForbiddenException("User is not a member of this project");
        }
    }

    /**
     * Ensures that the project keeps at least one proprietor if the given user or organization loses its
     * proprietor role.
     *
     * @throws ProprietorRequiredException if no other proprietor would remain
     */
    private void ensureProprietorRemains(final UUID projectId, final UUID excludedUserId,
        final UUID excludedOrganizationId) {
        final ProjectEntity project = projectRepository.findById(projectId);
        if (project != null && project.countProprietors(excludedUserId, excludedOrganizationId) == 0) {
            throw new ProprietorRequiredException(
                "A project must always have a proprietor, either a user or an organization");
        }
    }

    /**
     * Returns the organization of the project with role PROPRIETOR, which can become the managing organization.
     */
    private OrganizationEntity getProprietorOrganization(final ProjectEntity project, final UUID organizationId) {
        return project.getOrganizations() == null ? null : project.getOrganizations().stream()
            .filter(o -> o.getOrganization().getId().equals(organizationId))
            .filter(o -> o.getRole() == MemberRole.PROPRIETOR)
            .map(ProjectOrganizationEntity::getOrganization)
            .findFirst()
            .orElseThrow(() -> new BadRequestException(
                "The managing organization must be a project member with role PROPRIETOR"));
    }

    public MemberRole getProjectMemberRole(final UUID userId, final UUID projectId) {
        logger.infov("Retrieving project member role (user={0}, project={1})", userId, projectId);
        return projectRepository.findMembershipByUserIdAndProjectId(userId, projectId)
            .map(ProjectMembershipEntity::getRole)
            .orElseThrow(() -> new ForbiddenException(NO_MEMBERSHIP_MESSAGE));
    }

    public Set<? extends ProjectMemberModel> getProjectMembers(final UserModel user, final UUID projectId) {
        logger.infov("Retrieving project membership (user={0}, project={1})", user.getId(), projectId);
        return findAccessibleProject(user, projectId).getMembers();
    }

    @WithSpan("ProjectController.addProjectMember")
    @Transactional
    public ProjectMemberModel addProjectMember(final UserModel user, final UUID projectId,
        final ProjectMemberModel member) {
        logger.infov("Adding a project membership (user={0}, project={1}, memberEmail={2}, memberRole={3})",
            user.getId(), projectId, member.getEmail(), member.getRole());
        final ProjectEntity projectEntity = findAccessibleProject(user, projectId);

        UserEntity userEntity = userController.findOrCreateUser(member);
        if (projectRepository.findMembershipByUserIdAndProjectId(userEntity.getId(), projectId).isPresent()) {
            throw new AlreadyExistsException("User is already a member of this project");
        }
        projectEntity.addMember(userEntity, member.getRole());
        notificationController.informUserAboutProjectMembership(userEntity, projectEntity);
        projectRepository.mergeAndFlush(projectEntity);
        return projectRepository.findMembershipByUserIdAndProjectId(userEntity.getId(), projectId)
            .orElseThrow(() -> new NotFoundException(NO_MEMBERSHIP_MESSAGE));
    }

    @Transactional
    public ProjectMemberModel changeProjectMemberRole(final UUID projectId, final UUID memberId,
        final MemberRole memberRole) {
        logger.infov("Updating a project membership (projectId={0}, memberId={1}, memberRole={2})",
            projectId, memberId, memberRole);
        final ProjectMembershipEntity entity = projectRepository.findMembershipByUserIdAndProjectId(memberId, projectId)
            .orElseThrow(() -> new NotFoundException(NO_MEMBERSHIP_MESSAGE));
        if (entity.getRole() == MemberRole.PROPRIETOR && memberRole != MemberRole.PROPRIETOR) {
            ensureProprietorRemains(projectId, memberId, null);
        }
        entity.setRole(memberRole);
        return projectRepository.merge(entity);
    }

    @Transactional
    public boolean removeProjectMember(final UUID projectId, final UUID memberId) {
        logger.infov("Removing a project membership (projectId={0}, memberId={1})",
            projectId, memberId);
        final boolean isProprietor = projectRepository.findMembershipByUserIdAndProjectId(memberId, projectId)
            .map(m -> m.getRole() == MemberRole.PROPRIETOR)
            .orElse(false);
        if (isProprietor) {
            ensureProprietorRemains(projectId, memberId, null);
        }
        return projectRepository.removeMembershipByUserIdAndProjectId(memberId, projectId);
    }

    public MemberRole getProjectOrganizationRole(final UUID organizationId, final UUID projectId) {
        logger.infov("Retrieving project organization role (organizationId={0}, project={1})",
            organizationId, projectId);
        return projectOrganizationRepository.findByProjectIdAndOrganizationId(projectId, organizationId)
            .map(ProjectOrganizationEntity::getRole)
            .orElseThrow(() -> new ForbiddenException(NO_MEMBERSHIP_MESSAGE));
    }

    public Set<? extends OrganizationMemberModel> getProjectOrganizations(final UserModel user, final UUID projectId) {
        logger.infov("Retrieving project organizations (user={0}, project={1})", user.getId(), projectId);
        return findAccessibleProject(user, projectId).getOrganizations();
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
    public OrganizationMemberModel addProjectOrganization(final UserModel user, final UUID projectId,
        final OrganizationMemberModel organization) {
        logger.infov("Adding an organization to project (user={0}, project={1}, organizationId={2}, role={3})",
            user.getId(), projectId, organization.getOrganizationId(), organization.getRole());
        final ProjectEntity projectEntity = findAccessibleProject(user, projectId);

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
    public OrganizationMemberModel changeProjectOrganizationRole(final UUID projectId, final UUID organizationId,
        final MemberRole role) {
        logger.infov("Updating a project organization role (projectId={0}, organizationId={1}, role={2})",
            projectId, organizationId, role);
        final ProjectOrganizationEntity entity = projectOrganizationRepository
            .findByProjectIdAndOrganizationId(projectId, organizationId)
            .orElseThrow(() -> new NotFoundException("Organization is not assigned to this project"));
        if (entity.getRole() == MemberRole.PROPRIETOR && role != MemberRole.PROPRIETOR) {
            ensureProprietorRemains(projectId, null, organizationId);
            // the managing organization must be a proprietor of the project
            clearManagingOrganization(projectId, organizationId);
        }
        entity.setRole(role);
        return projectOrganizationRepository.merge(entity);
    }

    @Transactional
    public boolean removeProjectOrganization(final UUID projectId, final UUID organizationId) {
        logger.infov("Removing an organization from project (projectId={0}, organizationId={1})",
            projectId, organizationId);
        final boolean isProprietor = projectOrganizationRepository
            .findByProjectIdAndOrganizationId(projectId, organizationId)
            .map(o -> o.getRole() == MemberRole.PROPRIETOR)
            .orElse(false);
        if (isProprietor) {
            ensureProprietorRemains(projectId, null, organizationId);
        }
        clearManagingOrganization(projectId, organizationId);
        return projectOrganizationRepository.removeByProjectIdAndOrganizationId(projectId, organizationId);
    }

    /**
     * Removes the organization as managing organization from all projects it manages, e.g. before the organization
     * is deleted. Fires a {@link ManagingOrganizationChangedEvent} for every affected project.
     */
    @Transactional
    public void clearManagingOrganization(final UUID organizationId) {
        for (final UUID projectId : projectRepository.clearManagingOrganization(organizationId)) {
            logger.infov("Removing the managing organization of a project (projectId={0}, organizationId={1})",
                projectId, organizationId);
            managingOrganizationChangedEvent.fire(
                new ManagingOrganizationChangedEvent(projectId, organizationId, null, null));
        }
    }

    private void clearManagingOrganization(final UUID projectId, final UUID organizationId) {
        final ProjectEntity project = projectRepository.findById(projectId);
        if (project != null && organizationId.equals(project.getManagingOrganizationId())) {
            logger.infov("Removing the managing organization of a project (projectId={0}, organizationId={1})",
                projectId, organizationId);
            project.setManagingOrganization(null);
            projectRepository.merge(project);
            managingOrganizationChangedEvent.fire(
                new ManagingOrganizationChangedEvent(projectId, organizationId, null, null));
        }
    }

}
