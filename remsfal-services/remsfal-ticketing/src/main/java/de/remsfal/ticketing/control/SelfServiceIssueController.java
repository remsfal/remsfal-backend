package de.remsfal.ticketing.control;

import org.jboss.logging.Logger;

import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.AffectedContractorJson;
import de.remsfal.core.json.eventing.AffectedTenantJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.TenantJson;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;
import de.remsfal.ticketing.entity.dao.IssueRepository;
import de.remsfal.ticketing.entity.dto.IssueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class SelfServiceIssueController {

    private static final String SELF_SERVICE_ISSUE_TITLE = "Selbstständige Datensatzänderung";

    @Inject
    IssueRepository issueRepository;

    @Inject
    Logger logger;

    public void createIssueForAffectedTenant(final UUID reporterId, final UserJson reporter,
        final AffectedTenantJson affectedTenant) {
        final IssueEntity entity = new IssueEntity();
        entity.generateId();
        entity.setProjectId(affectedTenant.getProjectId());
        entity.setTitle(SELF_SERVICE_ISSUE_TITLE);
        entity.setType(IssueType.SELF_SERVICE);
        entity.setStatus(IssueStatus.PENDING);
        entity.setPriority(IssuePriority.UNCLASSIFIED);
        entity.setReporterId(reporterId);
        entity.setReportedBy(reporter.getName());
        entity.setVisibleToTenants(false);
        entity.setTenantUpdate(TenantJson.valueOf(reporter, affectedTenant.getTenantId()));
        issueRepository.insert(entity);
        logger.infov("Created self-service issue for affected tenant (tenantId={0}, projectId={1})",
            affectedTenant.getTenantId(), affectedTenant.getProjectId());
    }

    public void createIssueForAffectedContractor(final UUID changedByUserId, final String changedByName,
        final OrganizationJson organization, final AffectedContractorJson affectedContractor) {
        final IssueEntity entity = new IssueEntity();
        entity.generateId();
        entity.setProjectId(affectedContractor.getProjectId());
        entity.setTitle(SELF_SERVICE_ISSUE_TITLE);
        entity.setType(IssueType.SELF_SERVICE);
        entity.setStatus(IssueStatus.PENDING);
        entity.setPriority(IssuePriority.UNCLASSIFIED);
        entity.setReporterId(changedByUserId);
        entity.setReportedBy(changedByName);
        entity.setVisibleToTenants(false);
        entity.setContractorUpdate(ContractorJson.valueOf(organization, affectedContractor.getContractorId()));
        issueRepository.insert(entity);
        logger.infov("Created self-service issue for affected contractor (contractorId={0}, projectId={1})",
            affectedContractor.getContractorId(), affectedContractor.getProjectId());
    }

}
