package de.remsfal.common.image;

import jakarta.ws.rs.core.MediaType;

/**
 * A re-encoded, square image variant produced by {@link ImageProcessor}.
 *
 * @param size the requested edge length in pixels (the actual edge length is smaller if the
 *             original image was smaller, images are never upscaled)
 * @param content the encoded image without any metadata
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public record ProcessedImage(int size, byte[] content) {

    public static final MediaType MEDIA_TYPE = new MediaType("image", "jpeg");

    public static final String FILE_EXTENSION = "jpg";

    public MediaType contentType() {
        return MEDIA_TYPE;
    }

}
