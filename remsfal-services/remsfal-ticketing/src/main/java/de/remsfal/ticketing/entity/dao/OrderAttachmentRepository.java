package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.OrderAttachmentEntity;
import de.remsfal.ticketing.entity.dto.OrderAttachmentKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrderAttachmentRepository extends AbstractRepository<OrderAttachmentEntity, OrderAttachmentKey> {

    private static final String COL_PROCESS_PHASE = "process_phase";
    private static final String COL_PROCESS_ID = "process_id";
    private static final String COL_ATTACHMENT_ID = "attachment_id";

    public OrderAttachmentEntity insert(OrderAttachmentEntity entity) {
        return template.insert(entity);
    }

    public Optional<OrderAttachmentEntity> findById(OrderAttachmentKey key) {
        return template.select(OrderAttachmentEntity.class)
            .where(COL_PROCESS_PHASE).eq(key.getProcessPhase())
            .and(COL_PROCESS_ID).eq(key.getProcessId())
            .and(COL_ATTACHMENT_ID).eq(key.getAttachmentId())
            .singleResult();
    }

    public List<OrderAttachmentEntity> findByProcess(String processPhase, UUID processId) {
        return template.select(OrderAttachmentEntity.class)
            .where(COL_PROCESS_PHASE).eq(processPhase)
            .and(COL_PROCESS_ID).eq(processId)
            .result();
    }

    public void delete(OrderAttachmentKey key) {
        template.delete(OrderAttachmentEntity.class)
            .where(COL_PROCESS_PHASE).eq(key.getProcessPhase())
            .and(COL_PROCESS_ID).eq(key.getProcessId())
            .and(COL_ATTACHMENT_ID).eq(key.getAttachmentId())
            .execute();
    }

    public void deleteByProcess(String processPhase, UUID processId) {
        template.delete(OrderAttachmentEntity.class)
            .where(COL_PROCESS_PHASE).eq(processPhase)
            .and(COL_PROCESS_ID).eq(processId)
            .execute();
    }

}
