package de.remsfal.service.boundary;

import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.TokenInfo;
import de.remsfal.service.boundary.authentication.TokenValidator;

import static org.mockito.Mockito.when;

public abstract class AbstractResourceTest extends AbstractTest {

    @InjectMock
    TokenValidator tokenValidator;

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    void setupTestUsers() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, NAME, EMAIL) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_1)
            .setParameter(2, TestData.USER_TOKEN_1)
            .setParameter(3, TestData.USER_NAME_1)
            .setParameter(4, TestData.USER_EMAIL_1)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, NAME, EMAIL) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_2)
            .setParameter(2, TestData.USER_TOKEN_2)
            .setParameter(3, TestData.USER_NAME_2)
            .setParameter(4, TestData.USER_EMAIL_2)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, NAME, EMAIL) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_3)
            .setParameter(2, TestData.USER_TOKEN_3)
            .setParameter(3, TestData.USER_NAME_3)
            .setParameter(4, TestData.USER_EMAIL_3)
            .executeUpdate());
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, NAME, EMAIL) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID_4)
            .setParameter(2, TestData.USER_TOKEN_4)
            .setParameter(3, TestData.USER_NAME_4)
            .setParameter(4, TestData.USER_EMAIL_4)
            .executeUpdate());

        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN_1))
            .thenReturn(new TokenInfo(ImmutableUserJson.builder()
                .id(TestData.USER_TOKEN_1)
                .email(TestData.USER_EMAIL_1)
                .name(TestData.USER_NAME_1)
                .build()));
        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN_2))
            .thenReturn(new TokenInfo(ImmutableUserJson.builder()
                .id(TestData.USER_TOKEN_2)
                .email(TestData.USER_EMAIL_2)
                .name(TestData.USER_NAME_2)
                .build()));
        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN_3))
            .thenReturn(new TokenInfo(ImmutableUserJson.builder()
                .id(TestData.USER_TOKEN_3)
                .email(TestData.USER_EMAIL_3)
                .name(TestData.USER_NAME_3)
                .build()));
        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN_4))
            .thenReturn(new TokenInfo(ImmutableUserJson.builder()
                .id(TestData.USER_TOKEN_4)
                .email(TestData.USER_EMAIL_4)
                .name(TestData.USER_NAME_4)
                .build()));
    }

}