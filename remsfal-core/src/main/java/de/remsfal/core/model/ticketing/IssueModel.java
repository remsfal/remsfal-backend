package de.remsfal.core.model.ticketing;

import java.util.Set;
import java.util.UUID;

import de.remsfal.core.model.RentalUnitModel.UnitType;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueModel {

    UUID getId();

    UUID getProjectId();

    String getTitle();

    public enum IssueType {
        APPLICATION,
        DEFECT,
        INQUIRY,
        MAINTENANCE,
        TASK,
        TERMINATION
    }

    IssueType getType();

    public enum IssueCategory {
        // Defect categories
        BLOCKED_DRAIN, // Verstopfte Abflüsse
        ELECTRICAL_FAULT, // Elektrischer Defekt
        FIRE_DAMAGE, // Brandschaden
        HEATING_SYSTEM_MALFUNCTION, // Störung der Heizungsanlage
        PEST_INFESTATION, // Schädlingsbefall
        POLLUTION_INSIDE_BUILDING, // Verschmutzungen im Gebäude
        POLLUTION_OUTSIDE_BUILDING, // Verschmutzungen außerhalb des Gebäudes
        SANITARY_SYSTEM_DAMAGE, // Beschädigung der sanitären Anlagen
        ROLLER_SHUTTER_DAMAGE, // Beschädigte Rollläden
        WATER_DAMAGE, // Wasserschaden
        // INQUIRY categories
        CERTIFICATE_OF_NO_RENT_ARREARS, // Mietschuldenfreiheitsbescheinigung
        CONFIRMATION_OF_RESIDENCE, // Wohnungsgeberbestätigung
        // MAINTENANCE categories
        ALARM_SYSTEM_MAINTENANCE, // Wartung der Alarmanlage
        CHIMNEY_SWEEP_MAINTENANCE, // Schornsteinfeger
        CLEANING_MAINTENANCE, // Reinigungsdienst
        FIRE_ALARM_MAINTENANCE, // Wartung der Brandmeldeanlage
        FIRE_EXTINGUISHER_MAINTENANCE, // Wartung der Feuerlöscher
        GARDEN_MAINTENANCE, // Gartenpflege
        HEATING_MAINTENANCE, // Heizungswartung
        PUMP_MAINTENANCE, // Pumpen-/Hebeanlagenwartung
        SNOW_REMOVAL_MAINTENANCE, // Winterdienst
        TREE_CARE_MAINTENANCE, // Baumpflege
        // other categories
        GENERAL // Allgemeine Anfragen, Aufgaben oder Probleme
    }

    IssueCategory getCategory();

    public enum IssueStatus {
        PENDING,
        OPEN,
        IN_PROGRESS,
        CLOSED,
        REJECTED
    }

    IssueStatus getStatus();

    enum IssuePriority {
        URGENT,
        HIGH,
        MEDIUM,
        LOW,
        UNCLASSIFIED
    }

    IssuePriority getPriority();

    UUID getReporterId();

    UUID getAgreementId();

    Boolean isVisibleToTenants();

    UUID getRentalUnitId();

    UnitType getRentalUnitType();

    UUID getAssigneeId();

    String getLocation();

    String getDescription();

    UUID getParentIssue();

    Set<UUID> getChildrenIssues();

    Set<UUID> getRelatedTo();

    Set<UUID> getDuplicateOf();

    Set<UUID> getBlockedBy();

    Set<UUID> getBlocks();

}
