package de.remsfal.service.control;

import io.camunda.zeebe.client.ZeebeClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.Map;

@Path("/zeebe")
public class ZeebeController {

        @Inject
        ZeebeClient zeebeClient;

        @Path("/start")
        public String startWorkflow(@QueryParam("approve") Boolean approve) {

                Map<String, Object> variables = Map.of("approve", approve);

                var workflowInstance = zeebeClient.newCreateInstanceCommand()
                                .bpmnProcessId("ticket-process")
                                .latestVersion()
                                .variables(variables)
                                .send()
                                .join();

                return "Ticket Process instance started with key: " + workflowInstance.getProcessInstanceKey();
        }

}