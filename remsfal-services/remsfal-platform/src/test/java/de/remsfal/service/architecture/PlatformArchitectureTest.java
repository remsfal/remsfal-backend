package de.remsfal.service.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * Aggregates ArchUnit rules that enforce architecture constraints
 * for the platform/service module.
 * *
 * This test class imports rule sets for annotation usage and
 * layer naming conventions specific to the platform service.
 * *
 * Only production classes located under {@code de.remsfal.service} are analyzed.
 * Test classes are explicitly excluded from the analysis.
 */
@AnalyzeClasses(
        packages = "de.remsfal.service",
        importOptions = {
                ImportOption.DoNotIncludeTests.class
        }
)
class PlatformArchitectureTest {

    /**
     * Executes annotation architecture rules for the platform module.
     */
    @ArchTest
    static final ArchTests ANNOTATION_RULES =
            ArchTests.in(PlatformAnnotationRules.class);

    /**
     * Executes layer naming convention rules for the platform module.
     */
    @ArchTest
    static final ArchTests NAMING_RULES =
            ArchTests.in(PlatformLayerNamingRules.class);
}
