package de.remsfal.service;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;

public class ECBArchitectureTest {

    private static final String BASE_PACKAGE = "de.remsfal.service";

    @Test
    void boundary_should_only_access_control() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);

        ArchRule rule = classes()
                .that().resideInAPackage("..boundary..")
                .should().onlyAccessClassesThat()
                .resideInAnyPackage(
                        "..boundary..",
                        "..control..",
                        "..dto..",
                        "..model..",
                        "de.remsfal.core..",
                        "de.remsfal.common.authentication..",
                        "java..",                // Standard Java-Pakete
                        "jakarta..",             // Jakarta EE
                        "org.jboss.logging..",   // Logging Framework
                        "io.quarkus..",          // Quarkus Framework
                        "io.opentelemetry..",   // OpenTelemetry
                        "org.eclipse.microprofile..",
                        "com.google.api.client..",
                        "com.nimbusds.jose..",
                        "io.smallrye.jwt.auth.principal.."
                );

        rule.check(importedClasses);
    }

    @Test
    void control_should_only_access_entity() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);

        ArchRule rule = classes()
                .that().resideInAPackage("..control..")
                .should().onlyAccessClassesThat()
                .resideInAnyPackage(
                        "..entity..", // darf Entities nutzen
                        "..control..",
                        "..control.event..",    // falls Events dazugehören
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
                        "io.opentelemetry..",
                        "org.eclipse.microprofile..",
                        "io.smallrye.jwt.auth.principal.."
                );


        rule.check(importedClasses);
    }

    @Test
    void entity_should_not_access_boundary_or_control() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);

        ArchRule rule = noClasses()
                .that().resideInAPackage("..entity..")
                .should().accessClassesThat()
                .resideInAnyPackage(
                        "..control..",
                        "..boundary.."
                );

        rule.check(importedClasses);
    }
}
