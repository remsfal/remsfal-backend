package de.remsfal.notification.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Defines ArchUnit rules that restrict the usage of JAX-RS annotations
 * within the notification module.
 * *
 * These rules ensure that REST-related annotations are used only
 * in designated boundary or API layers and do not leak into
 * control or domain code.
 */
public final class NotificationAnnotationRules {

    /**
     * Base package for the notification module.
     */
    private static final String NOTIFICATION_BASE = "de.remsfal.notification..";

    private NotificationAnnotationRules() {
    }

    /**
     * Ensures that {@link Path} annotations are used only in
     * the notification boundary layer or in the central core API.
     * *
     * This rule is applied globally across all packages and prevents
     * REST endpoints from being declared outside the intended layers.
     */
    @ArchTest
    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY_OR_CORE_API =
            classes()
                    .that().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.notification.boundary..",
                            "de.remsfal.core.api.."
                    )
                    .allowEmptyShould(true);


    /**
     * Ensures that JAX-RS HTTP method annotations are used only
     * within the notification boundary layer.
     * *
     * This rule applies exclusively to the notification module and
     * intentionally does not validate usage inside {@code core.api}.
     */
    @ArchTest
    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY =
            methods()
                    .that().areDeclaredInClassesThat().resideInAnyPackage(NOTIFICATION_BASE)
                    .and().areAnnotatedWith(GET.class)
                    .or().areDeclaredInClassesThat().resideInAnyPackage(NOTIFICATION_BASE)
                    .and().areAnnotatedWith(POST.class)
                    .or().areDeclaredInClassesThat().resideInAnyPackage(NOTIFICATION_BASE)
                    .and().areAnnotatedWith(PUT.class)
                    .or().areDeclaredInClassesThat().resideInAnyPackage(NOTIFICATION_BASE)
                    .and().areAnnotatedWith(DELETE.class)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(
                            "de.remsfal.notification.boundary.."
                    )
                    .allowEmptyShould(true);
}
