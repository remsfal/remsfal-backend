# Implementierungsschritte: Contractor-Funktionalitäten aktivieren

## Übersicht

Contractors können derzeit keine Issues, Chat Sessions oder Orders sehen, da die Authorization-Logik nur Manager (Projektrollen) und Tenants (Tenancy-Beziehungen) berücksichtigt. Contractors benötigen Zugriff über ChatSession-Teilnahme.

## Lösung

Neue `issue_participants` Tabelle für effiziente Lookups + Erweiterte Authorization-Logik.

---

## Phase 1: Datenbank-Schema

**1.1** Neue Cassandra-Tabelle `issue_participants` erstellen:
- **Datei:** `remsfal-services/remsfal-ticketing/src/main/resources/META-INF/changesets/005-create-issue-participants-table.xml`
- **Primary Key:** `(user_id, issue_id, session_id)` - partitioniert nach `user_id`
- **Spalten:** `user_id`, `project_id`, `issue_id`, `session_id`, `role`, `created_at`
- **Index:** auf `issue_id` für Reverse-Lookups

**1.2** Liquibase Changelog aktualisieren:
- **Datei:** `remsfal-services/remsfal-ticketing/src/main/resources/META-INF/liquibase-changelog.xml`
- Changeset einbinden: `<include file="META-INF/changesets/005-create-issue-participants-table.xml"/>`

---

## Phase 2: Entity & Repository Layer

**2.1** `IssueParticipantEntity.java` erstellen:
- `@Entity("issue_participants")`
- Composite Key: `IssueParticipantKey` (userId, issueId, sessionId)
- Felder: `projectId`, `role`, `createdAt`

**2.2** `IssueParticipantKey.java` erstellen:
- Composite Key mit `userId`, `issueId`, `sessionId` (alle UUID)

**2.3** `IssueParticipantRepository.java` erstellen:
- `findIssueIdsByParticipant(UUID userId)` → Liste von Issue-IDs
- `insert(IssueParticipantEntity entity)`
- `delete(UUID userId, UUID issueId, UUID sessionId)`
- `exists(UUID userId, UUID issueId)` → Boolean

---

## Phase 3: Daten-Synchronisation

**3.1** `ChatSessionRepository` erweitern:
- `addParticipant()`: Zusätzlich in `issue_participants` einfügen
- `deleteMember()`: Zusätzlich aus `issue_participants` löschen
- `createChatSession()`: Initiale Teilnehmer auch in `issue_participants` einfügen

**3.2** `ChatSessionController` aktualisieren:
- `IssueParticipantRepository` injizieren
- Teilnehmer-Operationen synchronisieren beide Tabellen

---

## Phase 4: Authorization-Logik

**4.1** `IssueResource.getTenancyIssues()` → `getUnprivilegedIssues()` umbenennen & erweitern:
- **Tenants:** via `getTenancyProjects()` (bestehend)
- **Contractors:** via `issueParticipantRepository.findIssueIdsByParticipant(principal.getId())`
- Ergebnisse mergen, Duplikate entfernen
- `IssueListJson.valueOfFiltered()` zurückgeben

**4.2** `IssueResource.getIssues()` aktualisieren:
- Zeile 52: `getTenancyIssues(...)` → `getUnprivilegedIssues(...)`

**4.3** `IssueResource.getIssue()` erweitern:
- Nach Tenant-Check: Contractor-Check hinzufügen:
  ```java
  else if (isParticipantInIssue(issueId)) {
      return IssueJson.valueOfFiltered(issue);
  }
  ```

**4.4** Helper-Methode `isParticipantInIssue(UUID issueId)` hinzufügen:
```java
private boolean isParticipantInIssue(UUID issueId) {
    return issueParticipantRepository.exists(principal.getId(), issueId);
}
```

**4.5** `AbstractResource.checkReadPermissions()` erweitern:
- Nach Manager-Check: Contractor-Check hinzufügen:
  ```java
  if (issueParticipantRepository.exists(principal.getId(), issueId)) {
      return issue.getProjectId();
  }
  ```

---

## Phase 5: Chat Session Zugriff

**5.1** `ChatSessionResource` aktualisieren:
- `getChatSession()` und `getChatSessions()` nutzen erweiterte `checkReadPermissions()`

**5.2** `ChatMessageResource` aktualisieren:
- Alle Chat-Message-Operationen prüfen Teilnehmer-Status (nicht nur Projektrollen)

---

## Phase 6: Migration

**6.1** Daten-Migration:
- Einmaliges Script: Bestehende `chat_sessions` → `issue_participants` migrieren
- Alle Chat-Sessions lesen, Teilnehmer aus MAP extrahieren, in neue Tabelle einfügen

**6.2** Rückwärtskompatibilität:
- Bestehende Funktionalität für Manager und Tenants unverändert

---

## Phase 7: Testing

- Unit Tests: `getUnprivilegedIssues()` mit Contractors, `isParticipantInIssue()`, Synchronisation
- Integration Tests: Contractor sieht Issues als Teilnehmer, Chat-Zugriff, gefilterte Views, keine Zugriffe ohne Teilnahme
- Edge Cases: User ist Tenant UND Contractor, mehrere Projekte, Teilnehmer entfernt

---

## Akzeptanzkriterien

- ✅ Contractors sehen Issues, wo sie Chat-Teilnehmer sind
- ✅ Contractors können Chat Sessions für ihre Issues öffnen
- ✅ Contractors erhalten gefilterte Views (kein Vollzugriff wie Manager)
- ✅ `issue_participants` Tabelle synchronisiert
- ✅ Bestehende Funktionalität für Manager/Tenants unverändert
- ✅ Alle Tests bestehen
- ✅ Performance akzeptabel (keine Full-Table-Scans)

---

## Technische Notizen

- **Datenbank:** Cassandra (NoSQL) - Denormalisierung für effiziente Queries erforderlich
- **Authorization Pattern:** Rollenbasiert + Teilnehmer-basiert für Contractors
- **Datenkonsistenz:** `chat_sessions` und `issue_participants` müssen synchron bleiben
- **Performance:** Neue Tabelle ermöglicht O(1) Lookups statt O(n) Scans



