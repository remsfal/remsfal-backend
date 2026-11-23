package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableContractorJson;
import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.organization.ImmutableOrganizationEmployeeJson;
import de.remsfal.core.json.project.ImmutablePropertyJson;
import de.remsfal.core.json.project.ImmutableRentalAgreementJson;
import de.remsfal.core.json.project.ImmutableTenantJson;
import de.remsfal.core.json.project.PropertyJson;
import de.remsfal.core.model.ContractorModel;
import de.remsfal.core.model.OrganizationEmployeeModel.EmployeeRole;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.PropertyModel;
import de.remsfal.service.AbstractServiceTest;
import de.remsfal.service.control.event.ContractorEvent;
import de.remsfal.service.control.event.EventRecorder;
import de.remsfal.service.control.event.OrganizationEmployeeEvent;
import de.remsfal.service.control.event.RentalAgreementEvent;
import de.remsfal.service.control.event.RentalUnitEvent;
import de.remsfal.service.control.event.TenantEvent;
import de.remsfal.service.control.event.UserEvent;
import de.remsfal.service.entity.dto.RentalAgreementEntity;
import de.remsfal.test.TestData;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Verifies that the write operations fire their extension point events within the transaction and that an observer
 * can reject a change.
 */
@QuarkusTest
class ExtensionPointEventTest extends AbstractServiceTest {

    @Inject
    EventRecorder recorder;

    @Inject
    PropertyController propertyController;

    @Inject
    RentalAgreementController rentalAgreementController;

    @Inject
    ContractorController contractorController;

    @Inject
    OrganizationController organizationController;

    @Inject
    UserController userController;

    @BeforeEach
    void setUp() {
        setupTestUsers();
        setupTestProjects();
        setupTestOrganizations();
        recorder.reset();
    }

    @AfterEach
    void tearDown() {
        recorder.reset();
    }

    private PropertyModel createProperty() {
        return propertyController.createProperty(ACTING_USER, TestData.PROJECT_ID, TestData.propertyBuilder().build());
    }

    @Test
    void rentalUnit_createUpdateDelete_firesEvents() {
        final PropertyModel property = createProperty();
        propertyController.updateProperty(ACTING_USER, TestData.PROJECT_ID, property.getId(),
            ImmutablePropertyJson.builder().title("new title").build());
        assertTrue(propertyController.deleteProperty(ACTING_USER, TestData.PROJECT_ID, property.getId()));

        final List<RentalUnitEvent> events = recorder.getEvents(RentalUnitEvent.class);
        assertEquals(3, events.size());
        assertTrue(events.get(0) instanceof RentalUnitEvent.Created);
        assertTrue(events.get(1) instanceof RentalUnitEvent.Updated);
        assertTrue(events.get(2) instanceof RentalUnitEvent.Deleted);
        assertEquals("new title", events.get(1).unit().getTitle());
        assertEquals("new title", events.get(2).unit().getTitle());
        assertEquals(UnitType.PROPERTY, events.get(2).unitType());
        assertTrue(events.get(2).unit() instanceof PropertyJson);
        events.forEach(event -> assertEquals(TestData.PROJECT_ID, event.projectId()));
        events.forEach(event -> assertEquals(ACTING_USER.getId(), event.actingUser().getId()));
    }

    @Test
    void rentalUnit_observerThrows_rollsBackCreation() {
        recorder.setRejectedType(RentalUnitEvent.Created.class);

        assertThrows(IllegalStateException.class, this::createProperty);

        final long properties = entityManager
            .createQuery("SELECT count(p) FROM PropertyEntity p", Long.class)
            .getSingleResult();
        assertEquals(0L, properties);
    }

