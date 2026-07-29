package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
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
    void testSetAndGetTenantUpdate_withDateOfBirth_roundTripsThroughJson() {
        // regression test: OBJECT_MAPPER previously lacked JavaTimeModule, so a
        // non-null LocalDate field made serialization throw IllegalStateException
        final LocalDate dateOfBirth = LocalDate.of(1985, 3, 21);
        final TenantJson tenantUpdate = ImmutableTenantJson.builder()
            .id(UUID.randomUUID())
            .firstName("Max")
            .lastName("Mustermann")
            .dateOfBirth(dateOfBirth)
            .build();

        final IssueEntity entity = new IssueEntity();
        entity.setTenantUpdate(tenantUpdate);

        assertEquals(dateOfBirth, entity.getTenantUpdate().getDateOfBirth());
    }

    @Test
    void testSetTenantUpdateJson_forCassandraMapping_isReadableAsTenantJson() {
        final UUID tenantId = UUID.randomUUID();
        final IssueEntity entity = new IssueEntity();
        entity.setTenantUpdateJson("{\"id\":\"" + tenantId + "\",\"firstName\":\"Erika\"}");

        final TenantJson result = entity.getTenantUpdate();
        assertEquals(tenantId, result.getId());
        assertEquals("Erika", result.getFirstName());
    }

    @Test
    void testGetTenantUpdate_invalidJson_throwsIllegalStateException() {
        final IssueEntity entity = new IssueEntity();
        entity.setTenantUpdateJson("not-valid-json");

        assertThrows(IllegalStateException.class, entity::getTenantUpdate);
    }

}
