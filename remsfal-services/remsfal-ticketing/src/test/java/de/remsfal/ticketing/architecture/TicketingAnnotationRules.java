package de.remsfal.ticketing.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.ws.rs.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

public final class TicketingAnnotationRules {

    private static final String TICKETING_BASE = "de.remsfal.ticketing..";
    private static final String[] ALLOWED_JAXRS_PACKAGES = {
            "de.remsfal.ticketing.boundary..",
            "de.remsfal.core.api.."
    };

    private TicketingAnnotationRules() { }

    @ArchTest
    public static final ArchRule JPA_ANNOTATIONS_ONLY_IN_ENTITY_PACKAGES =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage("de.remsfal.ticketing.entity..")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY_OR_CORE_API =
            classes()
                    .that().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(ALLOWED_JAXRS_PACKAGES)
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY_OR_CORE_API =
            methods()
                    .that().areAnnotatedWith(GET.class)
                    .or().areAnnotatedWith(POST.class)
                    .or().areAnnotatedWith(PUT.class)
                    .or().areAnnotatedWith(PATCH.class)
                    .or().areAnnotatedWith(DELETE.class)
                    .and().areDeclaredInClassesThat().resideInAnyPackage(TICKETING_BASE)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(ALLOWED_JAXRS_PACKAGES)
                    .allowEmptyShould(true);
}
