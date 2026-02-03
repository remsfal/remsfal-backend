package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.IssueAttachmentEntity;
import de.remsfal.ticketing.entity.dto.IssueAttachmentKey;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class IssueAttachmentRepository extends AbstractRepository<IssueAttachmentEntity, IssueAttachmentKey> {

    private static final String COL_ISSUE_ID = "issue_id";
    private static final String COL_ATTACHMENT_ID = "attachment_id";

    public IssueAttachmentEntity insert(IssueAttachmentEntity entity) {
        return template.insert(entity);
    }

    public Optional<IssueAttachmentEntity> findById(IssueAttachmentKey key) {
        return template.select(IssueAttachmentEntity.class)
            .where(COL_ISSUE_ID).eq(key.getIssueId())
            .and(COL_ATTACHMENT_ID).eq(key.getAttachmentId())
            .singleResult();
    }

    public List<IssueAttachmentEntity> findByIssueId(UUID issueId) {
        return template.select(IssueAttachmentEntity.class)
            .where(COL_ISSUE_ID).eq(issueId)
            .result();
    }

    public void delete(IssueAttachmentKey key) {
        template.delete(IssueAttachmentEntity.class)
            .where(COL_ISSUE_ID).eq(key.getIssueId())
            .and(COL_ATTACHMENT_ID).eq(key.getAttachmentId())
            .execute();
    }

    public void deleteByIssueId(UUID issueId) {
        template.delete(IssueAttachmentEntity.class)
            .where(COL_ISSUE_ID).eq(issueId)
            .execute();
    }

}
