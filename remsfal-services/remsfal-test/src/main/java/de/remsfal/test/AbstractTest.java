package de.remsfal.test;

import java.util.Collections;

import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public abstract class AbstractTest {

    @Inject
    protected Logger logger;

    @BeforeEach
    void printTestMethod(final TestInfo testInfo) {
        String method = testInfo.getDisplayName();
        if (method.length() > 100) {
            method = method.substring(0, 100);
        }
        final String line = String.join("", Collections.nCopies(104, "#"));
        final String title = "# " +
            method + String.join("", Collections.nCopies(100 - method.length(), " ")) +
            " #";
        logger.info(line);
        logger.info(title);
        logger.info(line);
    }

}
