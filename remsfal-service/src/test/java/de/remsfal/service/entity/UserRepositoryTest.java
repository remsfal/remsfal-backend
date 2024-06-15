package de.remsfal.service.entity;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserEntity;

@QuarkusTest
class UserRepositoryTest extends AbstractTest {

    @Inject
    UserRepository repository;

    @Test
    void hashCode_SUCCESS_userEntity() {
        final UserEntity entity = new UserEntity();
        entity.generateId();
        assertEquals(Objects.hash(entity.getId()), entity.hashCode());
    }
    
    @Test
    void equals_SUCCESS_userEntity() {
        final UserEntity entity = new UserEntity();
        entity.generateId();
        entity.setTokenId(TestData.USER_TOKEN);
        entity.setEmail(TestData.USER_EMAIL);
        entity.setFirstName(TestData.USER_FIRST_NAME);
        entity.setLastName(TestData.USER_LAST_NAME);
        
        final UserEntity copy = new UserEntity();
        copy.setId(entity.getId());
        copy.setTokenId(TestData.USER_TOKEN);
        copy.setEmail(TestData.USER_EMAIL);
        copy.setFirstName(TestData.USER_FIRST_NAME);
        copy.setLastName(TestData.USER_LAST_NAME);
        
        assertTrue(entity.equals(copy));
    }
    
}
