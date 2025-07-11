package de.remsfal.core;

import org.immutables.value.Value;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 *
 * add boolean getters for detection,
 * remove generated builder from methods as they can't convert nested Objects to immutables
 * (add custom from methods to immutables definition instead, that do convert nested models)
 * Disable null checks in builder
 * Do not generate default JsonProperty names as it overwrites ObjectMapper strategy
 * jdkOnly for list deserialization
 */
@Value.Style(
        get = {"is*", "get*"},
        from = "",
        validationMethod = Value.Style.ValidationMethod.NONE,
        forceJacksonPropertyNames = false,
        jdkOnly = true
)
public @interface ImmutableStyle {

}
