package de.remsfal.service.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.service.entity.dto.EmailLoginAttemptEntity.AttemptResult;
import de.remsfal.service.entity.dto.EmailLoginAttemptEntity.AttemptType;

class EmailLoginAttemptEntityTest {

    private static final UUID ID = UUID.fromString("b9440c43-b5c0-4951-9c22-000000000002");

    private EmailLoginAttemptEntity entity1;
    private EmailLoginAttemptEntity entity2;

    private static EmailLoginAttemptEntity createEntity() {
        final EmailLoginAttemptEntity entity = new EmailLoginAttemptEntity();
        entity.setId(ID);
        entity.setEmail("user@example.com");
        entity.setIpAddress("127.0.0.1");
        entity.setType(AttemptType.VERIFY);
        entity.setResult(AttemptResult.SUCCESS);
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
        assertEquals("127.0.0.1", entity1.getIpAddress());
        assertEquals(AttemptType.VERIFY, entity1.getType());
        assertEquals(AttemptResult.SUCCESS, entity1.getResult());
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

        entity2.setResult(AttemptResult.INVALID_CODE);
        assertNotEquals(entity1, entity2);

        entity2 = createEntity();
        entity2.setType(AttemptType.REQUEST);
        assertNotEquals(entity1, entity2);

        entity2 = createEntity();
        entity2.setIpAddress("10.0.0.1");
        assertNotEquals(entity1, entity2);

        entity2 = createEntity();
        entity2.setEmail("other@example.com");
        assertNotEquals(entity1, entity2);

        entity2 = createEntity();
        entity2.generateId();
        assertNotEquals(entity1, entity2);
    }

}
