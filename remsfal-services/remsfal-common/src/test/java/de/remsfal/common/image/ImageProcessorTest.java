package de.remsfal.common.image;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageProcessorTest {

    private static final String GPS_SECRET = "WGS-84-SECRET-LOCATION";

    private ImageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ImageProcessor();
        processor.maxFileSize = 10 * 1024 * 1024;
        processor.maxPixels = 40_000_000L;
        processor.jpegQuality = 0.85f;
        processor.logger = Logger.getLogger(ImageProcessor.class);
    }

    @Test
    void processSquare_jpeg_producesRequestedSizes() throws IOException {
        byte[] jpeg = encode(createLandscape(800, 600), "jpeg");

        List<ProcessedImage> result = processor.processSquare(new ByteArrayInputStream(jpeg), 64, 128, 512);

        assertEquals(3, result.size());
        assertSquare(result.get(0), 64, 64);
        assertSquare(result.get(1), 128, 128);
        assertSquare(result.get(2), 512, 512);
        assertEquals(ProcessedImage.MEDIA_TYPE, result.get(0).contentType());
    }

    @Test
    void processSquare_png_cropsCenteredSquare() throws IOException {
        byte[] png = encode(createLandscape(300, 100), "png");

        List<ProcessedImage> result = processor.processSquare(new ByteArrayInputStream(png), 64);

        BufferedImage image = decode(result.get(0));
        assertEquals(64, image.getWidth());
        assertEquals(64, image.getHeight());
        // the red borders of the landscape image are cut off, only the blue center remains
        assertColor(Color.BLUE, image.getRGB(0, 32));
        assertColor(Color.BLUE, image.getRGB(32, 32));
        assertColor(Color.BLUE, image.getRGB(63, 32));
    }

    @Test
    void processSquare_pngWithTransparency_usesWhiteBackground() throws IOException {
        BufferedImage transparent = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

        List<ProcessedImage> result = processor.processSquare(
            new ByteArrayInputStream(encode(transparent, "png")), 64);

        assertColor(Color.WHITE, decode(result.get(0)).getRGB(32, 32));
    }

    @Test
    void processSquare_webp_isAccepted() throws IOException {
        try (InputStream webp = getClass().getResourceAsStream("/images/test-image.webp")) {
            assertNotNull(webp);

            List<ProcessedImage> result = processor.processSquare(webp, 64);

            BufferedImage image = decode(result.get(0));
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());
            assertColor(Color.BLUE, image.getRGB(0, 32));
            assertColor(Color.BLUE, image.getRGB(63, 32));
        }
    }

    @Test
    void processSquare_smallImage_isNotUpscaled() throws IOException {
        byte[] png = encode(createLandscape(90, 30), "png");

        List<ProcessedImage> result = processor.processSquare(new ByteArrayInputStream(png), 64, 512);

        assertSquare(result.get(0), 64, 30);
        assertSquare(result.get(1), 512, 30);
    }

    @Test
    void processSquare_jpegWithExifGps_stripsAllMetadata() throws IOException {
        byte[] jpeg = withExif(encode(createLandscape(400, 300), "jpeg"), 1);
        assertTrue(contains(jpeg, GPS_SECRET.getBytes(StandardCharsets.US_ASCII)));

        List<ProcessedImage> result = processor.processSquare(new ByteArrayInputStream(jpeg), 128);

        byte[] content = result.get(0).content();
        assertFalse(contains(content, GPS_SECRET.getBytes(StandardCharsets.US_ASCII)));
        assertFalse(contains(content, "Exif".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(contains(content, new byte[] { (byte) 0xFF, (byte) 0xE1 }), "no APP1 segment expected");
    }

    @Test
    void processSquare_jpegWithExifOrientation6_isRotatedClockwise() throws IOException {
        // landscape: red on the left half, blue on the right half
        BufferedImage source = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = source.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 100, 100);
        g.setColor(Color.BLUE);
        g.fillRect(100, 0, 100, 100);
        g.dispose();
        byte[] jpeg = withExif(encode(source, "jpeg"), 6);

        List<ProcessedImage> result = processor.processSquare(new ByteArrayInputStream(jpeg), 100);

        // rotated by 90 degrees clockwise: red is on top, blue at the bottom (cropped to the center)
        BufferedImage image = decode(result.get(0));
        assertColor(Color.RED, image.getRGB(50, 10));
        assertColor(Color.BLUE, image.getRGB(50, 90));
    }

    @Test
    void toOrientedRgb_allOrientations_keepDimensionsConsistent() {
        BufferedImage source = new BufferedImage(30, 20, BufferedImage.TYPE_INT_RGB);
        source.setRGB(0, 0, Color.RED.getRGB());
        for (int orientation = 1; orientation <= 8; orientation++) {
            BufferedImage oriented = ImageProcessor.toOrientedRgb(source, orientation);
            boolean swapped = orientation >= 5;
            assertEquals(swapped ? 20 : 30, oriented.getWidth());
            assertEquals(swapped ? 30 : 20, oriented.getHeight());
        }
        // orientation 8 (rotate 270 cw): top-left pixel moves to bottom-left
        assertColor(Color.RED, ImageProcessor.toOrientedRgb(source, 8).getRGB(0, 29));
        // orientation 6 (rotate 90 cw): top-left pixel moves to top-right
        assertColor(Color.RED, ImageProcessor.toOrientedRgb(source, 6).getRGB(19, 0));
    }

    @Test
    void processSquare_svg_isRejected() {
        byte[] svg = ("<?xml version=\"1.0\"?><svg xmlns=\"http://www.w3.org/2000/svg\">"
            + "<script>alert(1)</script></svg>").getBytes(StandardCharsets.UTF_8);

        InputStream svgInput = new ByteArrayInputStream(svg);
        assertThrows(NotSupportedException.class,
            () -> processor.processSquare(svgInput, 64));
    }

    @Test
    void processSquare_textAndGif_areRejected() throws IOException {
        byte[] text = "just some text".getBytes(StandardCharsets.UTF_8);
        byte[] gif = encode(createLandscape(10, 10), "gif");

        InputStream textInput = new ByteArrayInputStream(text);
        assertThrows(NotSupportedException.class,
            () -> processor.processSquare(textInput, 64));
        InputStream gifInput = new ByteArrayInputStream(gif);
        assertThrows(NotSupportedException.class,
            () -> processor.processSquare(gifInput, 64));
    }

    @Test
    void processSquare_fileTooLarge_isRejectedWith413() throws IOException {
        byte[] png = encode(createLandscape(300, 100), "png");
        processor.maxFileSize = png.length - 1;

        InputStream pngInput = new ByteArrayInputStream(png);
        WebApplicationException e = assertThrows(WebApplicationException.class,
            () -> processor.processSquare(pngInput, 64));
        assertEquals(413, e.getResponse().getStatus());
    }

    @Test
    void processSquare_tooManyPixels_isRejectedWith413() throws IOException {
        byte[] png = encode(new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB), "png");
        processor.maxPixels = 1_000_000L;

        InputStream pngInput = new ByteArrayInputStream(png);
        WebApplicationException e = assertThrows(WebApplicationException.class,
            () -> processor.processSquare(pngInput, 64));
        assertEquals(413, e.getResponse().getStatus());
    }

    @Test
    void processSquare_corruptImage_isRejectedWith400() {
        byte[] corrupt = new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3 };

        InputStream corruptInput = new ByteArrayInputStream(corrupt);
        assertThrows(BadRequestException.class,
            () -> processor.processSquare(corruptInput, 64));
    }

    @Test
    void processSquare_invalidArguments_areRejected() {
        assertThrows(BadRequestException.class, () -> processor.processSquare(null, 64));
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> processor.processSquare(empty));
        assertThrows(IllegalArgumentException.class, () -> processor.processSquare(empty, 0));
    }

    private static BufferedImage createLandscape(final int width, final int height) {
        // red borders left and right, blue square in the center
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLUE);
        g.fillRect((width - height) / 2, 0, height, height);
        g.dispose();
        return image;
    }

    private static byte[] encode(final BufferedImage image, final String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, out), "no writer for " + format);
        return out.toByteArray();
    }

    private static BufferedImage decode(final ProcessedImage image) throws IOException {
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(image.content()));
        assertNotNull(decoded);
        return decoded;
    }

    private static void assertSquare(final ProcessedImage image, final int size, final int edge)
        throws IOException {
        assertEquals(size, image.size());
        BufferedImage decoded = decode(image);
        assertEquals(edge, decoded.getWidth());
        assertEquals(edge, decoded.getHeight());
    }

    private static void assertColor(final Color expected, final int rgb) {
        Color actual = new Color(rgb);
        int tolerance = 40;
        assertTrue(Math.abs(expected.getRed() - actual.getRed()) < tolerance
            && Math.abs(expected.getGreen() - actual.getGreen()) < tolerance
            && Math.abs(expected.getBlue() - actual.getBlue()) < tolerance,
            "expected " + expected + " but was " + actual);
    }

    private static boolean contains(final byte[] data, final byte[] pattern) {
        outer:
        for (int i = 0; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Inserts an EXIF APP1 segment with an orientation tag and a GPS IFD (containing
     * {@link #GPS_SECRET} as GPSMapDatum) directly after the SOI marker of a JPEG.
     */
    private static byte[] withExif(final byte[] jpeg, final int orientation) {
        byte[] datum = (GPS_SECRET + "\0").getBytes(StandardCharsets.US_ASCII);
        // TIFF header (8) + IFD0 (2 + 2 * 12 + 4) + GPS IFD (2 + 2 * 12 + 4) + datum
        int ifd0Offset = 8;
        int gpsOffset = ifd0Offset + 2 + 2 * 12 + 4;
        int datumOffset = gpsOffset + 2 + 2 * 12 + 4;
        ByteBuffer tiff = ByteBuffer.allocate(datumOffset + datum.length);
        tiff.put(new byte[] { 'M', 'M', 0, 42 }).putInt(ifd0Offset);
        // IFD0: Orientation (SHORT), GPSInfo pointer (LONG)
        tiff.putShort((short) 2);
        tiff.putShort((short) 0x0112).putShort((short) 3).putInt(1).putShort((short) orientation).putShort((short) 0);
        tiff.putShort((short) 0x8825).putShort((short) 4).putInt(1).putInt(gpsOffset);
        tiff.putInt(0);
        // GPS IFD: GPSLatitudeRef (ASCII "N"), GPSMapDatum (ASCII, stored at offset)
        tiff.putShort((short) 2);
        tiff.putShort((short) 0x0001).putShort((short) 2).putInt(2).put(new byte[] { 'N', 0, 0, 0 });
        tiff.putShort((short) 0x0012).putShort((short) 2).putInt(datum.length).putInt(datumOffset);
        tiff.putInt(0);
        tiff.put(datum);

        byte[] exifHeader = { 'E', 'x', 'i', 'f', 0, 0 };
        int segmentLength = 2 + exifHeader.length + tiff.capacity();
        ByteBuffer result = ByteBuffer.allocate(jpeg.length + 2 + segmentLength);
        result.put(jpeg, 0, 2); // SOI
        result.put((byte) 0xFF).put((byte) 0xE1).putShort((short) segmentLength);
        result.put(exifHeader).put(tiff.array());
        result.put(jpeg, 2, jpeg.length - 2);
        return result.array();
    }

}
