package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.Test;

import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.boundary.authentication.TokenInfo;
import de.remsfal.service.boundary.authentication.TokenValidator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Inject;

@QuarkusTest
class ProjectResourceTest extends AbstractTest {
    
    @InjectMock
    TokenValidator tokenValidator;

    @Test
    void testHelloEndpoint() {
        runInTransaction(() -> entityManager
            .createNativeQuery("INSERT INTO USER (ID, TOKEN_ID, NAME, EMAIL) VALUES (?,?,?,?)")
            .setParameter(1, TestData.USER_ID)
            .setParameter(2, TestData.USER_TOKEN)
            .setParameter(3, TestData.USER_NAME)
            .setParameter(4, TestData.USER_EMAIL)
            .executeUpdate());

        when(tokenValidator.validate(anyString()))
            .thenReturn(new TokenInfo(ImmutableUserJson.builder()
                .id(TestData.USER_TOKEN)
                .email(TestData.USER_EMAIL)
                .name(TestData.USER_NAME)
                .build()));
        
        given()
            .when()
            .log().all()
            .header("Authorization", "Bearer " + TestData.USER_TOKEN)
//            .accept(ContentType.TEXT)
            .get("/api/v1/projects")
            .then()
            .log().all()
            .statusCode(200)
            .body(is("Hello RESTEasy"));
    }

}