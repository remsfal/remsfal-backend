package de.remsfal.common.image;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.twelvemonkeys.imageio.metadata.Directory;
import com.twelvemonkeys.imageio.metadata.Entry;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEG;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegment;
import com.twelvemonkeys.imageio.metadata.jpeg.JPEGSegmentUtil;
import com.twelvemonkeys.imageio.metadata.tiff.TIFF;
import com.twelvemonkeys.imageio.metadata.tiff.TIFFReader;

/**
 * Validates uploaded images and produces square, re-encoded variants of the requested sizes.
 * <ul>
 * <li>Only JPEG, PNG and WebP are accepted. The format is detected by its magic bytes,
 * the client's content type is not trusted. SVG is rejected (script content).</li>
 * <li>File size and pixel count are limited (protection against decompression bombs).</li>
 * <li>The image is re-encoded as JPEG, all metadata (EXIF, GPS, XMP, ICC, comments) is dropped.
 * The EXIF orientation is applied to the pixels before.</li>
 * <li>Each variant is a centered square crop, scaled down to the requested size.</li>
 * </ul>
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class ImageProcessor {

    static {
        // register the TwelveMonkeys plugins (WebP, JPEG) with the context class loader
        ImageIO.scanForPlugins();
    }

    enum ImageFormat {
        JPEG("jpeg"),
        PNG("png"),
        WEBP("webp");

        private final String formatName;

        ImageFormat(final String formatName) {
            this.formatName = formatName;
        }
    }

    @ConfigProperty(name = "remsfal.image.max-file-size", defaultValue = "10485760")
    int maxFileSize;

    @ConfigProperty(name = "remsfal.image.max-pixels", defaultValue = "40000000")
    long maxPixels;

    @ConfigProperty(name = "remsfal.image.jpeg-quality", defaultValue = "0.85")
    float jpegQuality;

    @Inject
    Logger logger;

    /**
     * Processes an uploaded image into square variants.
     *
     * @param inputStream the uploaded image
     * @param sizes the requested edge lengths in pixels
     * @return one {@link ProcessedImage} per requested size, in the given order
     * @throws NotSupportedException (415) if the image is not a JPEG, PNG or WebP
     * @throws WebApplicationException (413) if the file or the pixel count is too large
     * @throws BadRequestException (400) if the image cannot be decoded
     */
    public List<ProcessedImage> processSquare(final InputStream inputStream, final int... sizes) {
        if (sizes == null || sizes.length == 0) {
            throw new IllegalArgumentException("At least one size is required");
        }
        for (final int size : sizes) {
            if (size <= 0) {
                throw new IllegalArgumentException("Size must be positive: " + size);
            }
        }
        final byte[] data = readLimited(inputStream);
        final ImageFormat format = detectFormat(data);
        final BufferedImage decoded = decode(data, format);
        final int orientation = format == ImageFormat.JPEG ? readExifOrientation(data) : 1;
        final BufferedImage square = cropToSquare(toOrientedRgb(decoded, orientation));
        logger.debugv("Processing {0} image with {1}x{2} pixels",
            format, decoded.getWidth(), decoded.getHeight());

        final List<ProcessedImage> result = new ArrayList<>(sizes.length);
        for (final int size : sizes) {
            final BufferedImage scaled = scaleDown(square, Math.min(size, square.getWidth()));
            result.add(new ProcessedImage(size, encodeJpeg(scaled)));
        }
        return result;
    }

    private byte[] readLimited(final InputStream inputStream) {
        if (inputStream == null) {
            throw new BadRequestException("Image cannot be null");
        }
        try (inputStream) {
            final byte[] data = inputStream.readNBytes(maxFileSize + 1);
            if (data.length > maxFileSize) {
                throw new WebApplicationException("Image exceeds the maximum file size of "
                    + maxFileSize + " bytes", Response.Status.REQUEST_ENTITY_TOO_LARGE);
            }
            return data;
        } catch (IOException e) {
            throw new BadRequestException("Failed to read image", e);
        }
    }

    static ImageFormat detectFormat(final byte[] data) {
        if (startsWith(data, 0, 0xFF, 0xD8, 0xFF)) {
            return ImageFormat.JPEG;
        }
        if (startsWith(data, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return ImageFormat.PNG;
        }
        if (startsWith(data, 0, 'R', 'I', 'F', 'F') && startsWith(data, 8, 'W', 'E', 'B', 'P')) {
            return ImageFormat.WEBP;
        }
        throw new NotSupportedException("Unsupported image type, only JPEG, PNG and WebP are allowed");
    }

    private static boolean startsWith(final byte[] data, final int offset, final int... magic) {
        if (data.length < offset + magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if ((data[offset + i] & 0xFF) != magic[i]) {
                return false;
            }
        }
        return true;
    }

    private BufferedImage decode(final byte[] data, final ImageFormat format) {
        final Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(format.formatName);
        if (!readers.hasNext()) {
            throw new NotSupportedException("No image reader available for " + format);
        }
        final ImageReader reader = readers.next();
        try (ImageInputStream stream = new MemoryCacheImageInputStream(new ByteArrayInputStream(data))) {
            reader.setInput(stream, true, true);
            // check the dimensions from the header before decoding any pixels
            final long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
            if (pixels > maxPixels) {
                throw new WebApplicationException("Image exceeds the maximum of "
                    + maxPixels + " pixels", Response.Status.REQUEST_ENTITY_TOO_LARGE);
            }
            final BufferedImage image = reader.read(0);
            if (image == null) {
                throw new BadRequestException("Image could not be decoded");
            }
            return image;
        } catch (IOException | RuntimeException e) {
            if (e instanceof WebApplicationException webException) {
                throw webException;
            }
            throw new BadRequestException("Image could not be decoded", e);
        } finally {
            reader.dispose();
        }
    }

    private int readExifOrientation(final byte[] data) {
        try (ImageInputStream stream = new MemoryCacheImageInputStream(new ByteArrayInputStream(data))) {
            final List<JPEGSegment> segments = JPEGSegmentUtil.readSegments(stream, JPEG.APP1, "Exif");
            if (segments.isEmpty()) {
                return 1;
            }
            final InputStream exif = segments.get(0).data();
            // skip the pad byte following the "Exif\0" identifier
            if (exif.read() == -1) {
                return 1;
            }
            try (ImageInputStream exifStream = new MemoryCacheImageInputStream(exif)) {
                final Directory directory = new TIFFReader().read(exifStream);
                final Entry entry = directory.getEntryById(TIFF.TAG_ORIENTATION);
                if (entry != null && entry.getValue() instanceof Number orientation) {
                    return orientation.intValue();
                }
            }
        } catch (IOException | RuntimeException e) {
            logger.debugv("Unable to read EXIF orientation: {0}", e.getMessage());
        }
        return 1;
    }

    /**
     * Draws the image into a new RGB image, applying the EXIF orientation and placing
     * transparent areas on a white background (JPEG has no alpha channel).
     */
    static BufferedImage toOrientedRgb(final BufferedImage source, final int orientation) {
        final int w = source.getWidth();
        final int h = source.getHeight();
        final AffineTransform transform = switch (orientation) {
            case 2 -> new AffineTransform(-1, 0, 0, 1, w, 0);
            case 3 -> new AffineTransform(-1, 0, 0, -1, w, h);
            case 4 -> new AffineTransform(1, 0, 0, -1, 0, h);
            case 5 -> new AffineTransform(0, 1, 1, 0, 0, 0);
            case 6 -> new AffineTransform(0, 1, -1, 0, h, 0);
            case 7 -> new AffineTransform(0, -1, -1, 0, h, w);
            case 8 -> new AffineTransform(0, -1, 1, 0, 0, w);
            default -> new AffineTransform();
        };
        final boolean swap = orientation >= 5 && orientation <= 8;
        final BufferedImage target = new BufferedImage(swap ? h : w, swap ? w : h, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = target.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, target.getWidth(), target.getHeight());
            g.drawImage(source, transform, null);
        } finally {
            g.dispose();
        }
        return target;
    }

    static BufferedImage cropToSquare(final BufferedImage image) {
        final int side = Math.min(image.getWidth(), image.getHeight());
        final int x = (image.getWidth() - side) / 2;
        final int y = (image.getHeight() - side) / 2;
        return image.getSubimage(x, y, side, side);
    }

    /**
     * Scales a square image down in several halving steps, which gives a much better
     * quality than a single bicubic step for large reduction factors.
     */
    static BufferedImage scaleDown(final BufferedImage square, final int size) {
        BufferedImage current = square;
        int currentSize = square.getWidth();
        do {
            currentSize = Math.max(currentSize / 2, size);
            final BufferedImage next = new BufferedImage(currentSize, currentSize, BufferedImage.TYPE_INT_RGB);
            final Graphics2D g = next.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(current, 0, 0, currentSize, currentSize, null);
            } finally {
                g.dispose();
            }
            current = next;
        } while (currentSize > size);
        return current;
    }

    private byte[] encodeJpeg(final BufferedImage image) {
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG image writer available");
        }
        final ImageWriter writer = writers.next();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream stream = new MemoryCacheImageOutputStream(out)) {
            writer.setOutput(stream);
            final ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(jpegQuality);
            // no metadata: the original EXIF/GPS/XMP data is never written
            writer.write(null, new IIOImage(image, null, null), param);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode image", e);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }

}
