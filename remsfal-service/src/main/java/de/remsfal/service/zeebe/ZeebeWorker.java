package de.remsfal.service.zeebe;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.quarkiverse.zeebe.JobWorker;
import jakarta.inject.Inject;

import java.util.Map;

import org.jboss.logging.Logger;

public class ZeebeWorker {

    @Inject
    Logger logger;

    @JobWorker(type = "approve-ticket")
    public void completeTicket(final JobClient client, final ActivatedJob job) {
        logger.info("Task Handler approve-ticket was called.");
        client.newCompleteCommand(job.getKey())
                .variables(Map.of("approveTicketWasCalled", true))
                .send().join();
    }

    @JobWorker(type = "reject-ticket")
    public void rejectTicket(final JobClient client, final ActivatedJob job) {
        logger.info("Task Handler reject-ticket was called.");
        client.newCompleteCommand(job.getKey())
                .variables(Map.of("rejectTicketWasCalled", true))
                .send().join();
    }

    @JobWorker(type = "close-ticket")
    public void closeTicket(final JobClient client, final ActivatedJob job) {
        logger.info("Task Handler reject-ticket was called.");
        client.newCompleteCommand(job.getKey())
                .variables(Map.of("ticketClosed", true))
                .send().join();
    }

}