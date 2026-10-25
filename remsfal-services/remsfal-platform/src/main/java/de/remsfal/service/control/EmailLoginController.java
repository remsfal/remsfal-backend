package de.remsfal.service.control;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import de.remsfal.common.authentication.UnauthorizedException;
import de.remsfal.common.util.SecureTokens;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.control.exception.TooManyRequestsException;
import de.remsfal.service.entity.dao.AdditionalEmailRepository;
import de.remsfal.service.entity.dao.EmailLoginAttemptRepository;
import de.remsfal.service.entity.dao.EmailLoginCodeRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dto.AdditionalEmailEntity;
import de.remsfal.service.entity.dto.EmailLoginAttemptEntity.AttemptResult;
import de.remsfal.service.entity.dto.EmailLoginAttemptEntity.AttemptType;
import de.remsfal.service.entity.dto.EmailLoginCodeEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;
import io.quarkus.scheduler.Scheduled;

/**
 * Passwordless email login: a one-time code is sent to the email address and exchanged for a session. A
 * successful login proves the ownership of the email address, so the email is treated as verified.
 * <p>
 * Codes are stored as salted hashes only, expire after a short time, can be used once and allow a limited
 * number of attempts. Requests and attempts are rate limited per email address and per client IP address.
 */
@ApplicationScoped
public class EmailLoginController {

    static final int CODE_LENGTH = 8;

    private static final int SALT_BYTES = 16;

    @ConfigProperty(name = "de.remsfal.auth.email-login.code-ttl", defaultValue = "PT15M")
    Duration codeTtl;

    @ConfigProperty(name = "de.remsfal.auth.email-login.max-attempts", defaultValue = "5")
    int maxAttempts;

    @ConfigProperty(name = "de.remsfal.auth.email-login.rate-limit.window", defaultValue = "PT15M")
    Duration rateLimitWindow;

    @ConfigProperty(name = "de.remsfal.auth.email-login.rate-limit.per-email", defaultValue = "5")
    int requestsPerEmail;

    @ConfigProperty(name = "de.remsfal.auth.email-login.rate-limit.per-ip", defaultValue = "20")
    int requestsPerIp;

    @ConfigProperty(name = "de.remsfal.auth.email-login.rate-limit.verify-per-ip", defaultValue = "30")
    int verificationsPerIp;

    @ConfigProperty(name = "de.remsfal.auth.email-login.attempt-retention", defaultValue = "P30D")
    Duration attemptRetention;

    @Inject
    Logger logger;

    @Inject
    EmailLoginCodeRepository codeRepository;

    @Inject
    EmailLoginAttemptRepository attemptRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    AdditionalEmailRepository additionalEmailRepository;

    @Inject
    AuthorizationController authorizationController;

    @Inject
    NotificationController notificationController;

    /**
     * Sends a new login code to the email address and invalidates all older codes. Whether the address belongs
     * to a user only affects the language and the salutation of the email.
     *
     * @throws TooManyRequestsException if the email address or the client exceeded the rate limit
     */
    public void requestCode(final String email, final String locale, final String ipAddress) {
        final String normalizedEmail = normalize(email);
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime since = now.minus(rateLimitWindow);
        if (attemptRepository.countByEmailSince(normalizedEmail, AttemptType.REQUEST, since) >= requestsPerEmail
            || attemptRepository.countByIpAddressSince(ipAddress, AttemptType.REQUEST, since) >= requestsPerIp) {
            logger.warnv("Rejecting email login request due to rate limit (email={0}, ip={1})",
                normalizedEmail, ipAddress);
            attemptRepository.log(normalizedEmail, ipAddress, AttemptType.REQUEST, AttemptResult.RATE_LIMITED);
            throw new TooManyRequestsException("Too many login requests", rateLimitWindow.toSeconds());
        }

        final String code = SecureTokens.randomDigits(CODE_LENGTH);
        final String salt = SecureTokens.randomBase64Url(SALT_BYTES);
        codeRepository.create(normalizedEmail, hash(salt, code), salt, now.plus(codeTtl), now);
        attemptRepository.log(normalizedEmail, ipAddress, AttemptType.REQUEST, AttemptResult.SENT);
        notificationController.informUserAboutLoginCode(findUser(normalizedEmail).orElse(null), normalizedEmail,
            code, locale);
    }

    /**
     * Validates the login code of the email address and authenticates the user with an {@code EMAIL} identity.
     * On the first login a new user is created, a placeholder user is reclaimed or the identity is linked to the
     * active user owning this verified email address.
     *
     * @throws UnauthorizedException    if the code is invalid, expired, already used or has no attempts left
     * @throws TooManyRequestsException if the client exceeded the rate limit
     */
    public UserModel verifyCode(final String email, final String code, final String locale,
        final String ipAddress) {
        final String normalizedEmail = normalize(email);
        final LocalDateTime now = LocalDateTime.now();
        if (attemptRepository.countByIpAddressSince(ipAddress, AttemptType.VERIFY, now.minus(rateLimitWindow))
            >= verificationsPerIp) {
            logger.warnv("Rejecting email login attempt due to rate limit (email={0}, ip={1})",
                normalizedEmail, ipAddress);
            attemptRepository.log(normalizedEmail, ipAddress, AttemptType.VERIFY, AttemptResult.RATE_LIMITED);
            throw new TooManyRequestsException("Too many login attempts", rateLimitWindow.toSeconds());
        }

        final Optional<EmailLoginCodeEntity> loginCode = codeRepository.findActiveByEmail(normalizedEmail, now);
        // the attempt is counted before the comparison, so a code has at most maxAttempts comparisons
        if (loginCode.isEmpty()
            || !codeRepository.incrementAttempts(loginCode.get().getId(), maxAttempts, now)
            || !SecureTokens.isEqual(loginCode.get().getCodeHash(), hash(loginCode.get().getCodeSalt(), code))
            || !codeRepository.markUsed(loginCode.get().getId(), now)) {
            logger.infov("Invalid email login code (email={0}, ip={1})", normalizedEmail, ipAddress);
            attemptRepository.log(normalizedEmail, ipAddress, AttemptType.VERIFY, AttemptResult.INVALID_CODE);
            throw new UnauthorizedException("Invalid login code");
        }

        attemptRepository.log(normalizedEmail, ipAddress, AttemptType.VERIFY, AttemptResult.SUCCESS);
        return authorizationController.authenticateUser(IdentityProvider.EMAIL, normalizedEmail,
            normalizedEmail, true, locale);
    }

    /**
     * Deletes expired and used codes as well as login attempts older than the retention period.
     */
    @Scheduled(every = "${de.remsfal.auth.email-login.cleanup-interval:1h}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void cleanup() {
        final LocalDateTime now = LocalDateTime.now();
        final int codes = codeRepository.deleteExpiredOrUsed(now);
        final int attempts = attemptRepository.deleteOlderThan(now.minus(attemptRetention));
        logger.infov("Deleted expired email login codes and attempts (codes={0}, attempts={1})", codes, attempts);
    }

    private Optional<UserEntity> findUser(final String email) {
        return userRepository.findByEmail(email)
            .or(() -> additionalEmailRepository.findVerifiedByEmail(email).map(AdditionalEmailEntity::getUser));
    }

    private static String normalize(final String email) {
        return email.trim().toLowerCase();
    }

    private static String hash(final String salt, final String code) {
        return SecureTokens.sha256Hex(salt + ":" + code);
    }

}
