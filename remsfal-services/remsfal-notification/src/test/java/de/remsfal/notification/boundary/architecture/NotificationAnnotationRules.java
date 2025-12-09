package de.remsfal.notification.boundary.architecture;

import com.tngtech.archunit.lang.ArchRule;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;


public final class NotificationAnnotationRules {
    private NotificationAnnotationRules() {
    }

    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY =
            classes()
                    .that().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.notification.boundary.."
                    );

    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY =
            methods()
                    .that().areAnnotatedWith(GET.class)
                    .or().areAnnotatedWith(POST.class)
                    .or().areAnnotatedWith(PUT.class)
                    .or().areAnnotatedWith(DELETE.class)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(
                            "de.remsfal.notification.boundary.."
                    );
}
