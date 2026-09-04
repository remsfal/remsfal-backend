package de.remsfal.common.boundary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.common.model.FileUploadData;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public final class MultipartAttachmentProcessor {

    private MultipartAttachmentProcessor() {
    }

    public static <J> List<J> processAttachmentParts(final List<InputPart> fileParts,
        final Function<FileUploadData, J> processor) {
        final List<J> attachments = new ArrayList<>();
        if (fileParts == null) {
            return attachments;
        }
        for (final InputPart inputPart : fileParts) {
            try {
                final InputStream inputStream = inputPart.getBody(InputStream.class, null);
                final FileUploadData fileData = new FileUploadData(
                    inputStream, inputPart.getFileName(), inputPart.getMediaType());
                attachments.add(processor.apply(fileData));
            } catch (IOException e) {
                throw new BadRequestException("Failed to read file data", e);
            }
        }
        return attachments;
    }

    /**
     * Extracts and deserializes a single named {@code application/json} part from a multipart
     * request, e.g. the {@code timeline} part alongside one or more {@code attachment} file parts.
     */
    public static <T> T extractJsonPart(final MultipartFormDataInput input, final String partName,
        final Class<T> type) {
        try {
            final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
            final List<InputPart> parts = formDataMap.get(partName);
            if (parts == null || parts.isEmpty()) {
                throw new BadRequestException("Missing '" + partName + "' part in multipart request");
            }
            if (parts.size() > 1) {
                throw new BadRequestException("Multiple '" + partName + "' parts found in multipart request");
            }
            if (parts.get(0).getMediaType() == null
                || !parts.get(0).getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                throw new BadRequestException("'" + partName + "' part must be of type application/json");
            }

            final T value = parts.get(0).getBody(type, type);
            if (value == null) {
                throw new BadRequestException("Unable to parse '" + partName + "' data from request");
            }
            return value;
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse '" + partName + "' data", e);
        }
    }

}
