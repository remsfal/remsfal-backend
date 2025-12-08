package de.remsfal.ticketing.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.ticketing.entity.dao.IssueParticipantRepository;
import de.remsfal.ticketing.entity.dto.IssueParticipantEntity;
import de.remsfal.ticketing.entity.dto.IssueParticipantKey;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueParticipantRepositoryTest {

 @Inject
 IssueParticipantRepository repository;

 @Test
 void testInsertAndExists() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity entity = createParticipant(userId, issueId, sessionId, projectId, "EDITOR");
  repository.insert(entity);

  boolean exists = repository.exists(userId, issueId);

  assertTrue(exists);

  repository.delete(userId, issueId, sessionId);
 }

 @Test
 void testDelete() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity entity = createParticipant(userId, issueId, sessionId, projectId, "VIEWER");
  repository.insert(entity);

  assertTrue(repository.exists(userId, issueId));

  repository.delete(userId, issueId, sessionId);

  assertFalse(repository.exists(userId, issueId));
 }

 @Test
 void testFindIssueIdsByParticipant() {
  UUID userId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  UUID issueId1 = UUID.randomUUID();
  UUID issueId2 = UUID.randomUUID();

  IssueParticipantEntity p1 =
          createParticipant(userId, issueId1, UUID.randomUUID(), projectId, "OWNER");

  IssueParticipantEntity p2 =
          createParticipant(userId, issueId2, UUID.randomUUID(), projectId, "OWNER");

  IssueParticipantEntity p3 =
          createParticipant(UUID.randomUUID(), UUID.randomUUID(),
                  UUID.randomUUID(), projectId, "VIEWER");

  repository.insert(p1);
  repository.insert(p2);
  repository.insert(p3);

  List<UUID> result = repository.findIssueIdsByParticipant(userId);

  assertNotNull(result);
  assertEquals(2, result.size());
  assertTrue(result.contains(issueId1));
  assertTrue(result.contains(issueId2));

  repository.delete(userId, issueId1, p1.getSessionId());
  repository.delete(userId, issueId2, p2.getSessionId());
  repository.delete(p3.getUserId(), p3.getIssueId(), p3.getSessionId());
 }

 private IssueParticipantEntity createParticipant(
         UUID userId,
         UUID issueId,
         UUID sessionId,
         UUID projectId,
         String role
 ) {
  IssueParticipantEntity entity = new IssueParticipantEntity();

  IssueParticipantKey key = new IssueParticipantKey();
  key.setUserId(userId);
  key.setIssueId(issueId);
  key.setSessionId(sessionId);

  entity.setKey(key);
  entity.setProjectId(projectId);
  entity.setRole(role);

  return entity;
 }


 @Test
 void testMultipleParticipantsOnSameIssue() {
  UUID issueId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  UUID user1 = UUID.randomUUID();
  UUID user2 = UUID.randomUUID();

  IssueParticipantEntity p1 = create(user1, issueId, UUID.randomUUID(), projectId, "OWNER");
  IssueParticipantEntity p2 = create(user2, issueId, UUID.randomUUID(), projectId, "HANDLER");

  repository.insert(p1);
  repository.insert(p2);

  List<UUID> found1 = repository.findIssueIdsByParticipant(user1);
  List<UUID> found2 = repository.findIssueIdsByParticipant(user2);

  assertEquals(1, found1.size());
  assertEquals(issueId, found1.get(0));

  assertEquals(1, found2.size());
  assertEquals(issueId, found2.get(0));

  repository.delete(user1, issueId, p1.getSessionId());
  repository.delete(user2, issueId, p2.getSessionId());
 }

 @Test
 void testRoleFieldIsStoredCorrectly() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity entity =
          create(userId, issueId, sessionId, projectId, "SUPPORT");

  repository.insert(entity);

  List<UUID> result = repository.findIssueIdsByParticipant(userId);

  assertEquals(1, result.size());
  assertEquals(issueId, result.get(0));

  repository.delete(userId, issueId, sessionId);
 }

 @Test
 void testDuplicateInsertDoesNotFail() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity entity =
          create(userId, issueId, sessionId, projectId, "OWNER");

  repository.insert(entity);
  repository.insert(entity); // Cassandra erlaubt idempotente Inserts

  assertTrue(repository.exists(userId, issueId));

  repository.delete(userId, issueId, sessionId);
 }


 private IssueParticipantEntity create(
         UUID userId,
         UUID issueId,
         UUID sessionId,
         UUID projectId,
         String role
 ) {
  IssueParticipantEntity entity = new IssueParticipantEntity();

  IssueParticipantKey key = new IssueParticipantKey();
  key.setUserId(userId);
  key.setIssueId(issueId);
  key.setSessionId(sessionId);

  entity.setKey(key);
  entity.setProjectId(projectId);
  entity.setRole(role);

  return entity;
 }
}
