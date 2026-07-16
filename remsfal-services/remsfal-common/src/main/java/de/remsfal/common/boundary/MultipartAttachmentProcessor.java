package de.remsfal.common.boundary;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import jakarta.ws.rs.BadRequestException;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;

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

}