    @Test
    void rentalAgreement_createAndDelete_firesAgreementAndTenantEvents() {
        final RentalAgreementEntity agreement = rentalAgreementController.createRentalAgreement(ACTING_USER,
            TestData.PROJECT_ID, ImmutableRentalAgreementJson.builder()
                .startOfRental(LocalDate.now())
                .addTenants(ImmutableTenantJson.builder()
                    .firstName(TestData.USER_FIRST_NAME_1)
                    .lastName(TestData.USER_LAST_NAME_1)
                    .build())
                .build());
        final UUID tenantId = agreement.getTenants().get(0).getId();

        assertTrue(rentalAgreementController.deleteRentalAgreement(ACTING_USER, TestData.PROJECT_ID, agreement.getId()));

        final List<RentalAgreementEvent> agreementEvents = recorder.getEvents(RentalAgreementEvent.class);
        assertEquals(2, agreementEvents.size());
        assertTrue(agreementEvents.get(0) instanceof RentalAgreementEvent.Created);
        assertTrue(agreementEvents.get(1) instanceof RentalAgreementEvent.Deleted);
        assertEquals(agreement.getId(), agreementEvents.get(1).agreement().getId());
        final List<TenantEvent> tenantEvents = recorder.getEvents(TenantEvent.class);
        assertEquals(2, tenantEvents.size());
        assertTrue(tenantEvents.get(0) instanceof TenantEvent.Created);
        assertTrue(tenantEvents.get(1) instanceof TenantEvent.Deleted);
        assertEquals(tenantId, tenantEvents.get(1).tenant().getId());
        agreementEvents.forEach(event -> assertEquals(ACTING_USER.getId(), event.actingUser().getId()));
        tenantEvents.forEach(event -> assertEquals(ACTING_USER.getId(), event.actingUser().getId()));
        assertEquals(TestData.USER_EMAIL, tenantEvents.get(1).actingUser().getEmail());
    }

    @Test
    void contractor_createAndDelete_firesEvents() {
        final UserModel user = userController.getUser(TestData.USER_ID);
        final ContractorModel contractor = contractorController.createContractor(user, TestData.PROJECT_ID,
            ImmutableContractorJson.builder().name("Contractor").build());

        assertTrue(contractorController.deleteContractor(user, TestData.PROJECT_ID, contractor.getId()));

        final List<ContractorEvent> events = recorder.getEvents(ContractorEvent.class);
        assertEquals(2, events.size());
        assertTrue(events.get(0) instanceof ContractorEvent.Created);
        assertTrue(events.get(1) instanceof ContractorEvent.Deleted);
        assertEquals("Contractor", events.get(1).contractor().getName());
        assertEquals(user.getId(), events.get(1).actingUser().getId());
    }

    @Test
    void addEmployee_newEmail_firesUserCreatedAndEmployeeAdded() {
        final UserModel owner = userController.getUser(TestData.USER_ID_1);

        organizationController.addEmployee(TestData.ORGANIZATION_ID, owner,
            ImmutableOrganizationEmployeeJson.builder()
                .email("new-employee@example.org")
                .employeeRole(EmployeeRole.STAFF)
                .build());

        final List<UserEvent.Created> userEvents = recorder.getEvents(UserEvent.Created.class);
        assertEquals(1, userEvents.size());
        assertEquals("new-employee@example.org", userEvents.get(0).user().getEmail());
        final List<OrganizationEmployeeEvent.Added> events = recorder.getEvents(OrganizationEmployeeEvent.Added.class);
        assertEquals(1, events.size());
        assertEquals(TestData.ORGANIZATION_ID, events.get(0).organizationId());
        assertEquals("new-employee@example.org", events.get(0).employee().getEmail());
        assertEquals(owner.getId(), events.get(0).actingUser().getId());
    }

    @Test
    void updateUser_withAdditionalEmail_firesAdditionalEmailAdded() {
        userController.updateUser(actingUser(TestData.USER_ID_1), ImmutableUserJson.builder()
            .additionalEmails(List.of("additional@example.org"))
            .build());

        final List<UserEvent.AdditionalEmailAdded> events = recorder.getEvents(UserEvent.AdditionalEmailAdded.class);
        assertEquals(1, events.size());
        assertEquals("additional@example.org", events.get(0).email());
        assertEquals(TestData.USER_ID_1, events.get(0).userId());
        assertTrue(recorder.getEvents(UserEvent.ContactDataChanged.class).isEmpty());
    }

}
