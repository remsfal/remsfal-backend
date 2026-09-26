package de.remsfal.service.boundary.authentication;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.remsfal.core.api.AuthenticationEndpoint;
import de.remsfal.core.model.UserModel;
import de.remsfal.service.control.AuthorizationController;
import de.remsfal.service.control.DevDataSeedController;
import de.remsfal.service.control.DevDataSeedController.SeedUser;
import io.quarkus.arc.properties.IfBuildProperty;

/**
 * Login page for local development that replaces the Google login. Any email address can be used;
 * unknown users are created on first login just like with Google.
 * <p>
 * Only included in builds with {@code de.remsfal.auth.dev-login.enabled=true} (dev profile).
 */
@IfBuildProperty(name = DevLoginResource.ENABLED_PROPERTY, stringValue = "true")
@Path(DevLoginResource.PATH)
public class DevLoginResource extends AbstractAuthenticationResource {

    public static final String ENABLED_PROPERTY = "de.remsfal.auth.dev-login.enabled";

    public static final String PATH = "/" + AuthenticationEndpoint.CONTEXT + "/" + AuthenticationEndpoint.VERSION
        + "/" + AuthenticationEndpoint.SERVICE + "/dev-login";

    @ConfigProperty(name = ENABLED_PROPERTY, defaultValue = "false")
    boolean devLoginEnabled;

    @Inject
    AuthorizationController controller;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String loginPage() {
        checkEnabled();
        final StringBuilder users = new StringBuilder();
        for (final SeedUser seedUser : DevDataSeedController.SEED_USERS) {
            users.append("""
                <button type="submit" name="email" value="%s">
                  <strong>%s %s</strong><span>%s</span><small>%s</small>
                </button>
                """.formatted(escape(seedUser.email()), escape(seedUser.firstName()),
                escape(seedUser.lastName()), escape(seedUser.description()), escape(seedUser.email())));
        }
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>REMSFAL Dev Login</title>
              <style>
                body { font-family: system-ui, sans-serif; background: #f4f5f7; color: #1f2933;
                  margin: 0; padding: 32px 16px; }
                main { max-width: 440px; margin: 0 auto; background: #fff; border-radius: 8px;
                  padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,.12); }
                h1 { font-size: 1.25rem; margin: 0 0 4px; }
                p { color: #52606d; margin: 0 0 20px; font-size: .9rem; }
                form { display: flex; flex-direction: column; gap: 8px; }
                button { text-align: left; border: 1px solid #cbd2d9; background: #fff; border-radius: 6px;
                  padding: 10px 12px; cursor: pointer; display: flex; flex-direction: column; gap: 2px; font: inherit; }
                button:hover { border-color: #2f80ed; background: #f0f6ff; }
                button span, button small { color: #52606d; font-size: .85rem; }
                hr { border: 0; border-top: 1px solid #e4e7eb; margin: 16px 0; width: 100%%; }
                input { font: inherit; padding: 10px 12px; border: 1px solid #cbd2d9; border-radius: 6px; }
                .free { flex-direction: row; justify-content: center; background: #2f80ed; color: #fff;
                  border-color: #2f80ed; }
                .free:hover { background: #1c6ad6; }
              </style>
            </head>
            <body>
              <main>
                <h1>REMSFAL Dev Login</h1>
                <p>Development only &ndash; replaces the Google login. Emails are delivered to Mailpit.</p>
                <!-- no action attribute: the form is posted to the current URL including ?route=... -->
                <form method="post">
                  %s
                </form>
                <hr>
                <form method="post">
                  <input type="email" name="email" required placeholder="any address, e.g. someone@%s">
                  <button type="submit" class="free">Login</button>
                </form>
              </main>
            </body>
            </html>
            """.formatted(users, DevDataSeedController.DEV_DOMAIN);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("email") @NotBlank @Email @Size(max = 254) final String email,
        @DefaultValue("/") @QueryParam("route") final String route) {
        checkEnabled();
        final String normalizedEmail = email.trim().toLowerCase();
        logger.warnv("Dev login used for {0} - this must never happen in production!", normalizedEmail);
        final UserModel user = controller.authenticateUser(
            DevDataSeedController.tokenIdOf(normalizedEmail), normalizedEmail, resolveLocale());
        return createSession(user, sanitizeRoute(route));
    }

    private void checkEnabled() {
        if (!devLoginEnabled) {
            throw new NotFoundException();
        }
    }

    private static String sanitizeRoute(final String route) {
        if (route == null || !route.startsWith("/") || route.startsWith("//")) {
            return "/";
        }
        return route;
    }

    private static String escape(final String value) {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

}
