package de.remsfal.ticketing.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.eventing.AffectedTenantJson;
import de.remsfal.core.json.eventing.ImmutableAffectedContractorJson;
import de.remsfal.core.json.eventing.ImmutableAffectedTenantJson;
import de.remsfal.core.json.organization.ImmutableOrganizationJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import de.remsfal.ticketing.entity.filter.IssueFilter;

class SelfServiceIssueControllerTest {

    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID CONTRACTOR_ID = UUID.randomUUID();
    private static final UUID REPORTER_ID = UUID.randomUUID();

    private SelfServiceIssueController controller;
    private IssueRepository issueRepository;

    @BeforeEach
    void setUp() {
        issueRepository = mock(IssueRepository.class);
        when(issueRepository.findByQuery(any(), any(), any())).thenReturn(List.of());
        controller = new SelfServiceIssueController();
        controller.issueRepository = issueRepository;
        controller.logger = Logger.getLogger(SelfServiceIssueController.class);
    }

    private static UserJson user(final String firstName, final String mobilePhoneNumber) {
        return ImmutableUserJson.builder()
            .firstName(firstName)
            .lastName("Mustermann")
            .email("max@example.com")
            .mobilePhoneNumber(mobilePhoneNumber)
            .build();
    }

    private static AffectedTenantJson affectedTenant() {
        return ImmutableAffectedTenantJson.builder().tenantId(TENANT_ID).projectId(PROJECT_ID).build();
    }

    private static OrganizationJson organization(final String name) {
        return ImmutableOrganizationJson.builder().name(name).email("info@example.com").trade("Sanitär").build();
    }

    private static AffectedContractorJson affectedContractor() {
        return ImmutableAffectedContractorJson.builder().contractorId(CONTRACTOR_ID).projectId(PROJECT_ID).build();
    }

    private static IssueEntity openIssue(final IssueStatus status) {
        final IssueEntity entity = new IssueEntity();
        entity.generateId();
        entity.setProjectId(PROJECT_ID);
        entity.setTitle("Selbstständige Datensatzänderung");
        entity.setType(IssueType.SELF_SERVICE);
        entity.setStatus(status);
        entity.setReporterId(UUID.randomUUID());
        entity.setReportedBy("Old Reporter");
        return entity;
    }

    @Test
    void createIssueForAffectedTenant_createsPendingIssueWhenNoneIsOpen() {
        controller.createIssueForAffectedTenant(REPORTER_ID, user("Max", "+491701234567"), affectedTenant());

        final ArgumentCaptor<IssueEntity> captor = ArgumentCaptor.forClass(IssueEntity.class);
        verify(issueRepository).insert(captor.capture());
        verify(issueRepository, never()).update(any());
        final IssueEntity created = captor.getValue();
        assertEquals(PROJECT_ID, created.getProjectId());
        assertEquals(IssueType.SELF_SERVICE, created.getType());
        assertEquals(IssueStatus.PENDING, created.getStatus());
        assertEquals(REPORTER_ID, created.getReporterId());
        assertEquals(TENANT_ID, created.getTenantUpdate().getId());
        assertEquals("+491701234567", created.getTenantUpdate().getMobilePhoneNumber());
    }

    @Test
    void createIssueForAffectedTenant_looksForOpenSelfServiceIssuesOfTheProject() {
        controller.createIssueForAffectedTenant(REPORTER_ID, user("Max", null), affectedTenant());

        final ArgumentCaptor<IssueFilter> captor = ArgumentCaptor.forClass(IssueFilter.class);
        verify(issueRepository).findByQuery(captor.capture(), isNull(),
            eq(SelfServiceIssueController.OPEN_ISSUE_LOOKUP_LIMIT));
        final IssueFilter filter = captor.getValue();
        assertEquals(PROJECT_ID, filter.projectId());
        assertEquals(List.of(IssueType.SELF_SERVICE), filter.type());
        assertEquals(List.of(IssueStatus.PENDING, IssueStatus.OPEN, IssueStatus.IN_PROGRESS), filter.status());
    }

