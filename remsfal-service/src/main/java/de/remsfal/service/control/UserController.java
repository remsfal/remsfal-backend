package de.remsfal.service.control;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

import org.jboss.logging.Logger;

import de.remsfal.core.model.UserModel;
import de.remsfal.service.boundary.AlreadyExistsException;
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

    public List<? extends UserModel> getUsers() {
        return repository.getAll();
    }

    public String createUser(final UserModel user) {
        logger.infov("Creating a new user (name={0}, email={1})", user.getName(), user.getEmail());
        final UserEntity entity = new UserEntity();
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        try {
            transaction.begin();
            String userId = repository.add(entity);
            transaction.commit();
            return userId;
        } catch (EntityExistsException e) {
            throw new AlreadyExistsException("Unable to create user", e);
        } catch (Exception e) {
            try {
                transaction.rollback();
            } catch (Exception ex) {
                throw new InternalServerErrorException("Unable to rollback on create user", ex);
            }
            throw new InternalServerErrorException("Unable to create user", e);
        }
    }

    public UserModel getUser(final String userId) {
        logger.infov("Retrieving an existing user (id = {0})", userId);
        final UserModel user = repository.get(userId);
        if(user == null) {
            throw new NotFoundException("User not exist");
        }
        return user;
    }

    @Transactional
    public UserModel updateUser(final UserModel user) {
        logger.infov("Updating an existing user ({0})", user);
        final UserEntity entity = repository.get(user.getId());
        if(user.getName() != null) {
            entity.setName(user.getName());
        }
        if(user.getEmail() != null) {
            entity.setEmail(user.getEmail());
        }
        return repository.set(entity);
    }

    @Transactional
    public boolean deleteUser(final String userId) {
        logger.infov("Deleting an existing user (id = {0})", userId);
        return repository.remove(userId);
    }

}
