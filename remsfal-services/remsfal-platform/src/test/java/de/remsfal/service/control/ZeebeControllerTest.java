package de.remsfal.service.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.ws.rs.core.Response;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import io.camunda.zeebe.client.ZeebeClient;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for the ZeebeController class without using io.camunda.zeebe.client.api classes.
 */
class ZeebeControllerTest {

    @Mock
    ZeebeClient zeebeClient;

    @Mock
    Logger logger;

    @InjectMocks
    ZeebeController controller;

    @BeforeEach
    void setUp() {
        // Initialize mocks with RETURNS_DEEP_STUBS to handle chained method calls
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testStartWorkflowMissingProcessId() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("variables", Map.of("var1", "value1"));

        // Act
        Response response = controller.startWorkflow(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Invalid request", response.getEntity());

        // Verify that ZeebeClient was never interacted with
        verifyNoInteractions(zeebeClient);

        // Verify that an error was logged
        verify(logger).error(eq("Bad request: Missing 'processId' in request body."), any(IllegalArgumentException.class));
    }
}
