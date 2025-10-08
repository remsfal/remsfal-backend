package de.remsfal.service.entity.dto;

import java.util.Objects;

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
@NamedQuery(name = "UserAuthenticationEntity.updateRefreshToken",
    query = "update UserAuthenticationEntity userAuth set userAuth.refreshToken = :refreshToken " +
        "where userAuth.user.id = :userId")
@NamedQuery(name = "UserAuthenticationEntity.deleteRefreshToken",
    query = "update UserAuthenticationEntity userAuth set userAuth.refreshToken = null " +
        "where userAuth.user.id = :userId")
@Table(name = "user_authentications")
public class UserAuthenticationEntity extends MetaDataEntity implements UserAuthenticationModel {

    @Id
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID", nullable = false, columnDefinition = "uuid")
    private UserEntity user;

    @Column(name = "REFRESH_TOKEN", nullable = true)
    private String refreshToken;

    @Override
    public UserEntity getUser() {
        return user != null ? user : null;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
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
