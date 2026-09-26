package de.remsfal.service.control;

import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import de.remsfal.core.json.ImmutableAddressJson;
import de.remsfal.core.json.ImmutableContractorJson;
import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.project.ImmutableApartmentJson;
import de.remsfal.core.json.project.ImmutableBuildingJson;
import de.remsfal.core.json.project.ImmutableProjectJson;
import de.remsfal.core.json.project.ImmutablePropertyJson;
import de.remsfal.core.json.project.ImmutableRentJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.ApartmentModel;
import de.remsfal.core.model.project.BuildingModel;
import de.remsfal.core.model.project.ProjectModel;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.core.model.project.RentModel.BillingCycle;
import de.remsfal.service.entity.dao.UserRepository;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.runtime.StartupEvent;

/**
 * Seeds a small, linked sample data set for local development: a property manager, a tenant and a
 * contractor who share one project. All data is created through the regular controllers so that
 * linking, Kafka events and notification emails behave exactly as in production.
 * <p>
 * Only included in builds with {@code de.remsfal.dev.seed.enabled=true} (dev profile).
 */
@ApplicationScoped
@IfBuildProperty(name = "de.remsfal.dev.seed.enabled", stringValue = "true")
public class DevDataSeedController {

    /** Token ID prefix for users authenticated through the dev login instead of Google. */
    public static final String DEV_TOKEN_PREFIX = "dev|";

    /** Domain of all seeded dev users. */
    public static final String DEV_DOMAIN = "remsfal.dev";

    public static final SeedUser MANAGER =
        new SeedUser("verwalter@" + DEV_DOMAIN, "Vera", "Verwalter", "Property manager (project proprietor)");

    public static final SeedUser TENANT =
        new SeedUser("mieter@" + DEV_DOMAIN, "Max", "Mieter", "Tenant of apartment WE 01");

    public static final SeedUser CONTRACTOR =
        new SeedUser("handwerker@" + DEV_DOMAIN, "Hanna", "Handwerk", "Owner of the contractor organization");

    public static final List<SeedUser> SEED_USERS = List.of(MANAGER, TENANT, CONTRACTOR);

    static final String PROJECT_TITLE = "Musterhaus Berlin";

    static final String ORGANIZATION_NAME = "Sanitär Handwerk GmbH";

    private static final LocalDate START_OF_RENTAL = LocalDate.of(2025, 1, 1);

    public record SeedUser(String email, String firstName, String lastName, String description) {

        public String tokenId() {
            return tokenIdOf(email);
        }

    }

    @Inject
    Logger logger;

    @Inject
    UserRepository userRepository;

    @Inject
    AuthorizationController authorizationController;

    @Inject
    UserController userController;

    @Inject
    ProjectController projectController;

    @Inject
    PropertyController propertyController;

    @Inject
    BuildingController buildingController;

    @Inject
    ApartmentController apartmentController;

    @Inject
    RentalAgreementController rentalAgreementController;

    @Inject
    OrganizationController organizationController;

    @Inject
    ContractorController contractorController;

    public static String tokenIdOf(final String email) {
        return DEV_TOKEN_PREFIX + email.trim().toLowerCase();
    }

    void onStartup(@Observes final StartupEvent event) {
        try {
            seed();
        } catch (RuntimeException e) {
            logger.error("Unable to seed dev data", e);
        }
    }

    /**
     * Creates the sample data set unless the seeded property manager already exists.
     *
     * @return {@code true} if data was seeded, {@code false} if it already existed
     */
    public boolean seed() {
        if (userRepository.findByTokenId(MANAGER.tokenId()).isPresent()) {
            logger.info("Dev seed data already present, skipping");
            return false;
        }
        logger.warn("Seeding dev data - this must never happen in production!");

        final UserModel manager = createUser(MANAGER);
        final UserModel tenant = createUser(TENANT);
        final UserModel contractor = createUser(CONTRACTOR);

        final ProjectModel project = projectController.createProject(manager,
            ImmutableProjectJson.builder().title(PROJECT_TITLE).build());

        final PropertyModel property = propertyController.createProperty(project.getId(),
            ImmutablePropertyJson.builder()
                .title("Grundstück Musterstraße 1")
                .description("Seeded dev property")
                .build());
        final BuildingModel building = buildingController.createBuilding(project.getId(), property.getId(),
            ImmutableBuildingJson.builder()
                .title("Vorderhaus")
                .address(address())
                .build());
        final ApartmentModel apartment = apartmentController.createApartment(project.getId(), building.getId(),
            ImmutableApartmentJson.builder()
                .title("WE 01")
                .location("1. OG links")
                .build());

        rentalAgreementController.createRentalAgreement(project.getId(), ImmutableRentalAgreementJson.builder()
            .startOfRental(START_OF_RENTAL)
            .tenants(List.of(ImmutableTenantJson.builder()
                .firstName(TENANT.firstName())
                .lastName(TENANT.lastName())
                .email(tenant.getEmail())
                .build()))
            .apartmentRents(List.of(ImmutableRentJson.builder()
                .rentalUnitId(apartment.getId())
                .billingCycle(BillingCycle.MONTHLY)
                .firstPaymentDate(START_OF_RENTAL)
                .basicRent(850f)
                .operatingCostsPrepayment(180f)
                .heatingCostsPrepayment(90f)
                .build()))
            .build());

        // the organization email must be a verified email of its owner, the contractor is linked by it
        organizationController.createOrganization(ImmutableOrganizationJson.builder()
            .name(ORGANIZATION_NAME)
            .email(contractor.getEmail())
            .phone("+4930123456789")
            .trade("Sanitär")
            .vatIdentificationNumber("DE123456789")
            .address(address())
            .build(), contractor);
        contractorController.createContractor(manager, project.getId(), ImmutableContractorJson.builder()
            .name(ORGANIZATION_NAME)
            .email(contractor.getEmail())
            .phone("+4930123456789")
            .trade("Sanitär")
            .contactPerson(CONTRACTOR.firstName() + " " + CONTRACTOR.lastName())
            .build());

        logger.infov("Dev seed data created (project={0}, users={1})", project.getId(), SEED_USERS);
        return true;
    }

    private UserModel createUser(final SeedUser seedUser) {
        final UserModel user = authorizationController.authenticateUser(seedUser.tokenId(), seedUser.email(), "de");
        userController.updateUser(user.getId(), ImmutableUserJson.builder()
            .firstName(seedUser.firstName())
            .lastName(seedUser.lastName())
            .build());
        return user;
    }

    private static ImmutableAddressJson address() {
        return ImmutableAddressJson.builder()
            .street("Musterstraße 1")
            .city("Berlin")
            .province("Berlin")
            .zip("10115")
            .countryCode("DE")
            .build();
    }

}
