package de.remsfal.service.entity.dto;

import java.util.Objects;
import java.util.UUID;

import de.remsfal.core.model.UserAuthenticationModel;
import de.remsfal.service.entity.dto.superclass.MetaDataEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@NamedQuery(name = "UserAuthenticationEntity.findByUserId",
    query = "select userAuth from UserAuthenticationEntity userAuth where userAuth.user.id = :userId")
@NamedQuery(name = "UserAuthenticationEntity.updateRefreshTokenId",
    query = "update UserAuthenticationEntity userAuth set userAuth.refreshTokenId = :refreshTokenId " +
        "where userAuth.user.id = :userId")
@Table(name = "user_authentications")
public class UserAuthenticationEntity extends MetaDataEntity implements UserAuthenticationModel {

    @Id
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, columnDefinition = "uuid")
    private UserEntity user;

    @Column(name = "refresh_token_id")
    private UUID refreshTokenId;

    @Override
    public UUID getId() {
        return user != null ? user.getId() : null;
    }

    @Override
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }

    @Override
    public String getName() {
        return user != null ? user.getName() : null;
    }

    @Override
    public Boolean isActive() {
        return user != null ? user.isActive() : null;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(final UserEntity user) {
        this.user = user;
    }

    @Override
    public UUID getRefreshTokenId() {
        return refreshTokenId;
    }

    public void setRefreshTokenId(final UUID refreshTokenId) {
        this.refreshTokenId = refreshTokenId;
    }

    @Override
    public int hashCode() {
        if (user != null) {
            return Objects.hash(user.getId());
        } else {
            return Objects.hash((Object) null);
        }
    }

}
