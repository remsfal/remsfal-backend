package de.remsfal.service.entity.dto;

import de.remsfal.service.entity.dto.embeddable.OrganizationEmployeeKey;
import de.remsfal.test.AbstractTest;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class OrganizationEmployeeKeyTest extends AbstractTest {

    private OrganizationEmployeeKey organizationEmployeeKey1;
    private OrganizationEmployeeKey organizationEmployeeKey2;
    UUID organizationId;
    UUID userId;

    @BeforeEach
    public void setUp() {
        organizationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        organizationEmployeeKey1 = new OrganizationEmployeeKey();
        organizationEmployeeKey1.setOrganizationId(organizationId);
        organizationEmployeeKey1.setUserId(userId);

        organizationEmployeeKey2 = new OrganizationEmployeeKey();
        organizationEmployeeKey2.setOrganizationId(organizationId);
        organizationEmployeeKey2.setUserId(userId);

    }

    @Test
    void test_getter_and_setter() {
        assertEquals(organizationId, organizationEmployeeKey1.getOrganizationId());
        assertEquals(userId, organizationEmployeeKey1.getUserId());
    }

    @Test
    void test_hashCode() {
        assertEquals(organizationEmployeeKey1.hashCode(), Objects.hash(organizationId, userId));
    }

    @Test
    void isEqual_SUCCESS_sameInstance() {
        assertTrue(organizationEmployeeKey1.equals(organizationEmployeeKey1));
    }

//    @Test
//    void isUnequal_SUCCESS_differentInstancesSameValues() {
//        assertFalse(organization1.equals(organization2));
//    }

    @Test
    void isUnequal_SUCCESS_differentInstancesDifferentValues() {
        organizationEmployeeKey2.setUserId(UUID.randomUUID());
        assertFalse(organizationEmployeeKey1.equals(organizationEmployeeKey2));
    }
}
