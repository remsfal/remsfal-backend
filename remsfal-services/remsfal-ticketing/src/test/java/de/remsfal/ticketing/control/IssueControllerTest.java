package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.MessagePurpose;
import de.remsfal.ticketing.boundary.eventing.IssueEventProducer;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;

/**
 * Covers the multi-partition fan-out/merge that moved from {@link IssueRepository} into
 * {@link IssueController#getTenancyIssues(Map, UUID, Integer)} once the repository was reduced to
 * single-partition queries. Uses a mocked {@link IssueRepository} so the merge/sort/limit logic
 * can be verified without a real Cassandra instance.
 */
class IssueControllerTest {

    private IssueEntity issueOf(final UUID projectId, final UUID issueId) {
        final IssueEntity entity = new IssueEntity();
        final IssueKey key = new IssueKey();
        key.setProjectId(projectId);
        key.setIssueId(issueId);
        entity.setKey(key);
        return entity;
    }

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

        final Map<UUID, UUID> tenancyProjects = new LinkedHashMap<>();
        tenancyProjects.put(agreementA, projectA);
        tenancyProjects.put(agreementB, projectB);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByQuery(new IssueFilter(projectA, null, agreementA, null, null, null, null),
            true, null, 2))
            .thenReturn(List.of(issueOf(projectA, id4), issueOf(projectA, id1)));
        when(repository.findByQuery(new IssueFilter(projectB, null, agreementB, null, null, null, null),
            true, null, 2))
            .thenReturn(List.of(issueOf(projectB, id3), issueOf(projectB, id2)));

        final IssueController controller = new IssueController();
        controller.issueRepository = repository;

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

        final Map<UUID, UUID> tenancyProjects = new LinkedHashMap<>();
        tenancyProjects.put(agreementA, projectA);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByQuery(new IssueFilter(projectA, null, agreementA, null, null, null, null),
            true, cursor, 10))
            .thenReturn(List.of(issueOf(projectA, new UUID(0, 4))));

        final IssueController controller = new IssueController();
        controller.issueRepository = repository;

        final List<? extends IssueModel> page = controller.getTenancyIssues(tenancyProjects, cursor, 10);

        assertEquals(1, page.size());
        assertEquals(new UUID(0, 4), page.get(0).getId());
    }

    @Test
    void getTenancyIssues_noTenancies_returnsEmptyListWithoutQuerying() {
        final IssueController controller = new IssueController();

        final List<? extends IssueModel> page = controller.getTenancyIssues(Map.of(), null, 10);

        assertTrue(page.isEmpty());
    }

    @Test
    void getProjectIssues_delegatesDirectlyToRepositoryForSinglePartition() {
        final UUID projectId = UUID.randomUUID();
        final UUID cursor = UUID.randomUUID();
        final IssueEntity expected = issueOf(projectId, UUID.randomUUID());

        final IssueFilter filter = new IssueFilter(projectId, null, null, null, null,
            null, List.of(IssueStatus.OPEN));

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByQuery(filter, false, cursor, 10))
            .thenReturn(List.of(expected));

        final IssueController controller = new IssueController();
        controller.issueRepository = repository;

        final List<? extends IssueModel> result = controller.getProjectIssues(filter, cursor, 10);

        assertEquals(List.of(expected), result);
    }

    private IssueController controllerWithMocks(final IssueRepository repository,
        final TimelineController timelineController, final RemsfalPrincipal principal) {
        final IssueController controller = new IssueController();
        controller.logger = Logger.getLogger(IssueControllerTest.class);
        controller.issueRepository = repository;
        controller.issueEventProducer = mock(IssueEventProducer.class);
        controller.timelineController = timelineController;
        controller.principal = principal;
        return controller;
    }

    private IssueRepository insertingRepository() {
        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.insert(any(IssueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return repository;
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

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(insertingRepository(), timelineController, null);

        final IssueModel created = controller.createProjectIssue(user, issue);

        verify(timelineController).createTimelineEntry(agreementId, created.getId(), projectId,
            reporterId, "Max Manager", MessagePurpose.ISSUE_CREATED, "Die Heizung ist defekt");
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

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(insertingRepository(), timelineController, null);

        controller.createProjectIssue(user, issue);

        verify(timelineController, never()).createTimelineEntry(
            any(), any(), any(), any(), any(), any(MessagePurpose.class), any());
    }

    @Test
    void createIssue_managerCreated_noAgreement_createsNoTimelineEntry() {
        final UserModel user = mock(UserModel.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getName()).thenReturn("Max Manager");

        final IssueModel issue = mock(IssueModel.class);
        when(issue.getProjectId()).thenReturn(UUID.randomUUID());

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(insertingRepository(), timelineController, null);

        controller.createProjectIssue(user, issue);

        verify(timelineController, never()).createTimelineEntry(
            any(), any(), any(), any(), any(), any(MessagePurpose.class), any());
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

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(insertingRepository(), timelineController, null);

        controller.createTenancyIssue(user, issue, projectId);

        verify(timelineController, never()).createTimelineEntry(
            any(), any(), any(), any(), any(), any(MessagePurpose.class), any());
        verify(timelineController, never()).createTimelineEntry(
            any(), any(), any(), any(), any(), any(MessagePurpose.class), any(), any());
    }

    @Test
    void updateIssue_statusChangedOnVisibleTenancyIssue_createsStatusChangedTimelineEntry() {
        final UUID issueId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        final UUID principalId = UUID.randomUUID();

        final IssueEntity existing = issueOf(projectId, issueId);
        existing.setStatus(IssueStatus.OPEN);
        existing.setAgreementId(agreementId);
        existing.setVisibleToTenants(true);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByIssueId(issueId)).thenReturn(Optional.of(existing));
        when(repository.update(existing)).thenReturn(existing);

        final RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        when(principal.getId()).thenReturn(principalId);
        when(principal.getName()).thenReturn("Max Manager");

        final IssueModel patch = mock(IssueModel.class);
        when(patch.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(repository, timelineController, principal);

        controller.updateIssue(issueId, patch);

        verify(timelineController).createTimelineEntry(agreementId, issueId, projectId,
            principalId, "Max Manager", MessagePurpose.STATUS_CHANGED, "IN_PROGRESS");
    }

    @Test
    void updateIssue_sameStatusResent_createsNoTimelineEntry() {
        final UUID issueId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();

        final IssueEntity existing = issueOf(projectId, issueId);
        existing.setStatus(IssueStatus.OPEN);
        existing.setAgreementId(UUID.randomUUID());
        existing.setVisibleToTenants(true);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByIssueId(issueId)).thenReturn(Optional.of(existing));
        when(repository.update(existing)).thenReturn(existing);

        final IssueModel patch = mock(IssueModel.class);
        when(patch.getStatus()).thenReturn(IssueStatus.OPEN);

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(repository, timelineController,
            mock(RemsfalPrincipal.class));

        controller.updateIssue(issueId, patch);

        verify(timelineController, never()).createTimelineEntry(
            any(), any(), any(), any(), any(), any(MessagePurpose.class), any());
    }

    @Test
    void updateIssue_notVisibleToTenants_createsNoTimelineEntry() {
        final UUID issueId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();

        final IssueEntity existing = issueOf(projectId, issueId);
        existing.setStatus(IssueStatus.OPEN);
        existing.setAgreementId(UUID.randomUUID());
        existing.setVisibleToTenants(false);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByIssueId(issueId)).thenReturn(Optional.of(existing));
        when(repository.update(existing)).thenReturn(existing);

        final IssueModel patch = mock(IssueModel.class);
        when(patch.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(repository, timelineController,
            mock(RemsfalPrincipal.class));

        controller.updateIssue(issueId, patch);

        verify(timelineController, never()).createTimelineEntry(
            any(), any(), any(), any(), any(), any(MessagePurpose.class), any());
    }

    @Test
    void updateIssue_noAgreement_createsNoTimelineEntry() {
        final UUID issueId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();

        final IssueEntity existing = issueOf(projectId, issueId);
        existing.setStatus(IssueStatus.OPEN);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByIssueId(issueId)).thenReturn(Optional.of(existing));
        when(repository.update(existing)).thenReturn(existing);

        final IssueModel patch = mock(IssueModel.class);
        when(patch.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(repository, timelineController,
            mock(RemsfalPrincipal.class));

        controller.updateIssue(issueId, patch);

        verify(timelineController, never()).createTimelineEntry(
            any(), any(), any(), any(), any(), any(MessagePurpose.class), any());
    }

    @Test
    void closeIssue_openVisibleTenancyIssue_createsStatusChangedTimelineEntry() {
        final UUID issueId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();
        final UUID agreementId = UUID.randomUUID();
        final UUID principalId = UUID.randomUUID();

        final IssueEntity existing = issueOf(projectId, issueId);
        existing.setStatus(IssueStatus.OPEN);
        existing.setAgreementId(agreementId);
        existing.setVisibleToTenants(true);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByIssueId(issueId)).thenReturn(Optional.of(existing));
        when(repository.update(existing)).thenReturn(existing);

        final RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        when(principal.getId()).thenReturn(principalId);
        when(principal.getName()).thenReturn("Tina Tenant");

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(repository, timelineController, principal);

        controller.closeIssue(issueId);

        verify(timelineController).createTimelineEntry(agreementId, issueId, projectId,
            principalId, "Tina Tenant", MessagePurpose.STATUS_CHANGED, "CLOSED");
    }

    @Test
    void closeIssue_alreadyClosed_createsNoAdditionalTimelineEntry() {
        final UUID issueId = UUID.randomUUID();
        final UUID projectId = UUID.randomUUID();

        final IssueEntity existing = issueOf(projectId, issueId);
        existing.setStatus(IssueStatus.CLOSED);
        existing.setAgreementId(UUID.randomUUID());
        existing.setVisibleToTenants(true);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByIssueId(issueId)).thenReturn(Optional.of(existing));
        when(repository.update(existing)).thenReturn(existing);

        final TimelineController timelineController = mock(TimelineController.class);
        final IssueController controller = controllerWithMocks(repository, timelineController,
            mock(RemsfalPrincipal.class));

        controller.closeIssue(issueId);

        verify(timelineController, never()).createTimelineEntry(
            any(), any(), any(), any(), any(), any(MessagePurpose.class), any());
    }

}
