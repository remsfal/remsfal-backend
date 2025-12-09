package de.remsfal.test.ticketing.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Defines layer-specific naming conventions for the ticketing module.
 * *
 * These rules ensure that classes follow consistent naming patterns
 * based on their architectural layer, making responsibilities and
 * technical roles immediately visible.
 */
public final class TicketingLayerNamingRules {

    private TicketingLayerNamingRules() {
    }

    /**
     * Boundary layer classes represent REST APIs.
     * *
     * All top-level classes in the ticketing boundary package must
     * have a simple class name ending with {@code Resource}.
     */
    @ArchTest
    static final ArchRule boundary_classes_should_be_resources =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.boundary..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Resource");

    /**
     * Control layer classes encapsulate application and messaging logic.
     * *
     * All top-level classes in the ticketing control package must
     * end with {@code Controller}, {@code Producer}, or {@code Consumer}.
     */
    @ArchTest
    static final ArchRule control_classes_should_be_controllers_or_messaging_components =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.control..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Controller")
                    .orShould().haveSimpleNameEndingWith("Producer")
                    .orShould().haveSimpleNameEndingWith("Consumer");

    /**
     * DTO classes used in the persistence layer.
     * *
     * All top-level classes in {@code de.remsfal.ticketing.entity.dto..}
     * must have a simple class name ending with {@code Entity}.
     */
    @ArchTest
    static final ArchRule dto_classes_should_end_with_entity =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.entity.dto..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Entity");

    /**
     * DAO / repository classes responsible for data access.
     * *
     * All top-level classes in {@code de.remsfal.ticketing.entity.dao..}
     * must have a simple class name ending with {@code Repository}.
     */
    @ArchTest
    static final ArchRule dao_classes_should_end_with_repository =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.entity.dao..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Repository");
}

