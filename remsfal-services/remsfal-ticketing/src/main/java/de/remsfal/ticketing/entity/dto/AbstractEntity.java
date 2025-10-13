package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;

import java.time.Instant;

public abstract class AbstractEntity {

    @Column("created_at")
    protected Instant createdAt;

    @Column("modified_at")
    protected Instant modifiedAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public void touch() {
        this.modifiedAt = Instant.now();
    }

}
