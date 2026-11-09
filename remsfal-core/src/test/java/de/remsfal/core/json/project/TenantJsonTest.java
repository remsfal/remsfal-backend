package de.remsfal.core.json.project;

import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.project.TenantModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class TenantJsonTest {

    @Test
    void valueOfReturnsNullForNullTenantModel() {
        assertNull(TenantJson.valueOf((TenantModel) null));
    }

    @Test
    void valueOfReturnsNullForNullCustomerModel() {
        assertNull(TenantJson.valueOf((CustomerModel) null, null));
    }
}
