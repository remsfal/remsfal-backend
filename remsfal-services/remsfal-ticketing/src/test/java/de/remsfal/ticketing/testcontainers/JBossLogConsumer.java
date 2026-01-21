package de.remsfal.ticketing.testcontainers;

import java.util.function.Consumer;

import org.jboss.logging.Logger;
import org.testcontainers.containers.output.OutputFrame;

public class JBossLogConsumer implements Consumer<OutputFrame> {

    private final Logger logger;
    private final String prefix;

    public JBossLogConsumer(Logger logger) {
        this(logger, "");
    }

    public JBossLogConsumer(Logger logger, String prefix) {
        this.logger = logger;
        this.prefix = prefix;
    }

    @Override
    public void accept(OutputFrame outputFrame) {
        // remove line endings
        String message = outputFrame.getUtf8StringWithoutLineEnding();
        
        // ignore empty messages
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        String finalMessage = prefix + message;

        // differentiate between stdout and stderr
        if (outputFrame.getType() == OutputFrame.OutputType.STDERR) {
            logger.error(finalMessage);
        } else {
            logger.info(finalMessage);
        }
    }

}
