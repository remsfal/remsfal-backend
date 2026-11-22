package de.remsfal.service.control;

import de.remsfal.core.json.organization.ClientProjectListJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.model.OrganizationEmployeeModel;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.OrganizationModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.ProjectMemberModel.MemberRole;

import de.remsfal.core.json.organization.OrganizationEmployeeJson;
import de.remsfal.service.control.event.OrganizationEmployeeEvent;
import de.remsfal.service.control.event.OrganizationEvent;
import de.remsfal.service.control.exception.AlreadyExistsException;
import de.remsfal.service.control.exception.OrganizationAdminRequiredException;
import de.remsfal.service.control.exception.ProprietorRequiredException;
import de.remsfal.service.entity.dao.OrganizationRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dto.OrganizationEmployeeEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.ProjectEntity;
import de.remsfal.service.entity.dto.UserEntity;


import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestScoped
public class OrganizationController {

    @Inject
    Logger logger;

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ContractorOrganizationLinkController contractorOrganizationLinker;

    @Inject
    AddressController addressController;

    @Inject
    UserController userController;

    @Inject
    ActingUserController actingUser;

    @Inject
    Event<OrganizationEvent> organizationEvent;

    @Inject
    Event<OrganizationEmployeeEvent> organizationEmployeeEvent;

    /**
     * Retrieve an organization by id
     *
     * @param id id of the organization
     * @return organization entity
     */
    public OrganizationEntity getOrganizationById(final UUID id) {
        logger.infov("Retrieve Organization by id {0}", id);
        return organizationRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    /**
     * Retrieve all organizations owned by the given user.
     *
     * @param user the authenticated user
     * @return list of organizations where the user is OWNER
     */
    public List<OrganizationEntity> getOrganizations(final UserModel user) {
        logger.infov("Retrieving owned Organizations for user {0}", user.getId());
        return organizationRepository.findOrganizationEmployeesByUserId(user.getId())
            .stream()
            .filter(e -> e.getRole() == EmployeeRole.OWNER)
            .map(OrganizationEmployeeEntity::getOrganization)
            .toList();
    }

    /**
     * Create an organization
     *
     * @param organization the organization
     * @return the created organization entity
     */
    @Transactional
    public OrganizationEntity createOrganization(final OrganizationModel organization, final UserModel user) {

        logger.infov("Creating Organization (name={0}, email={1}, phone={2}, trade={3}, vatId={4}, address={5}",
            organization.getName(), organization.getEmail(), organization.getPhone(), organization.getTrade(),
            organization.getVatIdentificationNumber(), organization.getAddress());

        UserEntity userEntity = userController.findOrCreateUser(user);

        OrganizationEntity organizationEntity = mergeOrganizationFields(organization, new OrganizationEntity());
        organizationEntity.generateId();
        if (organization.getEmail() != null) {
            if (!userController.isVerifiedEmailForUser(user.getId(), organization.getEmail())) {
                throw new BadRequestException(
                    "Organization email must be the owner's primary or a verified additional email.");
            }
            final String normalizedEmail = organization.getEmail().trim().toLowerCase();
            if (organizationRepository.findByEmail(normalizedEmail).isPresent()) {
                throw new AlreadyExistsException(
                    "Email " + normalizedEmail + " is already used by another organization");
            }
            organizationEntity.setEmail(normalizedEmail);
        }
        organizationEntity.addEmployee(userEntity, EmployeeRole.OWNER);

        organizationRepository.persistAndFlush(organizationEntity);
        if (organizationEntity.getEmail() != null) {
            contractorOrganizationLinker.linkContractorsForNewOrganization(organizationEntity);
        }
        organizationEvent.fire(
            new OrganizationEvent.Created(OrganizationJson.valueOf(organizationEntity), user.getId()));

        return organizationEntity;
    }

    /**
     * Search organizations by name (case-insensitive, partial match).
     *
     * @param query  the search term (min. 3 characters)
     * @param offset pagination offset
     * @param limit  maximum number of results
     * @return matching organizations
     */
    public List<OrganizationEntity> searchOrganizations(final String query,
        final int offset, final int limit) {
        logger.infov("Searching organizations by name (query={0})", query);
        return organizationRepository.searchByName(query, offset, limit);
    }

    /**
     * Count organizations matching a name search.
     *
     * @param query the search term
     * @return total count
     */
    public long countSearchOrganizations(final String query) {
        return organizationRepository.countByName(query);
    }

    /**
     * Retrieve paginated distinct organizations that are contractors in projects accessible to the user.
     *
     * @param user   the authenticated user
     * @param offset pagination offset
     * @param limit  pagination limit
     * @return list of contractor organizations
     */
    public List<OrganizationEntity> getContractorOrganizations(final UserModel user,
        final int offset, final int limit) {
        logger.infov("Retrieving contractor organizations for user {0}", user.getId());
        return organizationRepository.findContractorOrganizationsByUser(user.getId(), offset, limit);
    }

    /**
     * Count distinct organizations that are contractors in projects accessible to the user.
     *
     * @param user the authenticated user
     * @return total count
     */
    public long countContractorOrganizations(final UserModel user) {
        return organizationRepository.countContractorOrganizationsByUser(user.getId());
    }

    public ClientProjectListJson getClients(final UUID organizationId) {
        logger.infov("Retrieving clients of organization {0}", organizationId);
        return ClientProjectListJson.valueOf(organizationRepository.findClientProjectsByOrganization(organizationId));
    }

    /**
     * Update an organization
     *
     * @param organization The organization entity containing the updated values
     * @return The updated organization
     */
    @Transactional
    public OrganizationEntity updateOrganization(final UserModel user, final UUID organizationId,
        final OrganizationModel organization) {

        logger.infov("Updating Organization (id={0}, name={1}, email={2}, phone={3}, trade={4}, vatId={5}, address={6}",
            organization.getId(), organization.getName(), organization.getEmail(), organization.getPhone(),
            organization.getTrade(), organization.getVatIdentificationNumber(), organization.getAddress());

        OrganizationEntity organizationEntity = organizationRepository
            .findOrganizationByUserId(user.getId(), organizationId)
            .orElseThrow(() -> new NotFoundException("Organization not exist or user is not an employee"));

        final boolean contractorRelevantChange = organization.getName() != null || organization.getPhone() != null
            || organization.getTrade() != null || organization.getAddress() != null
            || organization.getEmail() != null;

        if (organization.getEmail() != null) {
            if (!userController.isVerifiedEmailForUser(user.getId(), organization.getEmail())) {
                throw new BadRequestException(
                    "Organization email must be the owner's primary or a verified additional email.");
            }
            final String normalizedEmail = organization.getEmail().trim().toLowerCase();
            organizationRepository.findByEmail(normalizedEmail)
                .filter(o -> !o.getId().equals(organizationId))
                .ifPresent(o -> {
                    throw new AlreadyExistsException(
                        "Email " + normalizedEmail + " is already used by another organization");
                });
            organizationEntity.setEmail(normalizedEmail);
        }

        mergeOrganizationFields(organization, organizationEntity);

        final OrganizationEntity mergedEntity = organizationRepository.mergeAndFlush(organizationEntity);

        final OrganizationJson snapshot = OrganizationJson.valueOf(mergedEntity);
        organizationEvent.fire(new OrganizationEvent.Updated(snapshot, user.getId()));
        if (contractorRelevantChange) {
            organizationEvent.fire(new OrganizationEvent.ContactDataChanged(snapshot, user.getId()));
        }

        return mergedEntity;
    }

    private OrganizationEntity mergeOrganizationFields(final OrganizationModel model, final OrganizationEntity entity) {
        if (model.getName() != null) {
            entity.setName(model.getName());
        }
        if (model.getPhone() != null) {
            // an empty phone number removes it
            entity.setPhone(model.getPhone().isEmpty() ? null : model.getPhone());
        }
        if (model.getTrade() != null) {
            entity.setTrade(model.getTrade());
        }
        if (model.getVatIdentificationNumber() != null) {
            entity.setVatIdentificationNumber(model.getVatIdentificationNumber());
        }
        if (model.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(model.getAddress(), entity.getAddress()));
        }
        return entity;
    }


