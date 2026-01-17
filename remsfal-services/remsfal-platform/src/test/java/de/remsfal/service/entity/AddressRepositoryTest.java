package de.remsfal.service.entity;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.entity.dao.AddressRepository;
import de.remsfal.service.entity.dto.AddressValidationEntity;
import de.remsfal.test.TestData;

@QuarkusTest
class AddressRepositoryTest extends AbstractServiceTest {

    @Inject
    AddressRepository repository;

    @Test
    void hashCode_SUCCESS_addressValidationEntity() {
        final List<AddressValidationEntity> entities = repository.findAddressByZip(TestData.ADDRESS_ZIP);
        assertNotNull(entities);
        assertTrue(entities.get(0).hashCode() != 0);
    }
    
    @Test
    void equals_SUCCESS_addressValidationEntity() {
        final List<AddressValidationEntity> entities = repository.findAddressByZip(TestData.ADDRESS_ZIP);
        assertNotNull(entities);
        assertEquals(1, entities.size());
    }
    
}
