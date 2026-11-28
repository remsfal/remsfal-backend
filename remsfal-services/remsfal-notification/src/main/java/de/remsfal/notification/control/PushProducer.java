package de.remsfal.notification.control;

import java.util.Map;

public interface PushProducer {

    enum Result { DELIVERED, UNREGISTERED }

    Result sendToken(String token, String title, String body, Map<String, String> data);

    class DeliveryException extends RuntimeException {
        public DeliveryException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
