package de.remsfal.service.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.ws.rs.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public final class PlatformLayerNamingRules {

    private PlatformLayerNamingRules() { }

    @ArchTest
    static final ArchRule boundary_path_annotated_should_end_with_resource =
            classes()
                    .that().resideInAnyPackage("de.remsfal.service.boundary..")
                    .and().areTopLevelClasses()
                    .and().areAnnotatedWith(Path.class)
                    .should().haveSimpleNameEndingWith("Resource")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule control_classes_should_be_controllers_or_messaging_components =
            classes()
                    .that().resideInAnyPackage("de.remsfal.service.control",
                            "de.remsfal.service.control.producer.."
                    )
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Controller")
                    .orShould().haveSimpleNameEndingWith("Producer")
                    .orShould().haveSimpleNameEndingWith("Consumer")
                    .orShould().haveSimpleNameEndingWith("Enricher")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule control_events_should_end_with_event =
            classes()
                    .that().resideInAnyPackage("de.remsfal.service.control.event..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Event")
                    .allowEmptyShould(true);

    /**
     * entity.dto: allow Entity classes, Embeddables and Key classes.
     */
    @ArchTest
    static final ArchRule entity_dto_classes_should_follow_conventions =
            classes()
                    .that().resideInAnyPackage("de.remsfal.service.entity.dto..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Entity")
                    .orShould().haveSimpleNameEndingWith("Key")
                    .orShould().beAnnotatedWith(Embeddable.class)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dao_classes_should_end_with_repository =
            classes()
                    .that().resideInAnyPackage("de.remsfal.service.entity.dao..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Repository")
                    .allowEmptyShould(true);
}
