package de.remsfal.core.json;

import de.remsfal.core.json.organization.OrganizationJson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class OrganizationJsonTest {

    @Test
    void valueOfReturnsNullForNullModel() {
        assertNull(OrganizationJson.valueOf(null));
    }
}