    /**
     * Delete the organization with the given id. Fails if the organization is the only active proprietor of a
     * project or the managing organization of a project: these projects have to be deleted or transferred first.
     *
     * @param id id of the organization which should be deleted
     * @return true if the organization was deleted successfully
     * @throws ProprietorRequiredException if the organization is still required by a project
     */
    @Transactional
    public boolean deleteOrganization(final UUID id) {

        logger.infov("Deleting Organization (id={0})", id);

        OrganizationEntity organization = organizationRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Organization not found"));
        if (projectRepository.existsByManagingOrganizationId(id)) {
            throw new ProprietorRequiredException("The organization is the managing organization of at least one "
                + "project. Change the managing organization or delete these projects before deleting the "
                + "organization.");
        }
        final List<ProjectEntity> proprietorProjects = projectRepository
            .findProjectsByOrganizationIdAndRole(id, MemberRole.PROPRIETOR);
        final boolean soleProprietor = proprietorProjects.stream()
            .anyMatch(project -> project.countProprietors(null, id) == 0);
        if (soleProprietor) {
            throw new ProprietorRequiredException("The organization is the only proprietor of at least one project. "
                + "Transfer the ownership or delete these projects before deleting the organization.");
        }
        // The loaded projects (and their project organizations) still reference the organization. Detach them,
        // otherwise removing the organization fails on flush; the database cleans up the references
        // (ON DELETE CASCADE).
        proprietorProjects.forEach(projectRepository.getEntityManager()::detach);
        final OrganizationJson snapshot = OrganizationJson.valueOf(organization);
        final boolean deleted = organizationRepository.deleteById(organization.getId());
        if (deleted) {
            organizationEvent.fire(new OrganizationEvent.Deleted(snapshot, actingUser.getId()));
        }
        return deleted;
    }

