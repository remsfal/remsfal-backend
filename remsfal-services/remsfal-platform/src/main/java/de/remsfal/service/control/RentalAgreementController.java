package de.remsfal.service.control;

import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.RentalAgreementRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.service.entity.dto.UserEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class RentalAgreementController {

    @Inject
    Logger logger;

    @Inject
    RentalAgreementRepository rentalAgreementRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    public List<RentalAgreementEntity> getRentalAgreements(final UserModel tenant) {
        logger.infov("Retrieving all rental agreements (tenantId = {0})", tenant.getId());
        return rentalAgreementRepository.findRentalAgreementsByTenant(tenant.getId());
    }

    public RentalAgreementEntity getRentalAgreement(final UserModel tenant, final UUID agreementId) {
        logger.infov("Retrieving a rental agreement (tenantId = {0}, agreementId = {1})",
            tenant.getId(), agreementId);
        return rentalAgreementRepository.findRentalAgreementByTenant(tenant.getId(), agreementId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));
    }

    public List<RentalAgreementEntity> getRentalAgreementsByProject(final UUID projectId) {
        logger.infov("Retrieving all rental agreements (projectId = {0})", projectId);
        return rentalAgreementRepository.findRentalAgreementByProject(projectId);
    }

    public RentalAgreementEntity getRentalAgreementByProject(final UUID projectId, final UUID agreementId) {
        logger.infov("Retrieving a rental agreement (projectId = {0}, agreementId = {1})", projectId, agreementId);
        return rentalAgreementRepository.findRentalAgreementByProject(projectId, agreementId)
            .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));
    }

    @Transactional
    public RentalAgreementEntity createRentalAgreement(final UUID projectId, final RentalAgreementModel agreement) {
        logger.infov("Creating a rental agreement (project={0}", projectId);

        if (projectRepository.findById(projectId) == null) {
            throw new NotFoundException("Project not exist");
        }

        RentalAgreementEntity entity = new RentalAgreementEntity();
        entity.generateId();
        entity.setProjectId(projectId);
        entity.setStartOfRental(agreement.getStartOfRental());
        entity.setEndOfRental(agreement.getEndOfRental());

        final List<? extends CustomerModel> tenants = agreement.getTenants();
        if (tenants != null && !tenants.isEmpty()) {
            List<UserEntity> userEntities = tenants.stream()
                .map(user -> userRepository.findByIdWithAdditionalEmails(user.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            if (userEntities.size() != tenants.size()) {
                throw new BadRequestException("One or more users not found");
            }
            entity.setTenants(userEntities);
        }

        rentalAgreementRepository.persistAndFlush(entity);
        return entity;
    }

    @Transactional
    public RentalAgreementEntity updateRentalAgreement(final UUID projectId, final UUID agreementId,
            final RentalAgreementModel agreement) {
        logger.infov("Updating a rental agreement (projectId={0}, agreementId={1})", projectId, agreementId);
        final RentalAgreementEntity entity =
            rentalAgreementRepository.findRentalAgreementByProject(projectId, agreementId)
                .orElseThrow(() -> new NotFoundException("Rental agreement not exist"));

        if (agreement.getStartOfRental() != null) {
            entity.setStartOfRental(agreement.getStartOfRental());
        }
        if (agreement.getEndOfRental() != null) {
            entity.setEndOfRental(agreement.getEndOfRental());
        }

        final List<? extends CustomerModel> tenants = agreement.getTenants();
        if (tenants != null) {
            List<UserEntity> userEntities = tenants.stream()
                .map(user -> userRepository.findByIdWithAdditionalEmails(user.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            if (userEntities.size() != tenants.size()) {
                throw new BadRequestException("One or more users not found");
            }
            entity.setTenants(userEntities);
        }

        return rentalAgreementRepository.merge(entity);
    }

}
