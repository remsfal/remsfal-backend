package de.remsfal.service.boundary.authentication;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;

import de.remsfal.service.boundary.exception.UnauthorizedException;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.NewCookie.SameSite;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SessionManager {

    public static final String COOKIE_NAME = "remsfal_session";
    
    public static final String KEY_ALGORITHM = "AES";
    
    @ConfigProperty(name = "de.remsfal.auth.session.encryption-key") 
    private Optional<String> sessionSecretKey;

    @ConfigProperty(name = "de.remsfal.auth.session.cookie-path", defaultValue = "/")
    private String sessionCookiePath;

    @ConfigProperty(name = "de.remsfal.auth.session.cookie-same-site", defaultValue = "STRICT")
    private SameSite sessionCookieSameSite;

    @ConfigProperty(name = "de.remsfal.auth.session.timeout", defaultValue = "PT30M")
    private Duration sessionCookieTimeout;

    private SecretKey secretKey;
    
    @Startup
    public void initSessionParameters() throws NoSuchAlgorithmException {
        if(sessionSecretKey.isPresent()) {
            // decode the base64 encoded string
            byte[] decodedKey = Base64.getDecoder().decode(sessionSecretKey.get());
            // rebuild key using SecretKeySpec
            secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, KEY_ALGORITHM);
        } else {
            // Generate symmetric 128 bit AES key
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGen.init(128);
            secretKey = keyGen.generateKey();
        }
    }

    public String encryptSessionObject(final SessionInfo sessionInfo) {
        // Create the header and payload
        JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
        
        // Create the JWE object and encrypt it
        JWEObject jweObject = new JWEObject(header, sessionInfo.toPayload());
        try {
            jweObject.encrypt(new DirectEncrypter(secretKey));
        } catch (JOSEException e) {
            throw new InternalServerErrorException("Unable to encrypt session object", e);
        }

        // Serialize to a compact JOSE
        return jweObject.serialize();
    }

    public SessionInfo decryptSessionObject(final String sessionObject) {
        try {
            // Parse compact JOSE into JWE object
            JWEObject jweObject = JWEObject.parse(sessionObject);

            // Decrypt it
            jweObject.decrypt(new DirectDecrypter(secretKey));
            
            // convert to session info object
            return new SessionInfo(jweObject.getPayload());
        } catch (ParseException e) {
            throw new UnauthorizedException("Unable to parse session object", e);
        } catch (JOSEException e) {
            throw new InternalServerErrorException("Unable to decrypt session object", e);
        }
    }

    public NewCookie encryptSessionCookie(final SessionInfo sessionInfo) {
        final String sessionObject = encryptSessionObject(sessionInfo);
        return new NewCookie.Builder(COOKIE_NAME)
            .value(sessionObject)
            .path(sessionCookiePath)
            .sameSite(sessionCookieSameSite)
            .maxAge((int) sessionCookieTimeout.getSeconds())
            .build();
    }

    public SessionInfo decryptSessionCookie(final Cookie sessionCookie) {
        if(!COOKIE_NAME.equalsIgnoreCase(sessionCookie.getName())) {
            throw new InternalServerErrorException("Invalid session cookie");
        }
        return decryptSessionObject(sessionCookie.getValue());
    }

    public NewCookie removalSessionCookie() {
        return new NewCookie.Builder(COOKIE_NAME)
            .value("")
            .path(sessionCookiePath)
            .sameSite(sessionCookieSameSite)
            .maxAge(0)
            .build();
    }

    public Cookie findSessionCookie(final Map<String, Cookie> cookies) {
        return cookies.get(COOKIE_NAME);
    }
    
    public SessionInfo.Builder sessionInfoBuilder() {
        return SessionInfo.builder()
            .expireAfter(sessionCookieTimeout);
    }

}
