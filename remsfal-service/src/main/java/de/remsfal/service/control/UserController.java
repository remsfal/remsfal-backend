package de.remsfal.service.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.Date;

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
    private Event<AuthenticationEvent> authenticatedUser;

    @Transactional
    public UserModel authenticateUser(final String googleId, final String email) {
        logger.infov("Authenticating a user (googleId={0}, email={1})", googleId, email);
        try {
            final UserEntity entity = repository.findByTokenId(googleId);
            authenticatedUser.fireAsync(new AuthenticationEvent(googleId, email));
            return entity;
        } catch (NoResultException e) {
            return createUser(googleId, email);
        }
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

    public CustomerModel getUser(final String userId) {
        logger.infov("Retrieving a user (id = {0})", userId);
        final UserEntity user = repository.findById(userId);
        if(user == null) {
            throw new NotFoundException("User not exist");
        }
        return user;
    }

    @Transactional
    public CustomerModel updateUser(final String userId, final UserModel user) {
        logger.infov("Updating a user ({0})", user);
        final UserEntity entity = repository.findById(userId);
        if(user.getName() != null) {
            entity.setName(user.getName());
        }
        if(user.getEmail() != null) {
            entity.setEmail(user.getEmail());
        }
        return repository.merge(entity);
    }
    
    public void onPrincipalAuthentication(@ObservesAsync final AuthenticationEvent event) {
        logger.infov("Updating authentication timestamp of user (googleId={0}, email={1})", event.getGoogleId(), event.getEmail());
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
