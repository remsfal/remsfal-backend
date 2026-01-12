package de.remsfal.core;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Enforces naming and structural conventions for the core module.
 * *
 * The core module represents stable, implementation-independent
 * contracts and models that are shared across multiple modules.
 * *
 * These rules ensure that naming conventions and package responsibilities
 * are consistently applied.
 */
@AnalyzeClasses(
        packages = "de.remsfal.core",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class CoreNameConventionsTest {

    /**
     * Ensures that JSON representation classes are named consistently.
     * *
     * All top-level classes located in {@code de.remsfal.core.json..}
     * must have a simple class name ending with {@code Json}.
     */
    @ArchTest
    static final ArchRule json_classes_should_end_with_Json =
            classes()
                    .that().resideInAnyPackage("de.remsfal.core.json..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Json")
                    .allowEmptyShould(true);;

    /**
     * Ensures that API contract interfaces are named consistently.
     * *
     * All interfaces located in {@code de.remsfal.core.api..}
     * must have a simple name ending with {@code Endpoint}.
     */
    @ArchTest
    static final ArchRule api_interfaces_should_end_with_Endpoint =
            classes()
                    .that().resideInAnyPackage("de.remsfal.core.api..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Endpoint")
                    .allowEmptyShould(true);

    /**
     * Core API must be contracts only: no classes/implementations in core.api.
     */
    @ArchTest
    static final ArchRule core_api_should_only_contain_interfaces =
            classes()
                    .that().resideInAnyPackage("de.remsfal.core.api..")
                    .and().areTopLevelClasses()
                    .should().beInterfaces()
                    .allowEmptyShould(true);

    /**
     * Ensures that model interfaces follow a consistent naming convention.
     * *
     * All interfaces located in {@code de.remsfal.core.model..}
     * must have a simple name ending with {@code Model}.
     */
    @ArchTest
    static final ArchRule model_interfaces_should_end_with_Model =
            classes()
                    .that().resideInAnyPackage("de.remsfal.core.model..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Model")
                    .allowEmptyShould(true);;

    /**
     * Ensures that the core model package contains only interfaces.
     * *
     * This prevents concrete implementations from leaking into
     * the core module and keeps the model purely abstract.
     */
    @ArchTest
    static final ArchRule model_package_should_only_contain_interfaces =
            classes()
                    .that().resideInAnyPackage("de.remsfal.core.model..")
                    .and().areTopLevelClasses()
                    .should().beInterfaces()
                    .allowEmptyShould(true);
}

