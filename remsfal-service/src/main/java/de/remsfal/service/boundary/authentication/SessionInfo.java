package de.remsfal.service.boundary.authentication;

import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import de.remsfal.service.entity.dto.UserAuthenticationEntity;

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

    public Map<String, Object> getClaims() {
        //clear all null values from the claims
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        claimsSet.getClaims().forEach((key, value) -> {
            if (value != null) {
                builder.claim(key, value);
            }
        });
        return builder.build().getClaims();
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
        return expirationTime.before(new Date());
    }

    public long getExpireInSeconds() {
        final Date expirationTime = claimsSet.getExpirationTime();
        if (expirationTime == null) {
            return 0;
        }
        return (expirationTime.getTime() - System.currentTimeMillis()) / 1000;
    }
    
    public boolean isValid() {
        return getUserId() != null
            && getUserEmail() != null
            && !isExpired();
    }

    public boolean shouldRenew() {
        final Date expirationTime = claimsSet.getExpirationTime();
        if(expirationTime == null) {
            return false;
        }
        final long remainingTime = expirationTime.getTime() - System.currentTimeMillis();
        return remainingTime < Duration.ofMinutes(5).toMillis();
    }
    
    public Payload toPayload() {
        return new Payload(claimsSet.toJSONObject());
    }
    
    @Override
    public String toString() {
        return claimsSet.toString();
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

        public Builder claim(final String name, final Object value) {
            claimBuilder.claim(name, value);
            return this;
        }

        public Builder expireAfter(final Duration ttl) {
            final Date expirationTime = new Date(System.currentTimeMillis() + ttl.toMillis());
            claimBuilder.expirationTime(expirationTime);
            return this;
        }
        public Builder from(final SessionInfo sessionInfo) {
            claimBuilder.subject(sessionInfo.getUserId());
            claimBuilder.claim("email", sessionInfo.getUserEmail());
            claimBuilder.expirationTime(sessionInfo.claimsSet.getExpirationTime());
            return this;
        }

        public Builder from(final UserAuthenticationEntity userAuth) {
            claimBuilder.subject(userAuth.getUser().getId());
            claimBuilder.claim("email", userAuth.getUser().getEmail());
            claimBuilder.claim("refreshToken", userAuth.getRefreshToken());
            return this;
        }

        public SessionInfo build() {
            return new SessionInfo(claimBuilder.build());
        }
    }

}
