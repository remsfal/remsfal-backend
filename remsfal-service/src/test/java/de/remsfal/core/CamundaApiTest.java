package de.remsfal.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CamundaApiTest {

    private static final String BASE_URL = "http://localhost:8081/engine-rest";

    @Test
    @Tag("camunda")
    public void testProcessLifecycle() {
        Client client = ClientBuilder.newClient();

        try {
            System.out.println("Starting Camunda process lifecycle test...");

            // Health Check: Verify API availability
            Response healthCheckResponse = client
                    .target(BASE_URL + "/process-definition")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (healthCheckResponse.getStatus() != 200) {
                System.err.println("Camunda API is not reachable. Skipping test.");
                return;
            }

            // Step 1: Verify the process is deployed
            Response processDefinitionResponse = client
                    .target(BASE_URL + "/process-definition")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            assertEquals(200, processDefinitionResponse.getStatus(), "Failed to fetch process definitions.");
            String processDefinitions = processDefinitionResponse.readEntity(String.class);
            assertNotNull(processDefinitions, "No process definitions found.");
            System.out.println("Process Definitions: " + processDefinitions);

            // Ensure "open-ticket" process is available
            boolean processAvailable = processDefinitions.contains("\"key\":\"open-ticket\"");
            assertEquals(true, processAvailable, "Process 'open-ticket' should be deployed.");

            // Step 2: Start the process
            System.out.println("Starting the 'open-ticket' process...");
            Response startProcessResponse = client
                    .target(BASE_URL + "/process-definition/key/open-ticket/start")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity("{}", MediaType.APPLICATION_JSON));

            assertEquals(200, startProcessResponse.getStatus(), "Failed to start the 'open-ticket' process.");
            String processInstance = startProcessResponse.readEntity(String.class);
            assertNotNull(processInstance, "Process instance should not be null.");
            System.out.println("Started Process Instance: " + processInstance);

            // Step 3: Fetch the task ID
            System.out.println("Fetching active tasks...");
            Response taskResponse = client
                    .target(BASE_URL + "/task")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            assertEquals(200, taskResponse.getStatus(), "Failed to fetch tasks.");
            String tasks = taskResponse.readEntity(String.class);
            assertNotNull(tasks, "Tasks response should not be null.");
            System.out.println("Tasks: " + tasks);

            // Extract task ID from the response
            String taskId = extractTaskId(tasks);
            assertNotNull(taskId, "Task ID should not be null.");
            System.out.println("Fetched Task ID: " + taskId);

            // Step 4: Complete the task with variables
            System.out.println("Completing the task with ID: " + taskId + "...");
            String completeTaskPayload = "{\n" +
                    "  \"variables\": {\n" +
                    "    \"approved\": {\n" +
                    "      \"value\": false,\n" +
                    "      \"type\": \"Boolean\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            Response completeTaskResponse = client
                    .target(BASE_URL + "/task/" + taskId + "/complete")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(completeTaskPayload, MediaType.APPLICATION_JSON));

            assertEquals(204, completeTaskResponse.getStatus());

            System.out.println("Task " + taskId + " completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred during the Camunda process lifecycle test.", e);
        } finally {
            client.close();
        }
    }

    private String extractTaskId(String tasksJson) {
        try {
            int idIndex = tasksJson.indexOf("\"id\":\"");
            if (idIndex != -1) {
                int start = idIndex + 6; // Move past "id":" characters
                int end = tasksJson.indexOf("\"", start);
                return tasksJson.substring(start, end);
            }
        } catch (Exception e) {
            System.err.println("Failed to extract Task ID from response: " + e.getMessage());
        }
        return null;
    }
}
