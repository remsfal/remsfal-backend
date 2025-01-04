package de.remsfal.service.zeebe;

import io.quarkiverse.zeebe.test.InjectZeebeClient;
import io.quarkiverse.zeebe.test.ZeebeTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;
import static org.awaitility.Awaitility.await;

import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

@QuarkusTest
@QuarkusTestResource(ZeebeTestResource.class)
public class ZeebeProcessTest {

    @InjectZeebeClient
    ZeebeClient client;

    @Test
    public void processTest_TicketApproved() {
        ProcessInstanceEvent event = client.newCreateInstanceCommand()
                .bpmnProcessId("ticket-process").latestVersion()
                .variables(Map.of("approve", true)).send().join();

        ProcessInstanceAssert a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);

        BpmnAssert.assertThat(event)
                .hasVariableWithValue("ticketClosed", true);

        BpmnAssert.assertThat(event)
                .hasVariableWithValue("approveTicketWasCalled", true);
    }

    @Test
    public void processTest_TicketRejected() {
        ProcessInstanceEvent event = client.newCreateInstanceCommand()
                .bpmnProcessId("ticket-process").latestVersion()
                .variables(Map.of("approve", false)).send().join();

        ProcessInstanceAssert a = BpmnAssert.assertThat(event);
        await().atMost(7, SECONDS).untilAsserted(a::isCompleted);

        BpmnAssert.assertThat(event)
                .hasVariableWithValue("ticketClosed", true);

        BpmnAssert.assertThat(event)
                .hasVariableWithValue("rejectTicketWasCalled", true);
    }

}