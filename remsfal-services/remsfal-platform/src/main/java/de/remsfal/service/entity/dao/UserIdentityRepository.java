package de.remsfal.service.entity.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;
import de.remsfal.service.entity.dto.UserIdentityEntity;

@ApplicationScoped
public class UserIdentityRepository extends AbstractRepository<UserIdentityEntity> {

    private static final String PARAM_PROVIDER = "provider";
    private static final String PARAM_SUBJECT = "subject";

    public Optional<UserIdentityEntity> findByProviderAndSubject(final IdentityProvider provider,
        final String subject) {
        return find("from UserIdentityEntity identity join fetch identity.user "
            + "where identity.provider = ?1 and identity.subject = ?2", provider, subject).singleResultOptional();
    }

    @Transactional
    public boolean updateLastUsedAt(final IdentityProvider provider, final String subject,
        final LocalDateTime timestamp) {
        return getEntityManager().createNamedQuery("UserIdentityEntity.updateLastUsedAt")
            .setParameter("timestamp", timestamp)
            .setParameter(PARAM_PROVIDER, provider)
            .setParameter(PARAM_SUBJECT, subject)
            .executeUpdate() > 0;
    }

    public List<UserIdentityEntity> findByUserIdAndProvider(final UUID userId, final IdentityProvider provider) {
        return find("user.id = ?1 and provider = ?2", userId, provider).list();
    }

    @Transactional
    public boolean updateRefreshToken(final IdentityProvider provider, final String subject,
        final String refreshToken, final String clientId) {
        return getEntityManager().createNamedQuery("UserIdentityEntity.updateRefreshToken")
            .setParameter("refreshToken", refreshToken)
            .setParameter("clientId", clientId)
            .setParameter(PARAM_PROVIDER, provider)
            .setParameter(PARAM_SUBJECT, subject)
            .executeUpdate() > 0;
    }

    @Transactional
    public boolean updateEmailDisabled(final IdentityProvider provider, final String subject,
        final boolean emailDisabled) {
        return getEntityManager().createNamedQuery("UserIdentityEntity.updateEmailDisabled")
            .setParameter("emailDisabled", emailDisabled)
            .setParameter(PARAM_PROVIDER, provider)
            .setParameter(PARAM_SUBJECT, subject)
            .executeUpdate() > 0;
    }

    @Transactional
    public boolean deleteByProviderAndSubject(final IdentityProvider provider, final String subject) {
        return getEntityManager().createNamedQuery("UserIdentityEntity.deleteByProviderAndSubject")
            .setParameter(PARAM_PROVIDER, provider)
            .setParameter(PARAM_SUBJECT, subject)
            .executeUpdate() > 0;
    }

}
