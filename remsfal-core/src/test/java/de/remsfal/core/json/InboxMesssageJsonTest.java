package de.remsfal.core.json;

import de.remsfal.core.json.ticketing.InboxMessageJson;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InboxMesssageJsonTest {

    @Test
    void shouldCreateAndAccessInboxMessageJson() {
        InboxMessageJson msg = new InboxMessageJson();
        msg.id = "123";
        msg.type = "Nachricht";
        msg.contractor = "Baufirma Müller GmbH";
        msg.subject = "Neue Rechnung";
        msg.property = "Wohnung A12";
        msg.tenant = "Max Mustermann";
        msg.receivedAt = OffsetDateTime.now();

        assertEquals("123", msg.id);
        assertEquals("Nachricht", msg.type);
        assertEquals("Baufirma Müller GmbH", msg.contractor);
        assertEquals("Neue Rechnung", msg.subject);
        assertEquals("Wohnung A12", msg.property);
        assertEquals("Max Mustermann", msg.tenant);
        assertNotNull(msg.receivedAt);
    }
}
