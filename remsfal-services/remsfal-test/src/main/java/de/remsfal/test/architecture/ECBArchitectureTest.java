package de.remsfal.test.architecture;


import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Verifies compliance with the Entity-Control-Boundary (ECB) architecture
 * within the {@code de.remsfal.service} module.
 * *
 * The rules defined in this class ensure a strict separation of concerns
 * between boundary, control and entity layers.
 * *
 * Only production classes are analyzed. Test classes are excluded.
 */
@AnalyzeClasses(
        packages = "de.remsfal.service",
        importOptions = {
                com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class
        }
)
class ECBArchitectureTest {

    /**
     * Boundary classes may only access lower layers and explicitly allowed
     * libraries and framework packages.
     * *
     * Boundaries act as entry points (e.g. REST endpoints) and must not
     * contain business logic or access higher-level components.
     */

    @ArchTest
    static final ArchRule boundary_should_only_access_allowed_layers =
            classes()
                    .that().resideInAPackage("..boundary..")
                    .should().onlyAccessClassesThat()
                    .resideInAnyPackage(
                            "..boundary..",
                            "..control..",
                            "..dto..",
                            "..model..",
                            "de.remsfal.core..",
                            "de.remsfal.common.authentication..",
                            "java..",
                            "jakarta..",
                            "org.jboss.logging..",
                            "io.quarkus..",
                            "org.eclipse.microprofile..",
                            "com.google.api.client..",
                            "com.nimbusds.jose..",
                            "io.smallrye.jwt.auth.principal.."
                    );

    /**
     * Control classes may access entities, DTOs, models and infrastructure,
     * but must not depend on boundary classes.
     * *
     * Controls encapsulate application logic and coordinate use cases.
     */
    @ArchTest
    static final ArchRule control_should_only_access_allowed_layers =
            classes()
                    .that().resideInAPackage("..control..")
                    .should().onlyAccessClassesThat()
                    .resideInAnyPackage(
                            "..entity..",
                            "..control..",
                            "..control.event..",
                            "..dto..",
                            "..model..",
                            "de.remsfal.core..",
                            "de.remsfal.service.entity.dto..",
                            "de.remsfal.service.boundary.exception..",
                            "de.remsfal.common.authentication..",
                            "java..",
                            "jakarta..",
                            "org.jboss.logging..",
                            "io.quarkus..",
                            "org.eclipse.microprofile..",
                            "io.smallrye.jwt.auth.principal.."
                    );


    /**
     * Entity classes must not depend on boundary or control layers.
     * *
     * Entities represent the core domain model and must remain
     * independent of application and delivery concerns.
     */
    @ArchTest
    static final ArchRule entity_should_not_access_boundary_or_control =
            noClasses()
                    .that().resideInAPackage("..entity..")
                    .should().accessClassesThat()
                    .resideInAnyPackage(
                            "..control..",
                            "..boundary.."
                    );
}
