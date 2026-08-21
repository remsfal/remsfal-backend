package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.TimelineEntity;
import de.remsfal.ticketing.entity.filter.IssueFilter;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Covers {@link IssueController} against a real Cassandra instance (see {@link AbstractTicketingTest}).
 * {@link RemsfalPrincipal} is the only mocked dependency here, since it only resolves a real identity
 * from a JWT-authenticated HTTP request ({@code RemsfalPrincipal.getId()}/{@code getName()} read the
 * current {@code JsonWebToken}), which a bare CDI-injected controller call doesn't have. Everything
 * else (repository, timeline controller, event producer) is the real bean, same as
 * {@code de.remsfal.ticketing.boundary.OcrTest} for a comparable
 * {@code @InjectMock RemsfalPrincipal} + real-DB combination.
 */
@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class IssueControllerTest extends AbstractTicketingTest {

    @Inject
    IssueController controller;

    @Inject
    IssueRepository issueRepository;

    @Inject
    TimelineController timelineController;

    @InjectMock
    RemsfalPrincipal principal;

    @Test
    void getTenancyIssues_mergesAndSortsAcrossAgreementsByIssueIdDescending() {
        final UUID agreementA = UUID.randomUUID();
        final UUID agreementB = UUID.randomUUID();
        final UUID projectA = UUID.randomUUID();
        final UUID projectB = UUID.randomUUID();

        // Deterministic, comparable issue ids (real code uses UUIDv7, ordering semantics are the same).
        final UUID id1 = new UUID(0, 1);
        final UUID id2 = new UUID(0, 2);
        final UUID id3 = new UUID(0, 3);
        final UUID id4 = new UUID(0, 4);

        insertIssue(projectA, id1, "Issue A1", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), agreementA, null, "Issue A1");
        insertIssue(projectA, id4, "Issue A4", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), agreementA, null, "Issue A4");
        insertIssue(projectB, id2, "Issue B2", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), agreementB, null, "Issue B2");
        insertIssue(projectB, id3, "Issue B3", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), agreementB, null, "Issue B3");

        final Map<UUID, UUID> tenancyProjects = new LinkedHashMap<>();
        tenancyProjects.put(agreementA, projectA);
        tenancyProjects.put(agreementB, projectB);

        final List<? extends IssueModel> page = controller.getTenancyIssues(tenancyProjects, null, 2);

        assertEquals(2, page.size());
        assertEquals(id4, page.get(0).getId());
        assertEquals(id3, page.get(1).getId());
    }

    @Test
    void getTenancyIssues_continuesFromCursorOnEveryPartition() {
        final UUID agreementA = UUID.randomUUID();
        final UUID projectA = UUID.randomUUID();
        final UUID cursor = new UUID(0, 5);
        final UUID id4 = new UUID(0, 4);

        insertIssue(projectA, id4, "Issue A4", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), agreementA, null, "Issue A4");

        final Map<UUID, UUID> tenancyProjects = new LinkedHashMap<>();
        tenancyProjects.put(agreementA, projectA);

        final List<? extends IssueModel> page = controller.getTenancyIssues(tenancyProjects, cursor, 10);

        assertEquals(1, page.size());
        assertEquals(id4, page.get(0).getId());
    }

    @Test
    void getTenancyIssues_noTenancies_returnsEmptyListWithoutQuerying() {
        final List<? extends IssueModel> page = controller.getTenancyIssues(Map.of(), null, 10);

        assertTrue(page.isEmpty());
    }

    @Test
    void getProjectIssues_delegatesDirectlyToRepositoryForSinglePartition() {
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = new UUID(0, 1);
        final UUID cursor = new UUID(0, 2);

        insertIssue(projectId, issueId, "Issue", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), null, null, "Issue");

        final IssueFilter filter = new IssueFilter(projectId, null, null, null, null,
            null, List.of(IssueStatus.OPEN), null);

        final List<? extends IssueModel> result = controller.getProjectIssues(filter, cursor, 10);

        assertEquals(1, result.size());
        assertEquals(issueId, result.get(0).getId());
    }

    @Test
    void createIssue_managerCreated_visibleToTenants_createsIssueCreatedTimelineEntry() {
        final UUID projectId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        final UUID reporterId = UUID.randomUUID();

        final UserModel user = mock(UserModel.class);
        when(user.getId()).thenReturn(reporterId);
        when(user.getName()).thenReturn("Max Manager");

        final IssueModel issue = mock(IssueModel.class);
        when(issue.getProjectId()).thenReturn(projectId);
        when(issue.getAgreementId()).thenReturn(agreementId);
        when(issue.isVisibleToTenants()).thenReturn(true);
        when(issue.getDescription()).thenReturn("Die Heizung ist defekt");

        final IssueModel created = controller.createProjectIssue(user, issue);

        final List<TimelineEntity> entries =
            timelineController.getTimelineEntries(agreementId, created.getId(), projectId);
        assertEquals(1, entries.size());
        assertEquals(MessagePurpose.ISSUE_CREATED, entries.get(0).getPurpose());
        assertEquals("Die Heizung ist defekt", entries.get(0).getMessage());
        assertEquals(reporterId, entries.get(0).getSenderId());
        assertEquals("Max Manager", entries.get(0).getSenderName());
    }

    @Test
    void createIssue_managerCreated_notVisibleToTenants_createsNoTimelineEntry() {
        final UUID projectId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();

        final UserModel user = mock(UserModel.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getName()).thenReturn("Max Manager");

        final IssueModel issue = mock(IssueModel.class);
        when(issue.getProjectId()).thenReturn(projectId);
        when(issue.getAgreementId()).thenReturn(agreementId);
        when(issue.isVisibleToTenants()).thenReturn(false);

        final IssueModel created = controller.createProjectIssue(user, issue);

        assertTrue(timelineController.getTimelineEntries(agreementId, created.getId(), projectId).isEmpty());
    }

    @Test
    void createIssue_managerCreated_noAgreement_createsNoTimelineEntry() {
        final UUID projectId = UUID.randomUUID();

        final UserModel user = mock(UserModel.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getName()).thenReturn("Max Manager");

        final IssueModel issue = mock(IssueModel.class);
        when(issue.getProjectId()).thenReturn(projectId);

        controller.createProjectIssue(user, issue);

        // without an agreementId there is no tenancy to query the timeline by; assert directly
        // against the (freshly truncated, see AbstractTicketingTest) table that nothing was written
        assertEquals(0L, cqlSession.execute("SELECT COUNT(*) FROM remsfal.tenant_timelines").one().getLong(0));
    }

    @Test
    void createIssue_tenantCreated_createsNoTimelineEntryInternally() {
        // The tenant-facing create-with-attachments flow uploads attachments only after the issue
        // (and its id) exist, so it creates its own ISSUE_CREATED timeline entry (carrying the
        // attachment ids) afterwards instead of relying on an automatic one from IssueController.
        final UUID projectId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        final UUID reporterId = UUID.randomUUID();

        final UserModel user = mock(UserModel.class);
        when(user.getId()).thenReturn(reporterId);
        when(user.getName()).thenReturn("Tina Tenant");

        final IssueModel issue = mock(IssueModel.class);
        when(issue.getAgreementId()).thenReturn(agreementId);
        when(issue.isVisibleToTenants()).thenReturn(false);
        when(issue.getDescription()).thenReturn("Bitte um Rueckruf");

        final IssueModel created = controller.createTenancyIssue(user, issue, projectId);

        assertTrue(timelineController.getTimelineEntries(agreementId, created.getId(), projectId).isEmpty());
    }

    @Test
    void updateIssue_locationProvided_updatesLocation() {
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        insertIssue(projectId, issueId, "Issue", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), null, null, "Issue");
        final IssueEntity existing = issueRepository.findByIssueId(issueId).orElseThrow();
        existing.setLocation("Keller");
        issueRepository.update(existing);

        when(principal.getId()).thenReturn(UUID.randomUUID());
        when(principal.getName()).thenReturn("Max Manager");

        final IssueModel patch = mock(IssueModel.class);
        when(patch.getLocation()).thenReturn("Dachgeschoss");

        final IssueModel updated = controller.updateIssue(issueId, patch);

        assertEquals("Dachgeschoss", updated.getLocation());
    }

    @Test
    void updateIssue_locationNotProvided_keepsExistingLocation() {
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        insertIssue(projectId, issueId, "Issue", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), null, null, "Issue");
        final IssueEntity existing = issueRepository.findByIssueId(issueId).orElseThrow();
        existing.setLocation("Keller");
        issueRepository.update(existing);

        when(principal.getId()).thenReturn(UUID.randomUUID());
        when(principal.getName()).thenReturn("Max Manager");

        final IssueModel patch = mock(IssueModel.class);

        final IssueModel updated = controller.updateIssue(issueId, patch);

        assertEquals("Keller", updated.getLocation());
    }

    @Test
    void updateIssue_statusChangedOnVisibleTenancyIssue_createsStatusChangedTimelineEntry() {
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        final UUID principalId = UUID.randomUUID();

        insertIssue(projectId, issueId, "Issue", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), agreementId, null, "Issue");

        when(principal.getId()).thenReturn(principalId);
        when(principal.getName()).thenReturn("Max Manager");

        final IssueModel patch = mock(IssueModel.class);
        when(patch.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);

        controller.updateIssue(issueId, patch);

        final List<TimelineEntity> entries = timelineController.getTimelineEntries(agreementId, issueId, projectId);
        assertEquals(1, entries.size());
        assertEquals(MessagePurpose.STATUS_CHANGED, entries.get(0).getPurpose());
        assertEquals("IN_PROGRESS", entries.get(0).getMessage());
        assertEquals(principalId, entries.get(0).getSenderId());
        assertEquals("Max Manager", entries.get(0).getSenderName());
    }

    @Test
    void updateIssue_sameStatusResent_createsNoTimelineEntry() {
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();

        insertIssue(projectId, issueId, "Issue", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), agreementId, null, "Issue");

        when(principal.getId()).thenReturn(UUID.randomUUID());
        when(principal.getName()).thenReturn("Max Manager");

        final IssueModel patch = mock(IssueModel.class);
        when(patch.getStatus()).thenReturn(IssueStatus.OPEN);

        controller.updateIssue(issueId, patch);

        assertTrue(timelineController.getTimelineEntries(agreementId, issueId, projectId).isEmpty());
    }

    @Test
    void updateIssue_notVisibleToTenants_createsNoTimelineEntry() {
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();

        insertIssue(projectId, issueId, "Issue", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), null, null, "Issue");
        final IssueEntity existing = issueRepository.findByIssueId(issueId).orElseThrow();
        existing.setAgreementId(agreementId);
        existing.setVisibleToTenants(false);
        issueRepository.update(existing);

        when(principal.getId()).thenReturn(UUID.randomUUID());
        when(principal.getName()).thenReturn("Max Manager");

        final IssueModel patch = mock(IssueModel.class);
        when(patch.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);

        controller.updateIssue(issueId, patch);

        assertTrue(timelineController.getTimelineEntries(agreementId, issueId, projectId).isEmpty());
    }

    @Test
    void updateIssue_noAgreement_createsNoTimelineEntry() {
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();

        insertIssue(projectId, issueId, "Issue", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), null, null, "Issue");

        when(principal.getId()).thenReturn(UUID.randomUUID());
        when(principal.getName()).thenReturn("Max Manager");

        final IssueModel patch = mock(IssueModel.class);
        when(patch.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);

        controller.updateIssue(issueId, patch);

        assertEquals(0L, cqlSession.execute("SELECT COUNT(*) FROM remsfal.tenant_timelines").one().getLong(0));
    }

    @Test
    void closeIssue_openVisibleTenancyIssue_createsStatusChangedTimelineEntry() {
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        final UUID principalId = UUID.randomUUID();

        insertIssue(projectId, issueId, "Issue", IssueType.DEFECT, IssueStatus.OPEN, IssuePriority.MEDIUM,
            UUID.randomUUID(), agreementId, null, "Issue");

        when(principal.getId()).thenReturn(principalId);
        when(principal.getName()).thenReturn("Tina Tenant");

        controller.closeIssue(issueId);

        final List<TimelineEntity> entries = timelineController.getTimelineEntries(agreementId, issueId, projectId);
        assertEquals(1, entries.size());
        assertEquals(MessagePurpose.STATUS_CHANGED, entries.get(0).getPurpose());
        assertEquals("CLOSED", entries.get(0).getMessage());
        assertEquals(principalId, entries.get(0).getSenderId());
        assertEquals("Tina Tenant", entries.get(0).getSenderName());
    }

    @Test
    void closeIssue_alreadyClosed_createsNoAdditionalTimelineEntry() {
        final UUID projectId = UUID.randomUUID();
        final UUID issueId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();

        insertIssue(projectId, issueId, "Issue", IssueType.DEFECT, IssueStatus.CLOSED, IssuePriority.MEDIUM,
            UUID.randomUUID(), agreementId, null, "Issue");

        controller.closeIssue(issueId);

        assertTrue(timelineController.getTimelineEntries(agreementId, issueId, projectId).isEmpty());
    }

}
