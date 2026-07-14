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

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.dto.IssueKey;

/**
 * Covers the multi-partition fan-out/merge that moved from {@link IssueRepository} into
 * {@link IssueController#getTenancyIssues(UUID, Integer)} once the repository was reduced to
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

        final RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        when(principal.getTenancyProjects()).thenReturn(tenancyProjects);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByQuery(projectA, null, agreementA, null, null, null, true, null, 2))
            .thenReturn(List.of(issueOf(projectA, id4), issueOf(projectA, id1)));
        when(repository.findByQuery(projectB, null, agreementB, null, null, null, true, null, 2))
            .thenReturn(List.of(issueOf(projectB, id3), issueOf(projectB, id2)));

        final IssueController controller = new IssueController();
        controller.principal = principal;
        controller.issueRepository = repository;

        final List<? extends IssueModel> page = controller.getTenancyIssues(null, 2);

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

        final RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        when(principal.getTenancyProjects()).thenReturn(tenancyProjects);

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByQuery(projectA, null, agreementA, null, null, null, true, cursor, 10))
            .thenReturn(List.of(issueOf(projectA, new UUID(0, 4))));

        final IssueController controller = new IssueController();
        controller.principal = principal;
        controller.issueRepository = repository;

        final List<? extends IssueModel> page = controller.getTenancyIssues(cursor, 10);

        assertEquals(1, page.size());
        assertEquals(new UUID(0, 4), page.get(0).getId());
    }

    @Test
    void getTenancyIssues_noTenancies_returnsEmptyListWithoutQuerying() {
        final RemsfalPrincipal principal = mock(RemsfalPrincipal.class);
        when(principal.getTenancyProjects()).thenReturn(Map.of());

        final IssueController controller = new IssueController();
        controller.principal = principal;

        final List<? extends IssueModel> page = controller.getTenancyIssues(null, 10);

        assertTrue(page.isEmpty());
    }

    @Test
    void getProjectIssues_delegatesDirectlyToRepositoryForSinglePartition() {
        final UUID projectId = UUID.randomUUID();
        final UUID cursor = UUID.randomUUID();
        final IssueEntity expected = issueOf(projectId, UUID.randomUUID());

        final IssueRepository repository = mock(IssueRepository.class);
        when(repository.findByQuery(projectId, null, null, null, null, IssueStatus.OPEN, false, cursor, 10))
            .thenReturn(List.of(expected));

        final IssueController controller = new IssueController();
        controller.issueRepository = repository;

        final List<? extends IssueModel> result = controller.getProjectIssues(projectId, null, null, null, null,
            IssueStatus.OPEN, cursor, 10);

        assertEquals(List.of(expected), result);
    }

}
