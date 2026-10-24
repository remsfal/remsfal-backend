package de.remsfal.notification.entity.dao;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import de.remsfal.notification.entity.dto.UserEmailSettingsEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class UserEmailSettingsRepository implements PanacheRepositoryBase<UserEmailSettingsEntity, UUID> {
}
