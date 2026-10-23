package de.remsfal.notification.control;

public interface PushProducer {

    enum Result { DELIVERED, UNREGISTERED }

    Result send(String fid, String title, String body);

    class DeliveryException extends RuntimeException {
        public DeliveryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
