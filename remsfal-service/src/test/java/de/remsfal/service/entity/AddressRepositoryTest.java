package de.remsfal.service.entity;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractTest;
import de.remsfal.service.TestData;
import de.remsfal.service.entity.dao.AddressRepository;
import de.remsfal.service.entity.dto.AddressValidationEntity;

@QuarkusTest
class AddressRepositoryTest extends AbstractTest {

    @Inject
    AddressRepository repository;

    @Test
    void hashCode_SUCCESS_addressValidationEntity() {
        final List<AddressValidationEntity> entities = repository.findAddressByZip(TestData.ADDRESS_ZIP);
        assertNotNull(entities);
        assertNotNull(entities.get(0).hashCode());
    }
    
    @Test
    void equals_SUCCESS_addressValidationEntity() {
        final List<AddressValidationEntity> entities = repository.findAddressByZip(TestData.ADDRESS_ZIP);
        assertNotNull(entities);
        assertEquals(1, entities.size());
        
        final Optional<AddressValidationEntity> copy = repository.findAddressByParameters(TestData.addressBuilder().build());
        assertTrue(copy.isPresent());
        assertTrue(copy.get().equals(entities.get(0)));
    }
    
}
