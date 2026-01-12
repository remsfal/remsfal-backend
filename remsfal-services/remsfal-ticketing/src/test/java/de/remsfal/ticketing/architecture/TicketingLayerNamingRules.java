package de.remsfal.ticketing.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public final class TicketingLayerNamingRules {

    private TicketingLayerNamingRules() { }

    @ArchTest
    static final ArchRule boundary_classes_should_be_resources =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.boundary..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Resource")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule control_classes_should_be_controllers_or_messaging_components =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.control..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Controller")
                    .orShould().haveSimpleNameEndingWith("Producer")
                    .orShould().haveSimpleNameEndingWith("Consumer")
                    .allowEmptyShould(true);

    /**
     * entity.dto: allow Entity classes, Embeddables and Key classes.
     * (Whitelist based -> no growing exception list.)
     */
    @ArchTest
    static final ArchRule entity_dto_classes_should_follow_conventions =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.entity.dto..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Entity")
                    .orShould().haveSimpleNameEndingWith("Key")
                    .orShould().beAnnotatedWith(Embeddable.class)
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dao_classes_should_end_with_repository =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.entity.dao..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Repository")
                    .allowEmptyShould(true);
}
