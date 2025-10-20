package de.remsfal.core.json.ticketing;

import de.remsfal.core.model.CustomerModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IssueItemJsonTest {

    @Test
    void generateDisplayName_SUCCESS_withBothNames() {
        final CustomerModel user = new TestCustomerModel(
            UUID.randomUUID(),
            "john@example.com",
            "John",
            "Doe"
        );

        final String displayName = IssueItemJson.generateDisplayName(user);

        assertEquals("John Doe", displayName);
    }

    @Test
    void generateDisplayName_SUCCESS_withFirstNameOnly() {
        final CustomerModel user = new TestCustomerModel(
            UUID.randomUUID(),
            "john@example.com",
            "John",
            null
        );

        final String displayName = IssueItemJson.generateDisplayName(user);

        assertEquals("John", displayName);
    }

    @Test
    void generateDisplayName_SUCCESS_withLastNameOnly() {
        final CustomerModel user = new TestCustomerModel(
            UUID.randomUUID(),
            "doe@example.com",
            null,
            "Doe"
        );

        final String displayName = IssueItemJson.generateDisplayName(user);

        assertEquals("Doe", displayName);
    }

    @Test
    void generateDisplayName_SUCCESS_fallbackToEmailWhenNamesNull() {
        // Simulates Google auth user without firstName/lastName (issue #282)
        final CustomerModel user = new TestCustomerModel(
            UUID.randomUUID(),
            "googleuser@gmail.com",
            null,
            null
        );

        final String displayName = IssueItemJson.generateDisplayName(user);

        assertEquals("googleuser@gmail.com", displayName);
    }

    @Test
    void generateDisplayName_SUCCESS_fallbackToEmailWhenNamesBlank() {
        final CustomerModel user = new TestCustomerModel(
            UUID.randomUUID(),
            "user@example.com",
            "  ",
            "  "
        );

        final String displayName = IssueItemJson.generateDisplayName(user);

        assertEquals("user@example.com", displayName);
    }

    @Test
    void generateDisplayName_SUCCESS_nullWhenUserNull() {
        final String displayName = IssueItemJson.generateDisplayName(null);

        assertNull(displayName);
    }

    @Test
    void generateDisplayName_SUCCESS_trimsWhitespace() {
        final CustomerModel user = new TestCustomerModel(
            UUID.randomUUID(),
            "john@example.com",
            "  John  ",
            "  Doe  "
        );

        final String displayName = IssueItemJson.generateDisplayName(user);

        // format with trim() leaves extra spaces between words
        assertEquals("John     Doe", displayName);
    }

    /**
     * Test implementation of CustomerModel for unit testing.
     */
    static class TestCustomerModel implements CustomerModel {
        private final UUID id;
        private final String email;
        private final String firstName;
        private final String lastName;

        public TestCustomerModel(UUID id, String email, String firstName, String lastName) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public Boolean isActive() {
            return true;
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public String getLastName() {
            return lastName;
        }

        @Override
        public de.remsfal.core.model.AddressModel getAddress() {
            return null;
        }

        @Override
        public String getMobilePhoneNumber() {
            return null;
        }

        @Override
        public String getBusinessPhoneNumber() {
            return null;
        }

        @Override
        public String getPrivatePhoneNumber() {
            return null;
        }

        @Override
        public LocalDate getRegisteredDate() {
            return null;
        }

        @Override
        public LocalDateTime getLastLoginDate() {
            return null;
        }
    }
}
