package de.remsfal.core;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
        packages = "de.remsfal.core",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class CoreNameConventionsTest {

    @ArchTest
    static final ArchRule json_classes_should_end_with_Json =
            classes()
                    .that().resideInAnyPackage("de.remsfal.core.json..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Json");

    @ArchTest
    static final ArchRule api_interfaces_should_end_with_Endpoint =
            classes()
                    .that().resideInAnyPackage("de.remsfal.core.api..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Endpoint");

    @ArchTest
    static final ArchRule model_interfaces_should_end_with_Model =
            classes()
                    .that().resideInAnyPackage("de.remsfal.core.model..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Model");

    @ArchTest
    static final ArchRule model_package_should_only_contain_interfaces =
            classes()
                    .that().resideInAnyPackage("de.remsfal.core.model..")
                    .and().areTopLevelClasses()
                    .should().beInterfaces();
}

