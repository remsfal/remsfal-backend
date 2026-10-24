package de.remsfal.common.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class MetaDataEntityTest {

    private static final class TestEntity extends MetaDataEntity {
    }

    @Test
    void created_SUCCESS_setsCreatedAndModifiedAt() {
        final TestEntity entity = new TestEntity();
        assertNull(entity.getCreatedAt());
        assertNull(entity.getModifiedAt());

        entity.created();
        assertNotNull(entity.getCreatedAt());
        assertEquals(entity.getCreatedAt(), entity.getModifiedAt());
    }

    @Test
    void modified_SUCCESS_updatesOnlyModifiedAt() {
        final TestEntity entity = new TestEntity();
        entity.created();
        final LocalDateTime createdAt = entity.getCreatedAt();

        entity.modified();
        assertEquals(createdAt, entity.getCreatedAt());
        assertFalse(entity.getModifiedAt().isBefore(createdAt));
    }

    @Test
    void equals_SUCCESS_comparesTimestamps() {
        final TestEntity entity = new TestEntity();
        final TestEntity other = new TestEntity();

        assertEquals(entity, entity);
        assertEquals(entity, other);
        assertNotEquals(entity, null);
        assertNotEquals(entity, "no entity");

        // same createdAt (null), different modifiedAt
        other.modified();
        assertNotEquals(entity, other);
        // different createdAt
        entity.created();
        assertNotEquals(entity, other);
    }

}
