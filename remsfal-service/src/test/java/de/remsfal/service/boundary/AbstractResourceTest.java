package de.remsfal.service.boundary;

import io.quarkus.test.InjectMock;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.TokenInfo;
import de.remsfal.service.boundary.authentication.TokenValidator;

import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

public abstract class AbstractResourceTest extends AbstractTest {

    @InjectMock
    protected TokenValidator tokenValidator;

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
            .thenReturn(new TokenInfo(new Payload()
                .setSubject(TestData.USER_TOKEN_1)
                .setEmail(TestData.USER_EMAIL_1)
                .set("name", TestData.USER_NAME_1)));
        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN_2))
            .thenReturn(new TokenInfo(new Payload()
                .setSubject(TestData.USER_TOKEN_2)
                .setEmail(TestData.USER_EMAIL_2)
                .set("name", TestData.USER_NAME_2)));
        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN_3))
            .thenReturn(new TokenInfo(new Payload()
                .setSubject(TestData.USER_TOKEN_3)
                .setEmail(TestData.USER_EMAIL_3)
                .set("name", TestData.USER_NAME_3)));
        when(tokenValidator.validate("Bearer " + TestData.USER_TOKEN_4))
            .thenReturn(new TokenInfo(new Payload()
                .setSubject(TestData.USER_TOKEN_4)
                .setEmail(TestData.USER_EMAIL_4)
                .set("name", TestData.USER_NAME_4)));
    }

}