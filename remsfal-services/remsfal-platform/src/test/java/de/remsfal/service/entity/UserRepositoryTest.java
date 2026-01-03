package de.remsfal.service.entity;

import de.remsfal.service.entity.dto.AdditionalEmailEntity;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.test.TestData;

@QuarkusTest
class UserRepositoryTest extends AbstractServiceTest {

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
        
        assertEquals(entity, copy);
    }

    @Test
    void getAdditionalEmails_SUCCESS_returnsMappedEmails() {
        final UserEntity user = new UserEntity();
        user.generateId();
        user.setTokenId(TestData.USER_TOKEN);
        user.setEmail(TestData.USER_EMAIL);

        AdditionalEmailEntity ae1 = new AdditionalEmailEntity();
        ae1.generateId();
        ae1.setEmail(TestData.ALTERNATIVE_EMAIL_1);
        ae1.setUser(user);

        AdditionalEmailEntity ae2 = new AdditionalEmailEntity();
        ae2.generateId();
        ae2.setEmail(TestData.ALTERNATIVE_EMAIL_2);
        ae2.setUser(user);

        Set<AdditionalEmailEntity> emails = new HashSet<>();
        emails.add(ae1);
        emails.add(ae2);
        user.setAdditionalEmails(emails);

        List<String> result = user.getAdditionalEmails();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(TestData.ALTERNATIVE_EMAIL_1));
        assertTrue(result.contains(TestData.ALTERNATIVE_EMAIL_2));
    }
    
}
