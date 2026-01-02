package de.remsfal.ticketing.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.ticketing.control.events.IssuePriorityResultEvent;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class IssuePriorityResultConsumer {

    @Inject
    Logger logger;

    @Inject
    IssueRepository repository;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("tickets-incoming-prio")
    public void consume(final String payload) {
        try {
            IssuePriorityResultEvent event = objectMapper.readValue(payload, IssuePriorityResultEvent.class);
            if (event.getIssueId() == null || event.getProjectId() == null) {
                logger.warn("Received priority event without issueId or projectId, ignoring");
                return;
            }
            IssueKey key = new IssueKey();
            key.setIssueId(event.getIssueId());
            key.setProjectId(event.getProjectId());

            Optional<IssueEntity> entityOptional = repository.find(key);
            if (entityOptional.isEmpty()) {
                logger.warnf("Received priority event for unknown issue (projectId=%s, issueId=%s)",
                        event.getProjectId(), event.getIssueId());
                return;
            }

            IssueEntity entity = entityOptional.get();
            if (event.getPriority() != null) {
                try {
                    entity.setPriority(IssueModel.Priority.valueOf(event.getPriority()));
                } catch (IllegalArgumentException ex) {
                    logger.warnf("Unknown priority label '%s' in event for issue %s - storing raw value",
                            event.getPriority(), event.getIssueId());
                    entity.setPriority(event.getPriority());
                }
            }
            entity.setPriorityScore(event.getPriorityScore());
            entity.setPriorityModel(event.getPriorityModel());
            entity.setPriorityTimestamp(event.getPriorityTimestamp() != null ? event.getPriorityTimestamp() : Instant.now());
            repository.update(entity);
            logger.infov("Updated issue priority (issueId={0}, priority={1})", event.getIssueId(), event.getPriority());
        } catch (MismatchedInputException ex) {
            logger.warnf(ex, "Could not parse priority event payload: %s", payload);
        } catch (Exception ex) {
            logger.error("Failed to process priority event", ex);
        }
    }
}