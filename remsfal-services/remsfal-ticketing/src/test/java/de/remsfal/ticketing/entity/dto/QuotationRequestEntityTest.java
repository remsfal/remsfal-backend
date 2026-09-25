package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;

class QuotationRequestEntityTest {

    @Test
    void testTenants_nullByDefault() {
        final QuotationRequestEntity entity = new QuotationRequestEntity();

        assertNull(entity.getTenants());
    }

    @Test
    void testSetTenants_null_clearsTenants() {
        final QuotationRequestEntity entity = new QuotationRequestEntity();
        entity.setTenants(List.of(ImmutableUserJson.builder().firstName("Erika").build()));

        entity.setTenants(null);

        assertNull(entity.getTenants());
    }

    @Test
    void testSetTenants_roundTrip() {
        final QuotationRequestEntity entity = new QuotationRequestEntity();
        final UserJson tenant = ImmutableUserJson.builder()
            .firstName("Erika")
            .lastName("Musterfrau")
            .email("erika@example.com")
            .build();

        entity.setTenants(List.of(tenant));

        final List<UserJson> tenants = entity.getTenants();
        assertEquals(1, tenants.size());
        assertEquals("Erika", tenants.get(0).getFirstName());
        assertEquals("Musterfrau", tenants.get(0).getLastName());
        assertEquals("erika@example.com", tenants.get(0).getEmail());
    }

}
