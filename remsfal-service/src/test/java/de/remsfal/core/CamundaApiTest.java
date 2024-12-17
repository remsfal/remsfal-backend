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
            // Health-Check
            if (!isCamundaReachable(client)) {
                System.err.println("Camunda API ist nicht erreichbar. Test wird übersprungen.");
                return;
            }

            // Prozessdefinition prüfen
            String processDefinitions = getProcessDefinitions(client);
            assertNotNull(processDefinitions, "Keine Prozessdefinitionen gefunden.");
            assertEquals(true, processDefinitions.contains("\"key\":\"open-ticket\""), "Prozess 'open-ticket' sollte vorhanden sein.");

            // Prozess "open-ticket" starten
            String processInstance = startProcess(client, "open-ticket");
            assertNotNull(processInstance, "Process Instance sollte nicht null sein.");
            System.out.println("Gestartete Process Instance: " + processInstance);

            // Erste Aufgabe abschließen
            String firstTaskId = fetchTaskId(client);
            assertNotNull(firstTaskId, "Task-ID sollte nicht null sein.");
            completeTask(client, firstTaskId, createApprovalPayload(true));
            System.out.println("Erste Aufgabe " + firstTaskId + " wurde erfolgreich abgeschlossen.");

            // Nach dem Gateway sollte nun eine weitere Aufgabe anstehen
            // Zweite Aufgabe fetchen und abschließen
            String secondTaskId = fetchTaskId(client);
            assertNotNull(secondTaskId, "Zweite Task-ID sollte nicht null sein.");
            completeTask(client, secondTaskId, createApprovalPayload(true));
            System.out.println("Zweite Aufgabe " + secondTaskId + " wurde erfolgreich abgeschlossen.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fehler während des Camunda Prozessablaufs", e);
        } finally {
            client.close();
        }
    }

    /**
     * Prüft, ob die Camunda-API erreichbar ist.
     */
    private boolean isCamundaReachable(Client client) {
        Response healthCheckResponse = client
                .target(BASE_URL + "/process-definition")
                .request(MediaType.APPLICATION_JSON)
                .get();
        return healthCheckResponse.getStatus() == 200;
    }

    /**
     * Liest alle Prozessdefinitionen.
     */
    private String getProcessDefinitions(Client client) {
        Response response = client
                .target(BASE_URL + "/process-definition")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus(), "Fehler beim Abruf der Prozessdefinitionen.");
        return response.readEntity(String.class);
    }

    /**
     * Startet einen Prozess anhand des Prozessschlüssels.
     */
    private String startProcess(Client client, String processKey) {
        Response response = client
                .target(BASE_URL + "/process-definition/key/" + processKey + "/start")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{}", MediaType.APPLICATION_JSON));

        assertEquals(200, response.getStatus(), "Fehler beim Starten des Prozesses '" + processKey + "'.");
        return response.readEntity(String.class);
    }

    /**
     * Holt die erste gefundene Task-ID.
     */
    private String fetchTaskId(Client client) {
        String tasksJson = fetchTasks(client);
        return extractTaskId(tasksJson);
    }

    /**
     * Ruft die aktuellen Tasks ab und gibt deren JSON zurück.
     */
    private String fetchTasks(Client client) {
        Response response = client
                .target(BASE_URL + "/task")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, response.getStatus(), "Fehler beim Abruf der Tasks.");
        String tasks = response.readEntity(String.class);
        assertNotNull(tasks, "Tasks Antwort sollte nicht null sein.");
        System.out.println("Tasks: " + tasks);
        return tasks;
    }

    /**
     * Extrahiert die Task-ID aus dem Tasks-JSON.
     */
    private String extractTaskId(String tasksJson) {
        try {
            int idIndex = tasksJson.indexOf("\"id\":\"");
            if (idIndex != -1) {
                int start = idIndex + 6; // nach "id":" 
                int end = tasksJson.indexOf("\"", start);
                return tasksJson.substring(start, end);
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Extrahieren der Task-ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Schließt eine gegebene Task mit den übergebenen Variablen ab.
     */
    private void completeTask(Client client, String taskId, String payload) {
        Response response = client
                .target(BASE_URL + "/task/" + taskId + "/complete")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

        assertEquals(204, response.getStatus(), "Fehler beim Abschließen der Task mit ID " + taskId);
    }

    /**
     * Erzeugt ein JSON-Payload für das Abschließen einer Aufgabe mit einer
     * Approve-Variable.
     */
    private String createApprovalPayload(boolean approved) {
        return "{\n" +
                "  \"variables\": {\n" +
                "    \"approved\": {\n" +
                "      \"value\": " + approved + ",\n" +
                "      \"type\": \"Boolean\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
