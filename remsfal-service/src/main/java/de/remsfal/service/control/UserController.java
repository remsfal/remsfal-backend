package de.remsfal.service.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.Date;
import java.util.Optional;

import org.jboss.logging.Logger;

import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.authentication.AuthenticationEvent;
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
    AddressController addressController;

    @Inject
    private Event<AuthenticationEvent> authenticatedUser;

    @Transactional
    public UserModel authenticateUser(final String googleId, final String email) {
        logger.infov("Authenticating a user (googleId={0}, email={1})", googleId, email);

        final Optional<UserEntity> entity = repository.findByTokenId(googleId);
        if(entity.isPresent()) {
            authenticatedUser.fireAsync(new AuthenticationEvent(googleId, email));
            return entity.get();
        }

        return createUser(googleId, email);
    }

    @Transactional
    protected UserModel createUser(final String googleId, final String email) {
        logger.infov("Creating a new user (googleId={0}, email={1})", googleId, email);
        final UserEntity entity = new UserEntity();
        entity.generateId();
        entity.setTokenId(googleId);
        entity.setEmail(email.toLowerCase());
        entity.setAuthenticatedAt(new Date());
        try {
            repository.persistAndFlush(entity);
            return entity;
        } catch (PersistenceException e) {
            throw new AlreadyExistsException("Unable to create user", e);
        }
    }

    public UserEntity getUser(final String userId) {
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
    public CustomerModel updateUser(final String userId, final CustomerModel user) {
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
            entity.setAddress(addressController.updateAddress(entity.getAddress(), user.getAddress()));
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
        return repository.merge(entity);
    }
    
    public void onPrincipalAuthentication(@ObservesAsync final AuthenticationEvent event) {
        logger.infov("Updating authentication timestamp of user (googleId={0}, email={1})",
                event.getGoogleId(), event.getEmail());
        try {
            repository.updateAuthenticatedAt(event.getGoogleId(), event.getAuthenticatedAt());
        } catch (Exception e) {
            logger.error("Unable to update authentication timestamp", e);
        }
    }

    @Transactional
    public boolean deleteUser(final String userId) {
        logger.infov("Deleting a user (id = {0})", userId);
        return repository.remove(userId);
    }

}
