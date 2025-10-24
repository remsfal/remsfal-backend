package de.remsfal.core.model.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.remsfal.core.model.AddressModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AddressModelDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(AddressModel.class, new AddressModelDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    @DisplayName("Test deserialization with countryCode field")
    void testDeserializeWithCountryCode() throws Exception {
        String json = """
                {
                    "street": "Test Street 123",
                    "city": "Berlin",
                    "province": "Berlin",
                    "zip": "13357",
                    "countryCode": "DE"
                }
                """;

        AddressModel address = objectMapper.readValue(json, AddressModel.class);

        assertNotNull(address);
        assertEquals("Test Street 123", address.getStreet());
        assertEquals("Berlin", address.getCity());
        assertEquals("Berlin", address.getProvince());
        assertEquals("13357", address.getZip());
        assertEquals("DE", address.getCountry().getCountry());
    }

    @Test
    @DisplayName("Test deserialization with country field (backward compatibility)")
    void testDeserializeWithCountry() throws Exception {
        String json = """
                {
                    "street": "Main Avenue 456",
                    "city": "Munich",
                    "province": "Bavaria",
                    "zip": "80331",
                    "country": "DE"
                }
                """;

        AddressModel address = objectMapper.readValue(json, AddressModel.class);

        assertNotNull(address);
        assertEquals("Main Avenue 456", address.getStreet());
        assertEquals("Munich", address.getCity());
        assertEquals("Bavaria", address.getProvince());
        assertEquals("80331", address.getZip());
        assertEquals("DE", address.getCountry().getCountry());
    }

    @Test
    @DisplayName("Test deserialization without country field")
    void testDeserializeWithoutCountry() throws Exception {
        String json = """
                {
                    "street": "Park Lane 789",
                    "city": "Hamburg",
                    "province": "Hamburg",
                    "zip": "20095"
                }
                """;

        AddressModel address = objectMapper.readValue(json, AddressModel.class);

        assertNotNull(address);
        assertEquals("Park Lane 789", address.getStreet());
        assertEquals("Hamburg", address.getCity());
        assertEquals("Hamburg", address.getProvince());
        assertEquals("20095", address.getZip());
        assertEquals("", address.getCountry().getCountry());
    }

    @Test
    @DisplayName("Test deserialization with both countryCode and country fields (countryCode takes precedence)")
    void testDeserializeWithBothCountryFields() throws Exception {
        String json = """
                {
                    "street": "Test Street 999",
                    "city": "Frankfurt",
                    "province": "Hessen",
                    "zip": "60311",
                    "countryCode": "DE",
                    "country": "US"
                }
                """;

        AddressModel address = objectMapper.readValue(json, AddressModel.class);

        assertNotNull(address);
        assertEquals("Test Street 999", address.getStreet());
        assertEquals("Frankfurt", address.getCity());
        assertEquals("Hessen", address.getProvince());
        assertEquals("60311", address.getZip());
        assertEquals("DE", address.getCountry().getCountry());
    }
}