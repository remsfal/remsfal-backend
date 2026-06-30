package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.OrderPlacementEntity;
import de.remsfal.ticketing.entity.dto.OrderPlacementKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class OrderPlacementRepository extends AbstractRepository<OrderPlacementEntity, OrderPlacementKey> {

    public OrderPlacementEntity insert(final OrderPlacementEntity entity) {
        Instant now = Instant.now();
        entity.setModifiedAt(now);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        return template.insert(entity);
    }

}
