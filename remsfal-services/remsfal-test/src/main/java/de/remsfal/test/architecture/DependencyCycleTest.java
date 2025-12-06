package de.remsfal.test.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "de.remsfal",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class DependencyCycleTest {

    /**
     * Keine Zyklen zwischen den "Top-Level"-Slices unter de.remsfal..
     *
     * Pattern "de.remsfal.(*).." erzeugt z.B. Slices:
     *   de.remsfal.core..
     *   de.remsfal.service..
     *   de.remsfal.notification..
     *   de.remsfal.ticketing..
     *   de.remsfal.test..
     */
    @ArchTest
    static final ArchRule no_cycles_between_top_level_packages =
            slices()
                    .matching("de.remsfal.(*)..")
                    .should().beFreeOfCycles();
}
