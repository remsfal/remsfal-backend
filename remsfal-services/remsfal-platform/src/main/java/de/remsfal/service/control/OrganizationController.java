package de.remsfal.service.control;

import de.remsfal.core.model.OrganizationModel;
import de.remsfal.service.entity.dao.OrganizationRepository;
import de.remsfal.service.entity.dto.OrganizationEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RequestScoped
public class OrganizationController {

    //TODO: Implement logger

    @Inject
    Logger logger;

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    AddressController addressController;

    /**
     * Retrieve an organization by id
     *
     * @param id id of the organization
     * @return organization entity
     */
    public OrganizationEntity getOrganizationById(final UUID id) {
        logger.infov("Retrieve Organization by id {0}", id);
        return organizationRepository.findByIdOptional(id).orElseThrow(() -> new  NotFoundException("Organization not found"));
    }

    /**
     * Retrieve all organizations
     *
     * @return list of all organizations
     */
    public List<OrganizationEntity> getOrganizations() {
        return organizationRepository.findAll().list();
    }

    /**
     * Create an organization
     *
     * @param organization the organization
     * @return the created organization entity
     */
    @Transactional
    public OrganizationEntity createOrganization(final OrganizationModel organization) {

        OrganizationEntity organizationEntity = new OrganizationEntity();

        organizationEntity.generateId();
        organizationEntity.setName(organization.getName());
        organizationEntity.setEmail(organization.getEmail());
        organizationEntity.setPhone(organization.getPhone());
        organizationEntity.setTrade(organization.getTrade());

        if (organization.getAddress() != null) {
            organizationEntity.setAddress(addressController.updateAddress(organization.getAddress(), null));
        }

        organizationRepository.persistAndFlush(organizationEntity);

        return organizationEntity;
    }

    /**
     * Update an organization
     *
     * @param organization The organization entity containing the updated values
     * @return The updated organization
     */
    @Transactional
    public OrganizationEntity updateOrganization(final UUID organizationId, final OrganizationModel organization) {

        OrganizationEntity organizationEntity = organizationRepository.findByIdOptional(organizationId).orElseThrow(() -> new NotFoundException("Organization id not found"));

        if (organization.getName() != null) {
            organizationEntity.setName(organization.getName());
        }

        if (organization.getEmail() != null) {
            organizationEntity.setEmail(organization.getEmail());
        }

        if (organization.getPhone() != null) {
            organizationEntity.setPhone(organization.getPhone());
        }

        if (organization.getTrade() != null) {
            organizationEntity.setTrade(organization.getTrade());
        }

        if (organization.getAddress() != null) {
            organizationEntity.setAddress(addressController.updateAddress(organization.getAddress(), null));
        }

        return organizationRepository.mergeAndFlush(organizationEntity);
    }

    /**
     * Delete the organization with the given id
     *
     * @param id id of the organization which should be deleted
     * @return true if the organization was deleted successfully
     */
    @Transactional
    public boolean deleteOrganization(final UUID id) {
        OrganizationEntity organization = organizationRepository.findByIdOptional(id).orElseThrow(() -> new NotFoundException("Organization not found"));
        return organizationRepository.deleteById(organization.getId());
    }
}
