package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.TenantTimelineEntity;
import de.remsfal.ticketing.entity.dto.TenantTimelineKey;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TenantTimelineRepository extends AbstractRepository<TenantTimelineEntity, TenantTimelineKey> {
}