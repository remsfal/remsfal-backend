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
import java.util.Set;
import java.util.Objects;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.eventing.UserEventProducer;
import de.remsfal.service.control.exception.AlreadyExistsException;
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

    @Inject
    UserEventProducer userEventProducer;

    private static final String DEFAULT_LOCALE = "de";
    private static final int ADDITIONAL_EMAIL_VERIFICATION_TOKEN_VALID_HOURS = 24;

    @Transactional
    protected UserModel createUser(final String googleId, final String email) {
        logger.infov("Creating a new user (googleId={0}, email={1})", googleId, email);
        final UserEntity entity = new UserEntity();
        entity.generateId();
        entity.setTokenId(googleId);
        if (additionalEmailRepository.existsByEmail(email.toLowerCase())) {
            throw new AlreadyExistsException("Unable to create user");
        }
        entity.setEmail(email.toLowerCase());
        entity.setLocale(DEFAULT_LOCALE);
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
        final UserEntity entity = repository.findByIdWithAdditionalEmails(userId)
            .orElseThrow(() -> new NotFoundException("User not exist"));

        if (user.getFirstName() != null) {
            entity.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            entity.setLastName(user.getLastName());
        }
        if (user.getAddress() != null) {
            entity.setAddress(addressController.updateAddress(user.getAddress(), entity.getAddress()));
        }
        if (user.getMobilePhoneNumber() != null) {
            entity.setMobilePhoneNumber(user.getMobilePhoneNumber());
        }
        if (user.getBusinessPhoneNumber() != null) {
            entity.setBusinessPhoneNumber(user.getBusinessPhoneNumber());
        }
        if (user.getPrivatePhoneNumber() != null) {
            entity.setPrivatePhoneNumber(user.getPrivatePhoneNumber());
        }
        if (user.getLocale() != null) {
            entity.setLocale(user.getLocale());
        }
        if (user.getPlaceOfBirth() != null) {
            entity.setPlaceOfBirth(user.getPlaceOfBirth());
        }
        if (user.getDateOfBirth() != null) {
            entity.setDateOfBirth(user.getDateOfBirth());
        }
        if (user.getAdditionalEmails() != null) {
            final List<AdditionalEmailEntity> createdEmails = syncAdditionalEmails(entity, user.getAdditionalEmails());
            final UserEntity mergedEntity = repository.merge(entity);
            createdEmails.forEach(additionalEmail -> notificationController.informUserAboutAdditionalEmailVerification(
                mergedEntity,
                additionalEmail.getEmail(),
                additionalEmail.getVerificationToken()
            ));
            return mergedEntity;
        }
        return repository.merge(entity);
    }
    
    @Transactional
    public boolean deleteUser(final UUID userId) {
        logger.infov("Deleting a user (id = {0})", userId);
        final boolean deleted = repository.remove(userId);
        if (deleted) {
            userEventProducer.sendUserDeleted(userId);
        }
        return deleted;
    }

    public boolean isVerifiedEmailForUser(final UUID userId, final String email) {
        final String normalized = email.trim().toLowerCase();
        final UserEntity entity = repository.findByIdWithAdditionalEmails(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        if (normalized.equalsIgnoreCase(entity.getEmail())) {
            return true;
        }
        return entity.getAdditionalEmailEntities().stream()
            .anyMatch(ae -> ae.getEmail().equalsIgnoreCase(normalized) && ae.isVerified());
    }

    @Transactional
    public void verifyAdditionalEmail(final String verificationToken) {
        if (verificationToken == null || verificationToken.isBlank()) {
            throw new BadRequestException("Verification token is missing.");
        }
        final AdditionalEmailEntity additionalEmail = additionalEmailRepository
            .findByVerificationToken(verificationToken)
            .orElseThrow(() -> new NotFoundException("Verification token is invalid."));
        final LocalDateTime expiresAt = additionalEmail.getVerificationTokenExpiresAt();
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired.");
        }
        additionalEmail.setVerified(true);
        additionalEmail.setVerificationToken(null);
        additionalEmail.setVerificationTokenExpiresAt(null);
    }

    private List<AdditionalEmailEntity> syncAdditionalEmails(UserEntity user, List<String> newEmailsRaw) {
        List<String> newEmails = newEmailsRaw.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .distinct()
            .toList();

        if (newEmails.stream().anyMatch(email -> repository.findByEmail(email).isPresent())) {
            throw new AlreadyExistsException(
                "Alternative email must not be equal to an existing login email."
            );
        }

        Set<AdditionalEmailEntity> currentEmails = user.getAdditionalEmailEntities();
        currentEmails.removeIf(ae -> !newEmails.contains(ae.getEmail()));
        List<AdditionalEmailEntity> createdEmails = new ArrayList<>();

        Set<String> existingEmails = currentEmails.stream()
            .map(AdditionalEmailEntity::getEmail)
            .collect(Collectors.toSet());

        for (String email : newEmails) {

            if (existingEmails.contains(email)) {
                continue;
            }

            if (additionalEmailRepository.existsByEmail(email)) {
                throw new AlreadyExistsException(
                    "Alternative email already exists."
                );
            }

            AdditionalEmailEntity ae = new AdditionalEmailEntity();
            ae.generateId();
            ae.setUser(user);
            ae.setEmail(email);
            ae.setVerified(false);
            ae.setVerificationToken(UUID.randomUUID().toString());
            ae.setVerificationTokenExpiresAt(LocalDateTime.now()
                .plusHours(ADDITIONAL_EMAIL_VERIFICATION_TOKEN_VALID_HOURS));

            currentEmails.add(ae);
            createdEmails.add(ae);
        }
        return createdEmails;
    }

}
