package de.remsfal.notification.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.ws.rs.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * ArchUnit rules defining annotation usage constraints for the Notification service.
 * These rules enforce a clean separation of concerns by ensuring that
 * framework-specific annotations (JPA, JAX-RS) are only used in their
 * designated architectural layers.
 */
public final class NotificationAnnotationRules {

    /**
     * Base package of the Notification service.
     */
    private static final String NOTIFICATION_BASE = "de.remsfal.notification..";

    /**
     * Packages in which JAX-RS annotations are allowed.
     * - boundary: REST endpoints and transport-layer concerns
     * - core.api: shared API contracts used across services
     */
    private static final String[] ALLOWED_JAXRS_PACKAGES = {
            "de.remsfal.notification.boundary..",
            "de.remsfal.core.api.."
    };

    /**
     * Utility class – not meant to be instantiated.
     */
    private NotificationAnnotationRules() { }

    /**
     * Ensures that JPA annotations are only used in entity packages.
     * Classes annotated with {@link Entity}, {@link Embeddable}, or
     * {@link MappedSuperclass} must reside in
     * {@code de.remsfal.notification.entity..}.
     * This prevents persistence-related concerns from leaking into
     * other architectural layers.
     */
    @ArchTest
    public static final ArchRule JPA_ANNOTATIONS_ONLY_IN_ENTITY_PACKAGES =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage("de.remsfal.notification.entity..")
                    .allowEmptyShould(true);

    /**
     * Ensures that the JAX-RS {@link Path} annotation is only used in
     * boundary or core API packages.
     * This rule enforces that REST resource definitions remain
     * confined to the transport layer and shared API contracts.
     */
    @ArchTest
    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY_OR_CORE_API =
            classes()
                    .that().resideInAnyPackage(NOTIFICATION_BASE)
                    .and().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(ALLOWED_JAXRS_PACKAGES)
                    .allowEmptyShould(true);

    /**
     * Ensures that HTTP method annotations (GET, POST, PUT, PATCH, DELETE)
     * are only declared in boundary or core API packages.
     * This prevents REST-specific annotations from being used in
     * control, entity, or infrastructure layers.
     */
    @ArchTest
    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY_OR_CORE_API =
            methods()
                    .that().areAnnotatedWith(GET.class)
                    .or().areAnnotatedWith(POST.class)
                    .or().areAnnotatedWith(PUT.class)
                    .or().areAnnotatedWith(PATCH.class)
                    .or().areAnnotatedWith(DELETE.class)
                    .and().areDeclaredInClassesThat().resideInAnyPackage(NOTIFICATION_BASE)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(ALLOWED_JAXRS_PACKAGES)
                    .allowEmptyShould(true);
}
