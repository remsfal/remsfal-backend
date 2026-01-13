package de.remsfal.test.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Verifies that there are no cyclic dependencies between
 * top-level packages within the {@code de.remsfal} namespace.
 * Each first-level package below {@code de.remsfal} is treated as a slice.
 * Cyclic dependencies between these slices are not allowed.
 * This rule helps enforce a clean and maintainable module structure
 * by preventing mutual dependencies between major application modules.
 */
@AnalyzeClasses(
    packages = "de.remsfal",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public final class DependencyCycleTest {
    private DependencyCycleTest() {
        // utility class
    }

    /**
     * Ensures that no cyclic dependencies exist between
     * top-level slices below {@code de.remsfal}.
     * The slice definition {@code "de.remsfal.(*).."} creates slices such as:
     * <ul>
     *   <li>{@code de.remsfal.core..}</li>
     *   <li>{@code de.remsfal.service..}</li>
     *   <li>{@code de.remsfal.notification..}</li>
     *   <li>{@code de.remsfal.ticketing..}</li>
     *   <li>{@code de.remsfal.test..}</li>
     * </ul>
     * Any cyclic dependency between these slices will cause the test to fail.
     */
    @ArchTest
    static final ArchRule no_cycles_between_top_level_packages =
            slices()
                    .matching("de.remsfal.(*)..")
                    .should().beFreeOfCycles();


}
