package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.json.project.TenantJson;

class IssueEntityTest {

    @Test
    void testGetTenantUpdate_returnsNullWhenNotSet() {
        final IssueEntity entity = new IssueEntity();

        assertNull(entity.getTenantUpdate());
    }

    @Test
    void testSetTenantUpdate_null_clearsColumn() {
        final IssueEntity entity = new IssueEntity();
        entity.setTenantUpdate((TenantJson) null);

        assertNull(entity.getTenantUpdate());
    }

    @Test
    void testSetAndGetTenantUpdate_roundTripsThroughJson() {
        final UUID tenantId = UUID.randomUUID();
        final TenantJson tenantUpdate = ImmutableTenantJson.builder()
            .id(tenantId)
            .firstName("Max")
            .lastName("Mustermann")
            .mobilePhoneNumber("+491234567890")
            .build();

        final IssueEntity entity = new IssueEntity();
        entity.setTenantUpdate(tenantUpdate);

        final TenantJson result = entity.getTenantUpdate();
        assertEquals(tenantId, result.getId());
        assertEquals("Max", result.getFirstName());
        assertEquals("Mustermann", result.getLastName());
        assertEquals("+491234567890", result.getMobilePhoneNumber());
    }

    @Test
    void testSetTenantUpdateString_forCassandraMapping_isReadableAsTenantJson() {
        final UUID tenantId = UUID.randomUUID();
        final IssueEntity entity = new IssueEntity();
        entity.setTenantUpdate("{\"id\":\"" + tenantId + "\",\"firstName\":\"Erika\"}");

        final TenantJson result = entity.getTenantUpdate();
        assertEquals(tenantId, result.getId());
        assertEquals("Erika", result.getFirstName());
    }

    @Test
    void testGetTenantUpdate_invalidJson_throwsIllegalStateException() {
        final IssueEntity entity = new IssueEntity();
        entity.setTenantUpdate("not-valid-json");

        assertThrows(IllegalStateException.class, entity::getTenantUpdate);
    }

}
