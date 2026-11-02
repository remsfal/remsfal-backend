package de.remsfal.notification.control;

public interface PushProducer {

    enum Result { DELIVERED, UNREGISTERED }

    Result sendFid(String fid, String title, String body);

    Result sendToken(String token, String title, String body);

    class DeliveryException extends RuntimeException {
        public DeliveryException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
