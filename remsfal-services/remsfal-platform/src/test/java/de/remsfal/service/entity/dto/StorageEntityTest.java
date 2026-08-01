package de.remsfal.service.entity.dto;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
class StorageEntityTest {

    private static final UUID BUILDING_ID = UUID.fromString("b9440c43-b5c0-4951-9c22-000000000001");
    private static final Float USABLE_SPACE = 12.8f;
    private static final Float HEATING_SPACE = 5.5f;

    private StorageEntity entity1;
    private StorageEntity entity2;

    @BeforeEach
    void setUp() {
        entity1 = new StorageEntity();
        entity1.setBuildingId(BUILDING_ID);
        entity1.setUsableSpace(USABLE_SPACE);
        entity1.setHeatingSpace(HEATING_SPACE);
        entity1.setHeated(true);

        entity2 = new StorageEntity();
        entity2.setBuildingId(BUILDING_ID);
        entity2.setUsableSpace(USABLE_SPACE);
        entity2.setHeatingSpace(HEATING_SPACE);
        entity2.setHeated(true);
    }

    @Test
    @DisplayName("Default isHeated is false")
    void defaultIsHeatedIsFalse() {
        assertFalse(new StorageEntity().isHeated());
    }

    @Test
    @DisplayName("isHeated getter/setter roundtrip")
    void isHeatedGetterSetter() {
        final StorageEntity entity = new StorageEntity();
        entity.setHeated(true);
        assertEquals(Boolean.TRUE, entity.isHeated());

        entity.setHeated(false);
        assertEquals(Boolean.FALSE, entity.isHeated());
    }

    @Test
    @DisplayName("Tests two equal objects including isHeated")
    void testEqualsSameValues() {
        assertEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests two unequal objects (different isHeated)")
    void testEqualsDifferentIsHeated() {
        entity2.setHeated(false);
        assertNotEquals(entity1, entity2);
    }

    @Test
    @DisplayName("Tests two unequal objects (different heatingSpace, isHeated unreached)")
    void testEqualsDifferentHeatingSpace() {
        entity2.setHeatingSpace(99.9f);
        assertNotEquals(entity1, entity2);
    }
}
