package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.remsfal.ticketing.AbstractTicketingTest;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@QuarkusTest
public class ChatMessageControllerTest extends AbstractTicketingTest {

    @Inject
    ChatMessageController chatMessageController;

    @Test
    void testExtractFileNameFromUrl_Success() {
        assertEquals("file.txt",
            chatMessageController.extractFileNameFromUrl("http://localhost/files/file.txt"));
        assertEquals("file.txt", chatMessageController.extractFileNameFromUrl("file.txt"));
        assertEquals("document.pdf",
            chatMessageController.extractFileNameFromUrl("/path/to/document.pdf"));
    }

    @Test
    void testExtractFileNameFromUrl_NullInput_Exception() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> chatMessageController.extractFileNameFromUrl(null));
        assertEquals("File URL cannot be null or empty", ex.getMessage());
    }

    @Test
    void testExtractFileNameFromUrl_BlankInput_Exception() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> chatMessageController.extractFileNameFromUrl(" "));
        assertEquals("File URL cannot be null or empty", ex.getMessage());
    }

    @Test
    void testExtractFileNameFromUrl_EndsWithSlash_Exception() {
        String input = "https://example.com/files/";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> chatMessageController.extractFileNameFromUrl(input));
        assertEquals("Invalid file URL format: " + input, ex.getMessage());
    }
}
