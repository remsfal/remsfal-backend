package de.remsfal.notification;

import de.remsfal.notification.control.PushProducer;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;

/* Durch @Mock wird im Test-Classpath durch diese Klasse automatisch FirebasePushGateway in allen @QuarkusTest-Tests ersetzt.
Der Firebase-Konstruktor läuft dann nie, und kein Test braucht Credentials.*/
@Mock
@ApplicationScoped
public class TestPushProducer implements PushProducer {

    @Override
    public Result send(final String token, final String title, final String body) {
        return Result.DELIVERED;
    }
}
