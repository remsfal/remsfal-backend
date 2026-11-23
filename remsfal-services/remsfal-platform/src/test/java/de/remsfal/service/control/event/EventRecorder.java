package de.remsfal.service.control.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Test observer that records the synchronous extension point events and can simulate an observer that rejects a
 * change by throwing an exception.
 */
@ApplicationScoped
public class EventRecorder {

    private final List<Object> events = new CopyOnWriteArrayList<>();

    private volatile Class<?> rejectedType;

    void onProjectEvent(@Observes final ProjectEvent event) {
        store(event);
    }

    void onProjectMemberEvent(@Observes final ProjectMemberEvent event) {
        store(event);
    }

    void onProjectOrganizationEvent(@Observes final ProjectOrganizationEvent event) {
        store(event);
    }

    void onRentalUnitEvent(@Observes final RentalUnitEvent event) {
        store(event);
    }

    void onRentalAgreementEvent(@Observes final RentalAgreementEvent event) {
        store(event);
    }

    void onTenantEvent(@Observes final TenantEvent event) {
        store(event);
    }

    void onContractorEvent(@Observes final ContractorEvent event) {
        store(event);
    }

    void onOrganizationEvent(@Observes final OrganizationEvent event) {
        store(event);
    }

    void onOrganizationEmployeeEvent(@Observes final OrganizationEmployeeEvent event) {
        store(event);
    }

    void onUserEvent(@Observes final UserEvent event) {
        store(event);
    }

    private void store(final Object event) {
        events.add(event);
        if (rejectedType != null && rejectedType.isInstance(event)) {
            throw new IllegalStateException("Change rejected by test observer: " + event);
        }
    }

    /**
     * @return the recorded events of the given type in the order they were fired
     */
    public <T> List<T> getEvents(final Class<T> type) {
        return events.stream()
            .filter(type::isInstance)
            .map(type::cast)
            .toList();
    }

    /**
     * Lets the recorder reject (throw on) every event of the given type, e.g. {@code ProjectEvent.Created.class}.
     */
    public void setRejectedType(final Class<?> rejectedType) {
        this.rejectedType = rejectedType;
    }

    public void reset() {
        events.clear();
        rejectedType = null;
    }

}
