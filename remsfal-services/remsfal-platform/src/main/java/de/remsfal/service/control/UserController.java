package de.remsfal.service.control;

import de.remsfal.service.entity.dao.AdditionalEmailRepository;
import de.remsfal.service.entity.dto.AdditionalEmailEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.HashSet;

import org.jboss.logging.Logger;

import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.exception.AlreadyExistsException;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class UserController {
    
    @Inject
    Logger logger;

    @Inject
    UserRepository repository;

    @Inject
    AdditionalEmailRepository additionalEmailRepository;
    
    @Inject
    AddressController addressController;

    @Inject
    NotificationController notificationController;

    @Transactional
    protected UserModel createUser(final String googleId, final String email) {
        logger.infov("Creating a new user (googleId={0}, email={1})", googleId, email);
        final UserEntity entity = new UserEntity();
        entity.generateId();
        entity.setTokenId(googleId);
        entity.setEmail(email.toLowerCase());
        entity.setAuthenticatedAt(LocalDateTime.now());
        try {
            repository.persistAndFlush(entity);
            logger.infov("entity: " + entity);
            notificationController.informUserAboutRegistration(entity);
            return entity;
        } catch (PersistenceException e) {
            throw new AlreadyExistsException("Unable to create user", e);
        }
    }

    public UserEntity getUser(final UUID userId) {
        logger.infov("Retrieving a user (id = {0})", userId);
        return repository.findByIdOptional(userId).orElseThrow(() -> new NotFoundException("User does not exist"));
    }

    @Transactional(TxType.MANDATORY)
    public UserEntity findOrCreateUser(final UserModel user) {
        if (user.getId() != null) {
            return repository.findByIdOptional(user.getId())
                .orElseThrow(() -> new NotFoundException("User does not exist"));
        } else if (user.getEmail() != null) {
            Optional<UserEntity> userByEmail = repository.findByEmail(user.getEmail());
            if (userByEmail.isPresent()) {
                return userByEmail.get();
            }

            UserEntity userEntity = new UserEntity();
            userEntity.generateId();
            userEntity.setEmail(user.getEmail());
            repository.persist(userEntity);
            return userEntity;
        } else {
            throw new BadRequestException("Project member's email is missing");
        }
    }

    @Transactional
    public CustomerModel updateUser(final UUID userId, final CustomerModel user) {
        logger.infov("Updating a user ({0})", user);
        final UserEntity entity = repository.findByIdOptional(userId)
            .orElseThrow(() -> new NotFoundException("User not exist"));

        if(user.getFirstName() != null) {
            entity.setFirstName(user.getFirstName());
        }
        if(user.getLastName() != null) {
            entity.setLastName(user.getLastName());
        }
        if(user.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(user.getAddress(), entity.getAddress()));
        }
        if(user.getMobilePhoneNumber() != null) {
            entity.setMobilePhoneNumber(user.getMobilePhoneNumber());
        }
        if(user.getBusinessPhoneNumber() != null) {
            entity.setBusinessPhoneNumber(user.getBusinessPhoneNumber());
        }
        if(user.getPrivatePhoneNumber() != null) {
            entity.setPrivatePhoneNumber(user.getPrivatePhoneNumber());
        }
        if(user.getLocale() != null) {
            entity.setLocale(user.getLocale());
        } else{
            entity.setLocale("de");
        }
        if(user.getAdditionalEmails() != null) {
            syncAdditionalEmails(entity, user.getAdditionalEmails());
        }
        return repository.merge(entity);
    }
    
    @Transactional
    public boolean deleteUser(final UUID userId) {
        logger.infov("Deleting a user (id = {0})", userId);
        return repository.remove(userId);
    }

    private void syncAdditionalEmails(final UserEntity user, final List<String> newEmailsRaw) {

        List<String> newEmails = newEmailsRaw.stream()
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .distinct()
            .toList();

        List<AdditionalEmailEntity> currentEmails = additionalEmailRepository.findByUserId(user.getId());

        for (AdditionalEmailEntity ae : currentEmails) {
            final String email = ae.getEmail();
            if (!newEmails.contains(email)) {
                additionalEmailRepository.remove(ae.getId());
            }
        }

        for (String email : newEmails) {

            if (email.equalsIgnoreCase(user.getEmail())) {
                throw new AlreadyExistsException("Alternative email must not be equal to the login email.");
            }

            boolean exists = currentEmails.stream()
                .anyMatch(ae -> ae.getEmail().equalsIgnoreCase(email));

            if (!exists) {
                AdditionalEmailEntity ae = new AdditionalEmailEntity();
                ae.generateId();
                ae.setUser(user);
                ae.setEmail(email);
                ae.setVerified(false);

                additionalEmailRepository.persist(ae);
            }
        }

        user.setAdditionalEmails(new HashSet<>(additionalEmailRepository.findByUserId(user.getId())));
    }



}
