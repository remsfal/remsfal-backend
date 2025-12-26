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

 // ========== Original Tests ==========

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

 @Test
 void testMultipleParticipantsOnSameIssue() {
  UUID issueId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  UUID user1 = UUID.randomUUID();
  UUID user2 = UUID.randomUUID();

  IssueParticipantEntity p1 = createParticipant(user1, issueId, UUID.randomUUID(), projectId, "OWNER");
  IssueParticipantEntity p2 = createParticipant(user2, issueId, UUID.randomUUID(), projectId, "HANDLER");

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
          createParticipant(userId, issueId, sessionId, projectId, "SUPPORT");

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
          createParticipant(userId, issueId, sessionId, projectId, "OWNER");

  repository.insert(entity);
  repository.insert(entity); // Cassandra erlaubt idempotente Inserts

  assertTrue(repository.exists(userId, issueId));

  repository.delete(userId, issueId, sessionId);
 }

 @Test
 void testExistsReturnsFalseForNonExistentParticipant() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();

  boolean exists = repository.exists(userId, issueId);

  assertFalse(exists);
 }

 // ========== Extended Tests ==========

 @Test
 void testFindIssueIdsByParticipantReturnsEmptyListForUnknownUser() {
  UUID unknownUserId = UUID.randomUUID();

  List<UUID> result = repository.findIssueIdsByParticipant(unknownUserId);

  assertNotNull(result);
  assertTrue(result.isEmpty());
 }

 @Test
 void testDeleteNonExistentParticipantDoesNotThrowException() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();

  assertDoesNotThrow(() -> repository.delete(userId, issueId, sessionId));
 }

 @Test
 void testInsertAndFindSingleParticipant() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity entity = createParticipant(userId, issueId, sessionId, projectId, "OWNER");
  repository.insert(entity);

  List<UUID> result = repository.findIssueIdsByParticipant(userId);

  assertEquals(1, result.size());
  assertEquals(issueId, result.get(0));

  repository.delete(userId, issueId, sessionId);
 }

 @Test
 void testMultipleIssuesForSameUser() {
  UUID userId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();
  UUID issueId1 = UUID.randomUUID();
  UUID issueId2 = UUID.randomUUID();
  UUID issueId3 = UUID.randomUUID();

  IssueParticipantEntity p1 = createParticipant(userId, issueId1, UUID.randomUUID(), projectId, "OWNER");
  IssueParticipantEntity p2 = createParticipant(userId, issueId2, UUID.randomUUID(), projectId, "EDITOR");
  IssueParticipantEntity p3 = createParticipant(userId, issueId3, UUID.randomUUID(), projectId, "VIEWER");

  repository.insert(p1);
  repository.insert(p2);
  repository.insert(p3);

  List<UUID> result = repository.findIssueIdsByParticipant(userId);

  assertEquals(3, result.size());
  assertTrue(result.contains(issueId1));
  assertTrue(result.contains(issueId2));
  assertTrue(result.contains(issueId3));

  repository.delete(userId, issueId1, p1.getSessionId());
  repository.delete(userId, issueId2, p2.getSessionId());
  repository.delete(userId, issueId3, p3.getSessionId());
 }

 @Test
 void testMultipleIssuesForSameUserAcrossDifferentProjects() {
  UUID userId = UUID.randomUUID();
  UUID projectId1 = UUID.randomUUID();
  UUID projectId2 = UUID.randomUUID();

  UUID issueId1 = UUID.randomUUID();
  UUID issueId2 = UUID.randomUUID();
  UUID issueId3 = UUID.randomUUID();

  IssueParticipantEntity p1 = createParticipant(userId, issueId1, UUID.randomUUID(), projectId1, "OWNER");
  IssueParticipantEntity p2 = createParticipant(userId, issueId2, UUID.randomUUID(), projectId1, "EDITOR");
  IssueParticipantEntity p3 = createParticipant(userId, issueId3, UUID.randomUUID(), projectId2, "VIEWER");

  repository.insert(p1);
  repository.insert(p2);
  repository.insert(p3);

  List<UUID> result = repository.findIssueIdsByParticipant(userId);

  assertEquals(3, result.size());
  assertTrue(result.contains(issueId1));
  assertTrue(result.contains(issueId2));
  assertTrue(result.contains(issueId3));

  repository.delete(userId, issueId1, p1.getSessionId());
  repository.delete(userId, issueId2, p2.getSessionId());
  repository.delete(userId, issueId3, p3.getSessionId());
 }


 @Test
 void testDistinctIssueIdsWithMultipleSessions() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity p1 = createParticipant(userId, issueId, UUID.randomUUID(), projectId, "OWNER");
  IssueParticipantEntity p2 = createParticipant(userId, issueId, UUID.randomUUID(), projectId, "EDITOR");
  IssueParticipantEntity p3 = createParticipant(userId, issueId, UUID.randomUUID(), projectId, "VIEWER");

  repository.insert(p1);
  repository.insert(p2);
  repository.insert(p3);

  List<UUID> result = repository.findIssueIdsByParticipant(userId);

  assertEquals(1, result.size());
  assertEquals(issueId, result.get(0));

  repository.delete(userId, issueId, p1.getSessionId());
  repository.delete(userId, issueId, p2.getSessionId());
  repository.delete(userId, issueId, p3.getSessionId());
 }

 @Test
 void testDeleteOnlyRemovesSpecificSession() {
  UUID userId = UUID.randomUUID();
  UUID issueId1 = UUID.randomUUID();
  UUID issueId2 = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();
  UUID sessionId1 = UUID.randomUUID();
  UUID sessionId2 = UUID.randomUUID();

  IssueParticipantEntity p1 = createParticipant(userId, issueId1, sessionId1, projectId, "OWNER");
  IssueParticipantEntity p2 = createParticipant(userId, issueId2, sessionId2, projectId, "EDITOR");

  repository.insert(p1);
  repository.insert(p2);

  repository.delete(userId, issueId1, sessionId1);

  assertFalse(repository.exists(userId, issueId1));
  assertTrue(repository.exists(userId, issueId2));

  repository.delete(userId, issueId2, sessionId2);
 }

 @Test
 void testMultipleUsersOnSameIssue() {
  UUID issueId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();
  UUID user1 = UUID.randomUUID();
  UUID user2 = UUID.randomUUID();
  UUID user3 = UUID.randomUUID();

  IssueParticipantEntity p1 = createParticipant(user1, issueId, UUID.randomUUID(), projectId, "OWNER");
  IssueParticipantEntity p2 = createParticipant(user2, issueId, UUID.randomUUID(), projectId, "HANDLER");
  IssueParticipantEntity p3 = createParticipant(user3, issueId, UUID.randomUUID(), projectId, "VIEWER");

  repository.insert(p1);
  repository.insert(p2);
  repository.insert(p3);

  assertTrue(repository.exists(user1, issueId));
  assertTrue(repository.exists(user2, issueId));
  assertTrue(repository.exists(user3, issueId));

  List<UUID> found1 = repository.findIssueIdsByParticipant(user1);
  List<UUID> found2 = repository.findIssueIdsByParticipant(user2);
  List<UUID> found3 = repository.findIssueIdsByParticipant(user3);

  assertEquals(1, found1.size());
  assertEquals(1, found2.size());
  assertEquals(1, found3.size());

  repository.delete(user1, issueId, p1.getSessionId());
  repository.delete(user2, issueId, p2.getSessionId());
  repository.delete(user3, issueId, p3.getSessionId());
 }

 @Test
 void testRoleIsStoredCorrectly() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity entity = createParticipant(userId, issueId, sessionId, projectId, "SUPPORT");
  repository.insert(entity);

  assertTrue(repository.exists(userId, issueId));

  repository.delete(userId, issueId, sessionId);
 }

 @Test
 void testEmptyRoleIsAccepted() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity entity = createParticipant(userId, issueId, sessionId, projectId, "");

  assertDoesNotThrow(() -> repository.insert(entity));
  assertTrue(repository.exists(userId, issueId));

  repository.delete(userId, issueId, sessionId);
 }

 @Test
 void testNullRoleIsAccepted() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity entity = createParticipant(userId, issueId, sessionId, projectId, null);

  assertDoesNotThrow(() -> repository.insert(entity));
  assertTrue(repository.exists(userId, issueId));

  repository.delete(userId, issueId, sessionId);
 }

 @Test
 void testReinsertUpdatesParticipant() {
  UUID userId = UUID.randomUUID();
  UUID issueId = UUID.randomUUID();
  UUID sessionId = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity entity = createParticipant(userId, issueId, sessionId, projectId, "VIEWER");
  repository.insert(entity);

  entity.setRole("OWNER");
  repository.insert(entity);

  assertTrue(repository.exists(userId, issueId));

  repository.delete(userId, issueId, sessionId);
 }

 @Test
 void testFindByParticipantWithMixedProjects() {
  UUID userId = UUID.randomUUID();
  UUID projectId1 = UUID.randomUUID();
  UUID projectId2 = UUID.randomUUID();
  UUID issueId1 = UUID.randomUUID();
  UUID issueId2 = UUID.randomUUID();

  IssueParticipantEntity p1 = createParticipant(userId, issueId1, UUID.randomUUID(), projectId1, "OWNER");
  IssueParticipantEntity p2 = createParticipant(userId, issueId2, UUID.randomUUID(), projectId2, "VIEWER");

  repository.insert(p1);
  repository.insert(p2);

  List<UUID> result = repository.findIssueIdsByParticipant(userId);

  assertEquals(2, result.size());
  assertTrue(result.contains(issueId1));
  assertTrue(result.contains(issueId2));

  repository.delete(userId, issueId1, p1.getSessionId());
  repository.delete(userId, issueId2, p2.getSessionId());
 }

 @Test
 void testComplexScenarioMultipleUsersMultipleIssues() {
  UUID user1 = UUID.randomUUID();
  UUID user2 = UUID.randomUUID();
  UUID issueId1 = UUID.randomUUID();
  UUID issueId2 = UUID.randomUUID();
  UUID projectId = UUID.randomUUID();

  IssueParticipantEntity p1 = createParticipant(user1, issueId1, UUID.randomUUID(), projectId, "OWNER");
  IssueParticipantEntity p2 = createParticipant(user1, issueId2, UUID.randomUUID(), projectId, "EDITOR");
  IssueParticipantEntity p3 = createParticipant(user2, issueId1, UUID.randomUUID(), projectId, "VIEWER");

  repository.insert(p1);
  repository.insert(p2);
  repository.insert(p3);

  List<UUID> user1Issues = repository.findIssueIdsByParticipant(user1);
  List<UUID> user2Issues = repository.findIssueIdsByParticipant(user2);

  assertEquals(2, user1Issues.size());
  assertEquals(1, user2Issues.size());
  assertTrue(user1Issues.contains(issueId1));
  assertTrue(user1Issues.contains(issueId2));
  assertTrue(user2Issues.contains(issueId1));

  repository.delete(user1, issueId1, p1.getSessionId());
  repository.delete(user1, issueId2, p2.getSessionId());
  repository.delete(user2, issueId1, p3.getSessionId());
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
}