package de.remsfal.notification.boundary.push;

import java.io.FileInputStream;
import java.io.IOException;

import de.remsfal.notification.control.PushProducer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;

import de.remsfal.notification.control.PushProducer;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FirebasePushProducer implements PushProducer {

    public FirebasePushProducer(@ConfigProperty(name = "fcm.service-account-path") String path) {
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
    public Result send(String fid, String title, String body) {
        Message msg = Message.builder()
                .setFid(fid)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
        try {
            FirebaseMessaging.getInstance().send(msg);
            return Result.DELIVERED;
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                return Result.UNREGISTERED;
            }
            throw new PushProducer.DeliveryException("Push delivery failed", e);
        }
    }
}
