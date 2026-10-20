package de.remsfal.service.entity.dao;

import java.time.LocalDateTime;
import java.util.Optional;

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
    public boolean updateLastUsedAt(final IdentityProvider provider, final String subject, final LocalDateTime timestamp) {
        return getEntityManager().createNamedQuery("UserIdentityEntity.updateLastUsedAt")
            .setParameter("timestamp", timestamp)
            .setParameter(PARAM_PROVIDER, provider)
            .setParameter(PARAM_SUBJECT, subject)
            .executeUpdate() > 0;
    }

}
