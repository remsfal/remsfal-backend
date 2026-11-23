package de.remsfal.notification.boundary.push;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import de.remsfal.notification.control.PushProducer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FirebasePushProducer implements PushProducer {

    public FirebasePushProducer(@ConfigProperty(name = "fcm.service-account-path") final String path) {
        if (FirebaseApp.getApps().isEmpty()) {
            try (var in = new FileInputStream(path)) {
                FirebaseApp.initializeApp(FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(in))
                        .build());
            } catch (IOException e) {
                throw new IllegalStateException("Firebase credentials could not be loaded from " + path, e);
            }
        }
    }

    @Override
    public Result sendToken(final String token, final String title, final String body, final Map<String, String> data) {
        Duration lifetime = Duration.ofHours(1);

        Message msg = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setTtl(lifetime.toMillis())
                        .setNotification(AndroidNotification.builder()
                                .setChannelId("default_notification_channel_id")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .putHeader("apns-expiration",
                                Long.toString(Instant.now().plus(lifetime).getEpochSecond()))
                        .setAps(Aps.builder()
                                .setSound("default")
                                .build())
                        .build())
                .putAllData(data)
                .build();
        return send(msg);
    }

    private Result send(final Message message) {
        try {
            FirebaseMessaging.getInstance().send(message);
            return Result.DELIVERED;
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                return Result.UNREGISTERED;
            }
            throw new PushProducer.DeliveryException("Push delivery failed", e);
        }
    }
}
