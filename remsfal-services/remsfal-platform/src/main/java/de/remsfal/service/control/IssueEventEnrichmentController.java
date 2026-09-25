package de.remsfal.service.control;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.project.RentalUnitNodeDataJson;
import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.service.entity.dao.ApartmentRepository;
import de.remsfal.service.entity.dao.BuildingRepository;
import de.remsfal.service.entity.dao.CommercialRepository;
import de.remsfal.service.entity.dao.ContractorRepository;
import de.remsfal.service.entity.dao.UserRepository;
import de.remsfal.service.entity.dao.ProjectRepository;
import de.remsfal.service.entity.dao.PropertyRepository;
import de.remsfal.service.entity.dao.RentalAgreementRepository;
import de.remsfal.service.entity.dao.SiteRepository;
import de.remsfal.service.entity.dao.StorageRepository;
import de.remsfal.service.entity.dto.AddressEntity;
import de.remsfal.service.entity.dto.ApartmentEntity;
import de.remsfal.service.entity.dto.BuildingEntity;
import de.remsfal.service.entity.dto.CommercialEntity;
import de.remsfal.service.entity.dto.SiteEntity;
import de.remsfal.service.entity.dto.StorageEntity;
import de.remsfal.service.entity.dto.UserEntity;
import de.remsfal.service.entity.dto.superclass.RentalUnitEntity;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IssueEventEnrichmentController {

    @Inject
    @ConfigProperty(name = "de.remsfal.frontend.url.base")
    String frontendBaseUrl;

    @Inject
    Logger logger;

    @Inject
    UserRepository userRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ContractorRepository contractorRepository;

    @Inject
    RentalAgreementRepository rentalAgreementRepository;

    @Inject
    PropertyRepository propertyRepository;

    @Inject
    SiteRepository siteRepository;

    @Inject
    BuildingRepository buildingRepository;

    @Inject
    ApartmentRepository apartmentRepository;

    @Inject
    StorageRepository storageRepository;

    @Inject
    CommercialRepository commercialRepository;

    @Transactional
    public IssueEventJson enrich(final IssueEventJson event) {
        ProjectJson project = enrichProject(event);
        RentalAgreementJson rentalAgreement = enrichRentalAgreement(event);
        RentalUnitEntity rentalUnit = findRentalUnit(event.getIssue());
        IssueEventJson enrichedEvent = ImmutableIssueEventJson.builder()
            .issueEventType(event.getIssueEventType())
            .issueId(event.getIssueId())
            .issue(event.getIssue())
            .project(project)
            .rentalAgreement(rentalAgreement)
            .rentalUnit(rentalUnit != null ? RentalUnitNodeDataJson.valueOf(rentalUnit) : event.getRentalUnit())
            .placeOfPerformance(rentalUnit != null ? findPlaceOfPerformance(rentalUnit)
                : event.getPlaceOfPerformance())
            .link(buildIssueLink(event))
            .principal(enrichUser(event.getPrincipal()))
            .assignee(enrichUser(event.getAssignee()))
            .reporter(enrichUser(event.getReporter()))
            .initiator(enrichUser(event.getInitiator()))
            .contractor(enrichContractor(event.getContractor()))
            .confirmor(enrichUser(event.getConfirmor()))
            .offerer(enrichUser(event.getOfferer()))
            .chatMessage(event.getChatMessage())
            .timelineEntry(event.getTimelineEntry())
            .quotationRequest(event.getQuotationRequest())
            .quotation(event.getQuotation())
            .orderPlacement(event.getOrderPlacement())
            .build();
        logger.infov("Enriched issue event (issueId={0}, projectId={1})", event.getIssueId(),
            event.getIssue() != null ? event.getIssue().getProjectId() : null);
        return enrichedEvent;
    }

    private ProjectJson enrichProject(final IssueEventJson event) {
        if (event == null) {
            return null;
        }
        final UUID projectId = event.getIssue() != null ? event.getIssue().getProjectId() : null;
        if (projectId == null) {
            return event.getProject();
        }
        return projectRepository.findByIdOptional(projectId)
            .map(ProjectJson::valueOf)
            .orElse(event.getProject());
    }

    private RentalAgreementJson enrichRentalAgreement(final IssueEventJson event) {
        if (event == null) {
            return null;
        }
        final UUID agreementId = event.getIssue() != null ? event.getIssue().getAgreementId() : null;
        if (agreementId == null) {
            return event.getRentalAgreement();
        }
        return rentalAgreementRepository.findByIdOptional(agreementId)
            .map(RentalAgreementJson::valueOf)
            .orElse(event.getRentalAgreement());
    }

    private RentalUnitEntity findRentalUnit(final IssueJson issue) {
        if (issue == null || issue.getRentalUnitId() == null || issue.getRentalUnitType() == null) {
            return null;
        }
        final UUID unitId = issue.getRentalUnitId();
        final Optional<? extends RentalUnitEntity> unit = switch (issue.getRentalUnitType()) {
            case PROPERTY -> propertyRepository.findByIdOptional(unitId);
            case SITE -> siteRepository.findByIdOptional(unitId);
            case BUILDING -> buildingRepository.findByIdOptional(unitId);
            case APARTMENT -> apartmentRepository.findByIdOptional(unitId);
            case STORAGE -> storageRepository.findByIdOptional(unitId);
            case COMMERCIAL -> commercialRepository.findByIdOptional(unitId);
        };
        return unit.orElse(null);
    }

    private AddressJson findPlaceOfPerformance(final RentalUnitEntity unit) {
        final AddressEntity address;
        if (unit instanceof SiteEntity site) {
            address = site.getAddress();
        } else if (unit instanceof BuildingEntity building) {
            address = building.getAddress();
        } else if (unit instanceof ApartmentEntity apartment) {
            address = findBuildingAddress(apartment.getBuildingId());
        } else if (unit instanceof StorageEntity storage) {
            address = findBuildingAddress(storage.getBuildingId());
        } else if (unit instanceof CommercialEntity commercial) {
            address = findBuildingAddress(commercial.getBuildingId());
        } else {
            address = null;
        }
        return address != null ? AddressJson.valueOf(address) : null;
    }

    private AddressEntity findBuildingAddress(final UUID buildingId) {
        if (buildingId == null) {
            return null;
        }
        return buildingRepository.findByIdOptional(buildingId)
            .map(BuildingEntity::getAddress)
            .orElse(null);
    }

    private UserJson enrichUser(final UserJson user) {
        if (user == null || user.getId() == null) {
            return user;
        }
        Optional<UserEntity> entity = userRepository.findByIdOptional(user.getId());
        if (entity.isEmpty()) {
            return user;
        }
        UserEntity found = entity.get();
        return ImmutableUserJson.builder()
            .id(found.getId())
            .email(found.getEmail())
            .firstName(found.getFirstName())
            .lastName(found.getLastName())
            .build();
    }

    private ContractorJson enrichContractor(final ContractorJson contractor) {
        if (contractor == null || contractor.getId() == null) {
            return contractor;
        }
        return contractorRepository.findByIdOptional(contractor.getId())
            .map(ContractorJson::valueOf)
            .orElse(contractor);
    }

    String buildIssueLink(final IssueEventJson event) {
        final UUID projectId = event != null && event.getIssue() != null ? event.getIssue().getProjectId() : null;
        if (event == null || event.getIssueId() == null || projectId == null) {
            return frontendBaseUrl;
        }
        return frontendBaseUrl + "/projects/" + projectId + "/issueedit/" + event.getIssueId();
    }
}
