package de.remsfal.common.validation;

import jakarta.ws.rs.core.MediaType;
import java.util.Set;

/**
 * Validator for file upload media types.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public final class MediaTypeValidator {

    /**
     * Set of allowed media types for file uploads.
     */
    public static final Set<MediaType> ALLOWED_MEDIA_TYPES = Set.of(
        MediaType.TEXT_PLAIN_TYPE,
        MediaType.valueOf("image/jpg"),
        MediaType.valueOf("image/jpeg"),
        MediaType.valueOf("image/png"),
        MediaType.valueOf("image/gif"),
        MediaType.valueOf("application/pdf"),
        MediaType.APPLICATION_JSON_TYPE,
        MediaType.APPLICATION_XML_TYPE
    );

    private MediaTypeValidator() {
        // Utility class
    }

    /**
     * Validates whether the given content type is allowed for file uploads.
     *
     * @param contentType the content type to validate
     * @return true if the content type is allowed, false otherwise
     */
    public static boolean isValid(final MediaType contentType) {
        if (contentType == null) {
            return false;
        }
        return ALLOWED_MEDIA_TYPES.stream().anyMatch(
            allowedType -> allowedType.isCompatible(contentType)
        );
    }
}
