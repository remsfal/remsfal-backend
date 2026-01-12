package de.remsfal.service.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public final class PlatformEcbArchitectureRules {

    private static final String BASE = "de.remsfal.service..";

    private static final String[] ALLOWED_FOR_BOUNDARY = {
            "de.remsfal.service.boundary..",
            "de.remsfal.service.control..",
            "de.remsfal.service.dto..",
            "de.remsfal.service.model..",
            "de.remsfal.service.entity..",

            "de.remsfal.core..",
            "de.remsfal.common..",

            "java..",
            "jakarta..",
            "org.jboss.logging..",
            "io.quarkus..",
            "org.eclipse.microprofile..",
            "io.smallrye..",

            "com.google.api.client..",
            "com.nimbusds.."
    };

    private static final String[] ALLOWED_FOR_CONTROL = {
            "de.remsfal.service.control..",
            "de.remsfal.service.control.event..",
            "de.remsfal.service.dto..",
            "de.remsfal.service.model..",
            "de.remsfal.service.entity..",

            "de.remsfal.core..",
            "de.remsfal.common..",

            "java..",
            "jakarta..",
            "org.jboss.logging..",
            "io.quarkus..",
            "org.eclipse.microprofile..",
            "io.smallrye..",

            "com.nimbusds.."
    };

    private PlatformEcbArchitectureRules() { }

    @ArchTest
    public static final ArchRule boundary_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.service.boundary..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_BOUNDARY)
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule control_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.service.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_CONTROL)
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule control_should_not_access_boundary =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.service.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage("de.remsfal.service.boundary..")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule entity_should_not_access_boundary_or_control =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.service.entity..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage(
                            "de.remsfal.service.control..",
                            "de.remsfal.service.boundary.."
                    )
                    .allowEmptyShould(true);
}
