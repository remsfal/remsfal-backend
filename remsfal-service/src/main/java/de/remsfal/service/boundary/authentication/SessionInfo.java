package de.remsfal.service.boundary.authentication;

import java.text.ParseException;
import java.time.Duration;
import java.util.Date;

import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
public class SessionInfo {

    private final JWTClaimsSet claimsSet;
    
    public SessionInfo(final JWTClaimsSet claimsSet) {
        this.claimsSet = claimsSet;
    }

    public SessionInfo(final Payload payload) throws ParseException {
        this(JWTClaimsSet.parse(payload.toJSONObject()));
    }

    public String getUserId() {
        return claimsSet.getSubject();
    }

    public String getUserEmail() {
        try {
            return claimsSet.getStringClaim("email");
        } catch (ParseException e) {
            return null;
        }
    }

    public boolean isExpired() {
        final Date expirationTime = claimsSet.getExpirationTime();
        if(expirationTime == null) {
            return true;
        }
        return expirationTime.after(new Date());
    }
    
    public boolean isValid() {
        return getUserId() != null
            && getUserEmail() != null
            && !isExpired();
    }
    
    public Payload toPayload() {
        return new Payload(claimsSet.toJSONObject());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        /**
         * The claims.
         */
        private final JWTClaimsSet.Builder claimBuilder = new JWTClaimsSet.Builder();


        /**
         * Creates a new builder.
         */
        private Builder() {
            // Nothing to do
        }

        public Builder userId(final String userId) {
            claimBuilder.subject(userId);
            return this;
        }

        public Builder userEmail(final String userEmail) {
            claimBuilder.claim("email", userEmail);
            return this;
        }

        public Builder expireAfter(final Duration ttl) {
            final Date expirationTime = new Date(System.currentTimeMillis() + ttl.toMillis());
            claimBuilder.expirationTime(expirationTime);
            return this;
        }

        public SessionInfo build() {
            return new SessionInfo(claimBuilder.build());
        }
    }

}
