package de.remsfal.service.control;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;

@QuarkusTest
class UserControllerTest extends AbstractTest {

  @Inject
  UserController controller;

  @Test
  void createUser_SUCCESS_simpleUserCreated() {
    final UserModel userRequest =
        ImmutableUserJson.builder().name(TestData.USER_NAME).email(TestData.USER_EMAIL).build();

    UserModel user = controller.createUser(userRequest);
    assertNotNull(user);
    assertEquals(36, user.getId().length());
    assertEquals(TestData.USER_NAME, user.getName());
    assertEquals(TestData.USER_EMAIL, user.getEmail());

    final String userId = entityManager
        .createQuery("SELECT user.id FROM UserEntity user where user.email = :email", String.class)
        .setParameter("email", TestData.USER_EMAIL)
        .getSingleResult();
    assertEquals(user.getId(), userId);
  }

  @Test
  void getUser_SUCCESS_retrieveUser() {
    final String userId = UUID.randomUUID().toString();
    runInTransaction(() -> entityManager
        .createNativeQuery("INSERT INTO USER (ID, NAME, EMAIL) VALUES (?,?,?)")
        .setParameter(1, userId)
        .setParameter(2, TestData.USER_NAME)
        .setParameter(3, TestData.USER_EMAIL)
        .executeUpdate());

    UserModel user = controller.getUser(userId);
    assertNotNull(user);
    assertEquals(userId, user.getId());
    assertEquals(TestData.USER_NAME, user.getName());
    assertEquals(TestData.USER_EMAIL, user.getEmail());
  }

}
