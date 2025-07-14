package de.remsfal.common.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Singleton
public class JacksonConfig implements ObjectMapperCustomizer {

    @Override
    public void customize(final ObjectMapper objectMapper) {
        // Ignore all null-values for serialization
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

}
