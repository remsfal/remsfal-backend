package de.remsfal.service.control.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Test observer that records the synchronous project events and can simulate an observer that rejects the creation
 * of a project.
 */
@ApplicationScoped
public class ProjectEventRecorder {

    private final List<ProjectCreatedEvent> createdEvents = new CopyOnWriteArrayList<>();

    private final List<ManagingOrganizationChangedEvent> managingOrganizationEvents = new CopyOnWriteArrayList<>();

    private volatile boolean rejectProjectCreation;

    void onProjectCreated(@Observes final ProjectCreatedEvent event) {
        createdEvents.add(event);
        if (rejectProjectCreation) {
            throw new IllegalStateException("Project creation rejected by test observer");
        }
    }

    void onManagingOrganizationChanged(@Observes final ManagingOrganizationChangedEvent event) {
        managingOrganizationEvents.add(event);
    }

    public List<ProjectCreatedEvent> getCreatedEvents() {
        return List.copyOf(createdEvents);
    }

    public List<ManagingOrganizationChangedEvent> getManagingOrganizationEvents() {
        return List.copyOf(managingOrganizationEvents);
    }

    public void setRejectProjectCreation(final boolean rejectProjectCreation) {
        this.rejectProjectCreation = rejectProjectCreation;
    }

    public void reset() {
        createdEvents.clear();
        managingOrganizationEvents.clear();
        rejectProjectCreation = false;
    }

}
