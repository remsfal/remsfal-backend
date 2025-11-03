package de.remsfal.ticketing.boundary;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InboxRessourceExampleTest {

    @Test
    void shouldReturnInboxMessages() {
        InboxResourceExample resource = new InboxResourceExample();
        assertNotNull(resource.getInboxMessages());
    }
}