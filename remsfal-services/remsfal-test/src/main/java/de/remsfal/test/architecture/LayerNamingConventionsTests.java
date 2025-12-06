package de.remsfal.test.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import jakarta.ws.rs.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
        packages = "de.remsfal",
        importOptions = {
                com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class
        }
)
class LayerNamingConventionsTests {

    /**
     * Alle REST-Ressourcen im service- und notification-Modul heißen ...Resource.
     */
    @ArchTest
    static final ArchRule resources_should_be_named_Resource =
            classes()
                    .that().resideInAnyPackage(
                            "de.remsfal.service.boundary..",
                            "de.remsfal.notification.boundary.."
                    )
                    .and().areTopLevelClasses()
                    .and().areAnnotatedWith(Path.class)
                    .should().haveSimpleNameEndingWith("Resource");

    /**
     * Alle JPA-Entities im service-Modul liegen unter dto.superclass
     * und heißen ...Entity.
     */
    @ArchTest
    static final ArchRule entities_should_be_named_Entity =
            classes()
                    .that().resideInAnyPackage(
                            "de.remsfal.service.entity.dto.superclass.."
                    )
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Entity");

    /**
     * Alle Repositories im service-Modul liegen unter entity.dao
     * und heißen ...Repository.
     */
    @ArchTest
    static final ArchRule repositories_should_be_named_Repository =
            classes()
                    .that().resideInAnyPackage(
                            "de.remsfal.service.entity.dao.."
                    )
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Repository");
}
