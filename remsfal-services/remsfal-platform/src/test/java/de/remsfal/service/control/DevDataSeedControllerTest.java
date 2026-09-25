package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.service.AbstractServiceTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(DevDataSeedControllerTest.SeedProfile.class)
class DevDataSeedControllerTest extends AbstractServiceTest {

    public static class SeedProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("de.remsfal.dev.seed.enabled", "true");
        }

    }

    @Inject
    DevDataSeedController seeder;

    @Test
    void seed_SUCCESS_createsLinkedSampleData() {
        assertTrue(seeder.seed());

        for (final DevDataSeedController.SeedUser seedUser : DevDataSeedController.SEED_USERS) {
            final String tokenId = entityManager
                .createQuery("SELECT u.tokenId FROM UserEntity u WHERE u.email = :email", String.class)
                .setParameter("email", seedUser.email())
                .getSingleResult();
            assertEquals(seedUser.tokenId(), tokenId);
        }

        final Long projects = entityManager
            .createQuery("SELECT count(m) FROM ProjectMembershipEntity m "
                + "WHERE m.project.title = :title AND m.user.email = :email", Long.class)
            .setParameter("title", DevDataSeedController.PROJECT_TITLE)
            .setParameter("email", DevDataSeedController.MANAGER.email())
            .getSingleResult();
        assertEquals(1L, projects);

        final Long linkedTenants = entityManager
            .createQuery("SELECT count(t) FROM TenantEntity t WHERE t.user.email = :email", Long.class)
            .setParameter("email", DevDataSeedController.TENANT.email())
            .getSingleResult();
        assertEquals(1L, linkedTenants);

        final Long linkedContractors = entityManager
            .createQuery("SELECT count(c) FROM ContractorEntity c WHERE c.organization.name = :name", Long.class)
            .setParameter("name", DevDataSeedController.ORGANIZATION_NAME)
            .getSingleResult();
        assertEquals(1L, linkedContractors);
    }

    @Test
    void seed_SUCCESS_isIdempotent() {
        assertTrue(seeder.seed());
        assertFalse(seeder.seed());

        final Long projects = entityManager
            .createQuery("SELECT count(p) FROM ProjectEntity p WHERE p.title = :title", Long.class)
            .setParameter("title", DevDataSeedController.PROJECT_TITLE)
            .getSingleResult();
        assertEquals(1L, projects);
    }

}
