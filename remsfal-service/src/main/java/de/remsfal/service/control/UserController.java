package de.remsfal.service.control;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
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
    UserTransaction transaction;

    @Transactional
    public CustomerModel createUser(final UserModel tokenInfo) {
        logger.infov("Creating a new user (name={0}, email={1})", tokenInfo.getName(), tokenInfo.getEmail());
        final UserEntity entity = new UserEntity();
        entity.generateId();
        entity.setTokenId(tokenInfo.getId());
        entity.setName(tokenInfo.getName());
        entity.setEmail(tokenInfo.getEmail().toLowerCase());
        entity.setAuthenticatedAt(new Date());
        try {
            repository.persistAndFlush(entity);
            return entity;
        } catch (PersistenceException e) {
            throw new AlreadyExistsException("Unable to create user", e);
        }
    }

    public CustomerModel getUser(final String userId) {
        logger.infov("Retrieving an existing user (id = {0})", userId);
        final UserEntity user = repository.findById(userId);
        if(user == null) {
            throw new NotFoundException("User not exist");
        }
        return user;
    }

    @Transactional
    public CustomerModel updateUser(final String userId, final UserModel user) {
        logger.infov("Updating an existing user ({0})", user);
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
        logger.infov("Updating authentication timestamp of user ({0})", event.getUser());
        try {
            repository.updateAuthenticatedAt(event.getUser().getId(), event.getAuthenticatedAt());
        } catch (Exception e) {
            logger.error("Unable to update authentication timestamp", e);
        }
    }

    @Transactional
    public boolean deleteUser(final String userId) {
        logger.infov("Deleting an existing user (id = {0})", userId);
        return repository.remove(userId);
    }

}
