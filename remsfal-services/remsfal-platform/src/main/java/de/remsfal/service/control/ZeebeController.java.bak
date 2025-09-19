package de.remsfal.service.control;

import io.camunda.zeebe.client.ZeebeClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;
import org.jboss.logging.Logger;

@Path("/zeebe")
public class ZeebeController {

    @Inject
    ZeebeClient zeebeClient;

    @Inject
    Logger logger;

    @POST
    @Path("/start")
    public Response startWorkflow(Map<String, Object> requestBody) {
        try {
            if (!requestBody.containsKey("processId")) {
                throw new IllegalArgumentException("Missing 'processId' in request body.");
            }

            String processId = (String) requestBody.get("processId");
            Map<String, Object> variables = new HashMap<>();

            Object rawVars = requestBody.get("variables");
            if (rawVars instanceof Map<?, ?> rawMap) {
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() instanceof String key) {
                        variables.put(key, entry.getValue());
                    }
                }
            }

            var workflowInstance = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

            return Response.ok(
                String.format("Workflow '%s' started with key: %d",
                    processId,
                    workflowInstance.getProcessInstanceKey()))
                .build();

        } catch (IllegalArgumentException e) {
            logger.error("Bad request: " + e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid request")
                .build();
        }
    }

}