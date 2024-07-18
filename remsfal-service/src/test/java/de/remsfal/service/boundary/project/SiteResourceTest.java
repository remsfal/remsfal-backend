package de.remsfal.service.boundary.project;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.TestData;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import jakarta.ws.rs.core.Response.Status;

@QuarkusTest
class SiteResourceTest extends AbstractProjectResourceTest {

    static final String BASE_PATH = "/api/v1/projects/{projectId}/properties/{propertyId}/sites";

    @Override
    @BeforeEach
    protected void setupTestProperties() {
        super.setupTestUsers();
        super.setupTestProjects();
        super.setupTestProperties();
    }

    @Test
    void getSites_FAILED_noAuthentication() {
        given()
            .when()
            .get(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID)
            .then()
            .statusCode(Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void getSites_SUCCESS_emptyListIsReturned() {
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .get(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(ContentType.JSON)
            .and().body("sites.size()", Matchers.is(0));
    }

    @Test
    void createSite_SUCCESS_propertyIsCreated() {
        final String json = "{ \"title\":\"" + TestData.SITE_TITLE + "\","
            + "\"address\":{"
            + "\"street\":\"" + TestData.ADDRESS_STREET + "\","
            + "\"city\":\"" + TestData.ADDRESS_CITY + "\","
            + "\"province\":\"" + TestData.ADDRESS_PROVINCE + "\","
            + "\"zip\":\"" + TestData.ADDRESS_ZIP + "\","
            + "\"countryCode\":\"" + TestData.ADDRESS_COUNTRY + "\"}}";
        given()
            .when()
            .cookie(buildCookie(TestData.USER_ID, TestData.USER_EMAIL, Duration.ofMinutes(10)))
            .contentType(ContentType.JSON)
            .body(json)
            .post(BASE_PATH, TestData.PROJECT_ID, TestData.PROPERTY_ID)
            .then()
            .statusCode(Status.CREATED.getStatusCode())
            .contentType(ContentType.JSON)
            .header("location", Matchers.containsString(BASE_PATH.replace("{projectId}", TestData.PROJECT_ID)
                .replace("{propertyId}", TestData.PROPERTY_ID) + "/"))
            .and().body("id", Matchers.notNullValue())
            .and().body("title", Matchers.equalTo(TestData.SITE_TITLE));

        long enties = entityManager
            .createQuery("SELECT count(site) FROM SiteEntity site where site.title = :title", Long.class)
            .setParameter("title", TestData.SITE_TITLE)
            .getSingleResult();
        assertEquals(1, enties);
    }

}