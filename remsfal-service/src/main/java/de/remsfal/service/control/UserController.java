package de.remsfal.service.control;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.ws.rs.NotFoundException;

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
    UserTransaction transaction;

    @Transactional
    public CustomerModel createUser(final UserModel tokenInfo) {
        logger.infov("Creating a new user (name={0}, email={1})", tokenInfo.getName(), tokenInfo.getEmail());
        final UserEntity entity = new UserEntity();
        entity.generateId();
        entity.setTokenId(tokenInfo.getId());
        entity.setName(tokenInfo.getName());
        entity.setEmail(tokenInfo.getEmail().toLowerCase());
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
    public CustomerModel getUserByTokenId(final String tokenId) {
        logger.infov("Retrieving an existing user (tokenId = {0})", tokenId);
        final UserEntity user = repository.findByTokenId(tokenId);
        if(user == null) {
            throw new NotFoundException("User not exist");
        }
        return user;
    }
    @Transactional
    public CustomerModel updateUser(final UserModel user) {
        logger.infov("Updating an existing user ({0})", user);
        final UserEntity entity = repository.findById(user.getId());
        if(user.getName() != null) {
            entity.setName(user.getName());
        }
        if(user.getEmail() != null) {
            entity.setEmail(user.getEmail());
        }
        return repository.merge(entity);
    }

    @Transactional
    public boolean deleteUser(final String userId) {
        logger.infov("Deleting an existing user (id = {0})", userId);
        return repository.remove(userId);
    }

}
