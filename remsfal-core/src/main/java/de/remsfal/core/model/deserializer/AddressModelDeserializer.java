package de.remsfal.core.model.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.remsfal.core.json.ImmutableAddressJson;
import de.remsfal.core.model.AddressModel;

import java.io.IOException;

public class AddressModelDeserializer extends JsonDeserializer<AddressModel> {
    @Override
    public AddressModel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        // Support both "country" and "countryCode" for backward compatibility
        String countryCode;
        if (node.has("countryCode")) {
            countryCode = node.get("countryCode").asText();
        } else if (node.has("country")) {
            countryCode = node.get("country").asText();
        } else {
            countryCode = "";
        }

        return ImmutableAddressJson.builder()
                .street(node.get("street").asText())
                .city(node.get("city").asText())
                .province(node.get("province").asText())
                .zip(node.get("zip").asText())
                .countryCode(countryCode)
                .build();
    }
}