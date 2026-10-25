package de.remsfal.service.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmailLoginCodeEntityTest {

    private static final UUID ID = UUID.fromString("b9440c43-b5c0-4951-9c22-000000000001");
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 1, 1, 12, 15);
    private static final LocalDateTime USED_AT = LocalDateTime.of(2026, 1, 1, 12, 5);

    private EmailLoginCodeEntity entity1;
    private EmailLoginCodeEntity entity2;

    private static EmailLoginCodeEntity createEntity() {
        final EmailLoginCodeEntity entity = new EmailLoginCodeEntity();
        entity.setId(ID);
        entity.setEmail("user@example.com");
        entity.setCodeHash("hash");
        entity.setCodeSalt("salt");
        entity.setExpiresAt(EXPIRES_AT);
        entity.setUsedAt(USED_AT);
        entity.setAttempts(2);
        return entity;
    }

    @BeforeEach
    void setUp() {
        entity1 = createEntity();
        entity2 = createEntity();
    }

    @Test
    void getters_SUCCESS_returnValuesOfSetters() {
        assertEquals(ID, entity1.getId());
        assertEquals("user@example.com", entity1.getEmail());
        assertEquals("hash", entity1.getCodeHash());
        assertEquals("salt", entity1.getCodeSalt());
        assertEquals(EXPIRES_AT, entity1.getExpiresAt());
        assertEquals(USED_AT, entity1.getUsedAt());
        assertEquals(2, entity1.getAttempts());
    }

    @Test
    void setEmail_SUCCESS_normalizesEmail() {
        entity1.setEmail("  User@Example.COM ");
        assertEquals("user@example.com", entity1.getEmail());

        entity1.setEmail(null);
        assertNull(entity1.getEmail());
    }

    @Test
    void equals_SUCCESS_sameValues() {
        assertEquals(entity1, entity1);
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void equals_FAILED_differentValues() {
        assertNotEquals(entity1, null);
        assertNotEquals(entity1, new Object());

        entity2.setCodeHash("other");
        assertNotEquals(entity1, entity2);

        entity2 = createEntity();
        entity2.setEmail("other@example.com");
        assertNotEquals(entity1, entity2);

        entity2 = createEntity();
        entity2.generateId();
        assertNotEquals(entity1, entity2);
    }

}
