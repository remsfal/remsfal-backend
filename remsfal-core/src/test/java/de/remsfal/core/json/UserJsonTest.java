package de.remsfal.core.json;

import java.util.UUID;

import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.UserModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserJsonTest {

    @Test
    void valueOfUserModelCopiesAvailableFields() {
        final UUID id = UUID.randomUUID();
        final UserModel model = mock(UserModel.class);
        when(model.getId()).thenReturn(id);
        when(model.getEmail()).thenReturn("tenant@example.com");
        when(model.getName()).thenReturn("Max Mustermann");

        final UserJson json = UserJson.valueOf(model);

        assertEquals(id, json.getId());
        assertEquals("tenant@example.com", json.getEmail());
        assertEquals("Max", json.getFirstName());
        assertEquals("Mustermann", json.getLastName());
    }

    @Test
    void valueOfUserModelHandlesAbsentFieldsAndSingleName() {
        final UserModel model = mock(UserModel.class);
        when(model.getName()).thenReturn("Max");

        final UserJson json = UserJson.valueOf(model);

        assertNull(json.getId());
        assertNull(json.getEmail());
        assertEquals("Max", json.getFirstName());
        assertNull(json.getLastName());
    }

    @Test
    void valueOfCustomerModelPreservesPhoneNumbers() {
        final CustomerModel model = mock(CustomerModel.class);
        when(model.getMobilePhoneNumber()).thenReturn("+491701234567");
        when(model.getBusinessPhoneNumber()).thenReturn("");

        final UserJson json = UserJson.valueOf(model);

        assertEquals("+491701234567", json.getMobilePhoneNumber());
        assertEquals("", json.getBusinessPhoneNumber());
        assertNull(json.getPrivatePhoneNumber());
    }
}