    /**
     * Retrieves the employee role of a user within the requested organization
     * @param organizationId id of the organization
     * @param user employee of the organization
     * @return role of the employee
     */
    public EmployeeRole getEmployeeRole(final UUID organizationId, final UserModel user) {

        logger.infov("Retrieving Employee Role (id={0}, user={1})", organizationId, user.getName());

        return organizationRepository.findOrganizationEmployeeByOrganizationIdAndUserId(organizationId, user.getId())
                .map(OrganizationEmployeeEntity::getRole)
                .orElseThrow(() -> new ForbiddenException("Organization not exist or user is not an employee"));
    }

    public OrganizationEmployeeModel getOrganizationEmployee(final UUID organizationId, final UUID employeeId) {
        return organizationRepository.findOrganizationEmployeeByOrganizationIdAndUserId(organizationId, employeeId)
                .orElseThrow(() -> new NotFoundException("Organization not exist or user is not an employee"));
    }

    public List<? extends OrganizationEmployeeModel> getEmployeesByOrganization(final UUID organizationId) {
        return organizationRepository.findOrganizationEmployeesByOrganizationId(organizationId);
    }

    /**
     * Retrieve all organization memberships for the given user, including the user's role in each.
     *
     * @param user the authenticated user
     * @return list of organization employee entries (org + role + user info)
     */
    public List<OrganizationEmployeeEntity> getOrganizationEmployments(final UserModel user) {
        logger.infov("Retrieving organization employments for user {0}", user.getId());
        return organizationRepository.findOrganizationEmployeesByUserId(user.getId());
    }

    @Transactional
    public OrganizationEmployeeModel addEmployee(final UUID organizationId, final UserModel user,
        final OrganizationEmployeeModel employee) {

        final OrganizationEntity organization = organizationRepository
            .findOrganizationByUserId(user.getId(), organizationId)
            .orElseThrow(() -> new NotFoundException("Organization not exist or user is not an employee"));

        UserEntity userEntity = userController.findOrCreateUser(employee);
        organization.addEmployee(userEntity, employee.getEmployeeRole());
        organizationRepository.mergeAndFlush(organization);
        final OrganizationEmployeeEntity added = organizationRepository
            .findOrganizationEmployeeByOrganizationIdAndUserId(organizationId, userEntity.getId())
            .orElseThrow(() -> new NotFoundException("Organization not exist or user is not an employee"));
        organizationEmployeeEvent.fire(new OrganizationEmployeeEvent.Added(organizationId,
            OrganizationEmployeeJson.valueOf(added), user.getId()));
        return added;
    }

    /**
     * Changes the role of an employee. The last active OWNER or MANAGER of an organization can not be demoted.
     *
     * @throws OrganizationAdminRequiredException if no active OWNER or MANAGER would remain
     */
    @Transactional
    public OrganizationEmployeeModel updateEmployeeRole(final UUID organizationId, final UUID employeeId,
        final EmployeeRole role) {
        OrganizationEmployeeEntity entity = organizationRepository
            .findOrganizationEmployeeByOrganizationIdAndUserId(organizationId, employeeId)
            .orElseThrow(() -> new NotFoundException("Organization not exist or user is not an employee"));
        if (isAdmin(entity.getRole()) && !isAdmin(role)) {
            ensureAdminRemains(entity.getOrganization(), employeeId);
        }

        entity.setRole(role);

        final OrganizationEmployeeEntity merged = organizationRepository.merge(entity);
        organizationEmployeeEvent.fire(new OrganizationEmployeeEvent.RoleChanged(organizationId,
            OrganizationEmployeeJson.valueOf(merged), actingUser.getId()));
        return merged;
    }

    /**
     * Removes an employee from the organization. The last active OWNER or MANAGER of an organization can not be
     * removed, the organization has to be deleted instead.
     *
     * @throws OrganizationAdminRequiredException if no active OWNER or MANAGER would remain
     */
    @Transactional
    public void removeEmployee(final UUID organizationId, final UUID employeeId) {
        final Optional<OrganizationEmployeeEntity> employee = organizationRepository
            .findOrganizationEmployeeByOrganizationIdAndUserId(organizationId, employeeId);
        if (employee.isEmpty()) {
            return;
        }
        if (isAdmin(employee.get().getRole())) {
            ensureAdminRemains(employee.get().getOrganization(), employeeId);
        }
        final OrganizationEmployeeJson snapshot = OrganizationEmployeeJson.valueOf(employee.get());
        organizationRepository.deleteOrganizationEmployeesByOrganizationIdAndUserId(organizationId, employeeId);
        organizationEmployeeEvent.fire(
            new OrganizationEmployeeEvent.Removed(organizationId, snapshot, actingUser.getId()));
    }

    private static boolean isAdmin(final EmployeeRole role) {
        return role == EmployeeRole.OWNER || role == EmployeeRole.MANAGER;
    }

    private static void ensureAdminRemains(final OrganizationEntity organization, final UUID excludedUserId) {
        if (!organization.hasActiveAdmin(excludedUserId)) {
            throw new OrganizationAdminRequiredException("An organization must always have an active employee "
                + "with role OWNER or MANAGER. Delete the organization instead.");
        }
    }
}
