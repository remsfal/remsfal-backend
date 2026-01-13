package de.remsfal.ticketing.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.nosql.Embeddable;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Layer-specific naming conventions for the Ticketing service.
 * These rules enforce consistent class naming based on architectural
 * responsibility (boundary, control, persistence).
 * Clear naming conventions make architectural roles explicit and
 * help to detect misplaced or misused classes early.
 */
public final class TicketingLayerNamingRules {

    /**
     * Utility class – not meant to be instantiated.
     */
    private TicketingLayerNamingRules() { }

    /**
     * Boundary layer naming rule.
     * Boundary components represent REST endpoints.
     * All top-level classes in the boundary package must therefore
     * end with {@code Resource}.
     */
    @ArchTest
    static final ArchRule boundary_classes_should_be_resources =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.boundary..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Resource")
                    .allowEmptyShould(true);

    /**
     * Control layer naming rules.
     * Control components encapsulate application logic or messaging behavior.
     * Their class names must clearly indicate their responsibility.
     */
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
     * Naming rules for persistence-related DTO classes.
     * Classes in {@code de.remsfal.ticketing.entity.dto..} are validated
     * using a whitelist-based approach.
     * Allowed types are:
     * - entity representations ({@code *Entity})
     * - composite keys ({@code *Key})
     * - metadata/helper structures ({@code *EntityMetaData}, {@code *FieldMetaData})
     * - embeddable value objects annotated with {@link Embeddable}
     * This avoids an ever-growing exception list while still allowing
     * well-defined, non-entity DTO structures.
     */
    @ArchTest
    static final ArchRule entity_dto_classes_should_follow_conventions =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.entity.dto..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Entity")
                    .orShould().haveSimpleNameEndingWith("Key")
                    .orShould().haveSimpleNameEndingWith("EntityMetaData")
                    .orShould().haveSimpleNameEndingWith("FieldMetaData")
                    .orShould().beAnnotatedWith(Embeddable.class)
                    .allowEmptyShould(true);

    /**
     * DAO / repository naming rule.
     * Classes responsible for data access must be clearly identifiable
     * by ending with {@code Repository}.
     */
    @ArchTest
    static final ArchRule dao_classes_should_end_with_repository =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.entity.dao..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Repository")
                    .allowEmptyShould(true);
}
