package de.remsfal.notification.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.ws.rs.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public final class NotificationLayerNamingRules {

    private NotificationLayerNamingRules() { }

    @ArchTest
    static final ArchRule boundary_classes_should_be_resources_or_consumers =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Resource")
                    .orShould().haveSimpleNameEndingWith("Consumer")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule boundary_path_annotated_should_end_with_resource =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .and().areTopLevelClasses()
                    .and().areAnnotatedWith(Path.class)
                    .should().haveSimpleNameEndingWith("Resource")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule control_classes_should_be_controllers_or_messaging_components =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.control..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Controller")
                    .orShould().haveSimpleNameEndingWith("Producer")
                    .orShould().haveSimpleNameEndingWith("Consumer")
                    .allowEmptyShould(true);

    /**
     * DTO classes used in persistence layer.
     * Requirement: Java classes in de.remsfal.*.dto end with Entity (typo in requirement kept: Enitity -> Entity).
     */
    @ArchTest
    static final ArchRule entity_dto_classes_should_follow_conventions =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.entity.dto..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Entity")
                    .orShould().haveSimpleNameEndingWith("Key")
                    .orShould().beAnnotatedWith(Embeddable.class)
                    .allowEmptyShould(true);

    /**
     * DAO/repository classes.
     */
    @ArchTest
    static final ArchRule dao_classes_should_end_with_repository =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.entity.dao..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Repository")
                    .allowEmptyShould(true);
}