    @Test
    void createIssueForAffectedTenant_mergesIntoOpenIssueOfTheSameTenant() {
        final IssueEntity existing = openIssue(IssueStatus.OPEN);
        existing.setTenantUpdate(TenantJson.valueOf(user("Maxi", "+491700000000"), TENANT_ID));
        when(issueRepository.findByQuery(any(), any(), any())).thenReturn(List.of(existing));

        controller.createIssueForAffectedTenant(REPORTER_ID, user("Max", "+491701234567"), affectedTenant());

        verify(issueRepository, never()).insert(any());
        final ArgumentCaptor<IssueEntity> captor = ArgumentCaptor.forClass(IssueEntity.class);
        verify(issueRepository).update(captor.capture());
        final IssueEntity merged = captor.getValue();
        assertSame(existing, merged);
        assertEquals(IssueStatus.OPEN, merged.getStatus());
        assertEquals(REPORTER_ID, merged.getReporterId());
        assertEquals("Max Mustermann", merged.getReportedBy());
        assertEquals("Max", merged.getTenantUpdate().getFirstName());
        assertEquals("+491701234567", merged.getTenantUpdate().getMobilePhoneNumber());
        assertEquals(TENANT_ID, merged.getTenantUpdate().getId());
    }

    @Test
    void createIssueForAffectedTenant_ignoresOpenIssuesOfOtherTenantsAndContractors() {
        final IssueEntity otherTenant = openIssue(IssueStatus.PENDING);
        otherTenant.setTenantUpdate(TenantJson.valueOf(user("Erika", null), UUID.randomUUID()));
        final IssueEntity contractorIssue = openIssue(IssueStatus.PENDING);
        contractorIssue.setContractorUpdate(ContractorJson.valueOf(organization("Musterbau"), CONTRACTOR_ID));
        when(issueRepository.findByQuery(any(), any(), any())).thenReturn(List.of(otherTenant, contractorIssue));

        controller.createIssueForAffectedTenant(REPORTER_ID, user("Max", null), affectedTenant());

        final ArgumentCaptor<IssueEntity> captor = ArgumentCaptor.forClass(IssueEntity.class);
        verify(issueRepository).insert(captor.capture());
        verify(issueRepository, never()).update(any());
        assertNotEquals(otherTenant.getId(), captor.getValue().getId());
        assertEquals(TENANT_ID, captor.getValue().getTenantUpdate().getId());
    }

    @Test
    void createIssueForAffectedContractor_createsPendingIssueWhenNoneIsOpen() {
        controller.createIssueForAffectedContractor(REPORTER_ID, "Max Mustermann", organization("Musterbau GmbH"),
            affectedContractor());

        final ArgumentCaptor<IssueEntity> captor = ArgumentCaptor.forClass(IssueEntity.class);
        verify(issueRepository).insert(captor.capture());
        verify(issueRepository, never()).update(any());
        final IssueEntity created = captor.getValue();
        assertEquals(IssueStatus.PENDING, created.getStatus());
        assertEquals("Max Mustermann", created.getReportedBy());
        assertEquals(CONTRACTOR_ID, created.getContractorUpdate().getId());
        assertEquals("Musterbau GmbH", created.getContractorUpdate().getName());
    }

    @Test
    void createIssueForAffectedContractor_mergesIntoOpenIssueOfTheSameContractor() {
        final IssueEntity existing = openIssue(IssueStatus.IN_PROGRESS);
        existing.setContractorUpdate(ContractorJson.valueOf(organization("Musterbau"), CONTRACTOR_ID));
        when(issueRepository.findByQuery(any(), any(), any())).thenReturn(List.of(existing));

        controller.createIssueForAffectedContractor(REPORTER_ID, "Max Mustermann", organization("Musterbau GmbH"),
            affectedContractor());

        verify(issueRepository, never()).insert(any());
        final ArgumentCaptor<IssueEntity> captor = ArgumentCaptor.forClass(IssueEntity.class);
        verify(issueRepository).update(captor.capture());
        final IssueEntity merged = captor.getValue();
        assertSame(existing, merged);
        assertEquals(IssueStatus.IN_PROGRESS, merged.getStatus());
        assertEquals(REPORTER_ID, merged.getReporterId());
        assertEquals("Max Mustermann", merged.getReportedBy());
        assertEquals("Musterbau GmbH", merged.getContractorUpdate().getName());
    }

    @Test
    void createIssueForAffectedContractor_ignoresOpenIssuesOfOtherContractors() {
        final IssueEntity otherContractor = openIssue(IssueStatus.PENDING);
        otherContractor.setContractorUpdate(ContractorJson.valueOf(organization("Andere"), UUID.randomUUID()));
        when(issueRepository.findByQuery(any(), any(), any())).thenReturn(List.of(otherContractor));

        controller.createIssueForAffectedContractor(REPORTER_ID, "Max Mustermann", organization("Musterbau GmbH"),
            affectedContractor());

        verify(issueRepository).insert(any());
        verify(issueRepository, never()).update(any());
    }

}
