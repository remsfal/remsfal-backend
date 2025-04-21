package de.remsfal.service.boundary;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.TestData;

import static io.restassured.RestAssured.given;

import java.time.Duration;

import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;

@QuarkusTest
class AddressResourceTest extends AbstractResourceTest {

    static final String BASE_PATH = "/api/v1/address";

    @BeforeEach
    protected void setupTestUsers() {
        super.setupTestUsers();
    }

    @Test
    void getSupportedCountries_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH + "/countries")
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getSupportedCountries_SUCCESS_germanyIsReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .get(BASE_PATH + "/countries")
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("countries", Matchers.notNullValue())
            .and().body("countries.size()", Matchers.is(1))
            .and().body("countries.countryCode", Matchers.hasItems("DE"))
            .and().body("countries.name", Matchers.hasItems("Deutschland"));
    }

    @Test
    void getPossibleCities_FAILED_noQueryParameterProvided() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .get(BASE_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getPossibleCities_FAILED_zipToShort() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .queryParam("zip", "123")
            .get(BASE_PATH)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void getPossibleCities_SUCCESS_berlinIsReturned() {
        given()
            .when()
            .cookie(buildAccessTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .cookie(buildRefreshTokenCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(100)))
            .queryParam("zip", "10318")
            .get(BASE_PATH)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("zip", Matchers.hasItems("10318"))
            .and().body("city", Matchers.hasItems("Berlin"))
            .and().body("province", Matchers.hasItems("Berlin"))
            .and().body("countryCode", Matchers.hasItems("DE"));
    }

}