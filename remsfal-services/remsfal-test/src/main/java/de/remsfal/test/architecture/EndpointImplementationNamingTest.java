package de.remsfal.test.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
@AnalyzeClasses(
        packages = "de.remsfal",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class EndpointImplementationNamingTest {


        /**
         * Alle Klassen, die ein Interface mit Namen *Endpoint implementieren,
         * sollen selbst auf 'Resource' enden.
         * Beispiel:
         *   TicketEndpoint  -> TicketResource
         */
        @ArchTest
        static final ArchRule implementations_of_Endpoint_should_end_with_Resource =
                classes()
                        .that().areTopLevelClasses()
                        .should(implementEndpointAndBeNamedResource());

        private static ArchCondition<JavaClass> implementEndpointAndBeNamedResource() {
            return new ArchCondition<>("implement an *Endpoint interface and be named *Resource") {
                @Override
                public void check(JavaClass clazz, ConditionEvents events) {
                    boolean implementsEndpoint = clazz.getAllRawInterfaces().stream()
                            .anyMatch(i -> i.getSimpleName().endsWith("Endpoint"));

                    // Wenn die Klasse kein *Endpoint implementiert -> egal
                    if (!implementsEndpoint) {
                        return;
                    }

                    if (!clazz.getSimpleName().endsWith("Resource")) {
                        String message = String.format(
                                "Class %s implements an *Endpoint interface (%s) but is not named *Resource",
                                clazz.getFullName(),
                                clazz.getAllRawInterfaces().stream()
                                        .filter(i -> i.getSimpleName().endsWith("Endpoint"))
                                        .map(JavaClass::getFullName)
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("unknown Endpoint")
                        );
                        events.add(SimpleConditionEvent.violated(clazz, message));
                    }
                }
            };
        }
    }
