package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.OrderAttachmentEntity;
import de.remsfal.ticketing.entity.dto.OrderAttachmentKey;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.jnosql.databases.cassandra.mapping.CassandraTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrderAttachmentRepository extends AbstractRepository<OrderAttachmentEntity, OrderAttachmentKey> {

    private static final String COL_PROCESS_PHASE = "process_phase";
    private static final String COL_PROCESS_ID = "process_id";
    private static final String COL_ATTACHMENT_ID = "attachment_id";

    @Inject
    CassandraTemplate cassandraTemplate;

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

    /**
     * Finds attachments for several processes of the same phase in a single query.
     * {@code process_id} is the trailing component of the table's partition key, so an
     * {@code IN} query is a plain multi-partition read and needs no secondary index.
     */
    public List<OrderAttachmentEntity> findByProcess(String processPhase, List<UUID> processIds) {
        if (processIds.isEmpty()) {
            return List.of();
        }
        return cassandraTemplate.<OrderAttachmentEntity>cql(
            "SELECT * FROM remsfal.order_attachments WHERE " + COL_PROCESS_PHASE + " = ? AND "
                + COL_PROCESS_ID + " IN ?", processPhase, processIds)
            .toList();
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
