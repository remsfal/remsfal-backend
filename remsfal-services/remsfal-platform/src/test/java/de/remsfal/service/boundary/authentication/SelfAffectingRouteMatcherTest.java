package de.remsfal.service.boundary.authentication;

import java.util.UUID;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class SelfAffectingRouteMatcherTest {

    @Test
    void alwaysSelfAffectingRoutesIgnoreActorId() {
        assertTrue(SelfAffectingRouteMatcher.isForcedRenewalRequest("POST", "/api/v1/organizations", null));
        assertTrue(SelfAffectingRouteMatcher.isForcedRenewalRequest(
            "DELETE", "/api/v1/organizations/" + UUID.randomUUID(), null));
        assertTrue(SelfAffectingRouteMatcher.isForcedRenewalRequest("POST", "/api/v1/projects", null));
        assertTrue(SelfAffectingRouteMatcher.isForcedRenewalRequest(
            "DELETE", "/api/v1/projects/" + UUID.randomUUID(), null));
    }

    @Test
    void parameterizedRouteMatchesWhenActorEqualsPathParam() {
        UUID actorId = UUID.randomUUID();
        String path = "/api/v1/organizations/" + UUID.randomUUID() + "/employees/" + actorId;
        assertTrue(SelfAffectingRouteMatcher.isForcedRenewalRequest("PATCH", path, actorId));
        assertTrue(SelfAffectingRouteMatcher.isForcedRenewalRequest("DELETE", path, actorId));
    }

    @Test
    void parameterizedRouteDoesNotMatchWhenActorDiffersFromPathParam() {
        UUID actorId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        String path = "/api/v1/projects/" + UUID.randomUUID() + "/members/" + otherId;
        assertFalse(SelfAffectingRouteMatcher.isForcedRenewalRequest("PATCH", path, actorId));
        assertFalse(SelfAffectingRouteMatcher.isForcedRenewalRequest("DELETE", path, actorId));
    }

    @Test
    void parameterizedRouteDoesNotMatchWhenActorIdUnknown() {
        String path = "/api/v1/projects/" + UUID.randomUUID() + "/members/" + UUID.randomUUID();
        assertFalse(SelfAffectingRouteMatcher.isForcedRenewalRequest("DELETE", path, null));
    }

    @Test
    void malformedPathParamDoesNotMatch() {
        UUID actorId = UUID.randomUUID();
        String path = "/api/v1/projects/" + UUID.randomUUID() + "/members/not-a-uuid";
        assertFalse(SelfAffectingRouteMatcher.isForcedRenewalRequest("DELETE", path, actorId));
    }

    @Test
    void nonMatchingMethodOrPathReturnsFalse() {
        assertFalse(SelfAffectingRouteMatcher.isForcedRenewalRequest("GET", "/api/v1/organizations", null));
        assertFalse(SelfAffectingRouteMatcher.isForcedRenewalRequest("POST", "/api/v1/some/other/path", null));
        assertFalse(SelfAffectingRouteMatcher.isForcedRenewalRequest(
            "PATCH", "/api/v1/organizations/" + UUID.randomUUID(), null));
    }

}
