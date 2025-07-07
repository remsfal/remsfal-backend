package deserializer;

import de.remsfal.core.json.UserJson;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class UserJsonDeserializer extends ObjectMapperDeserializer<UserJson> {
    public UserJsonDeserializer() {
        super(UserJson.class);
    }
}
