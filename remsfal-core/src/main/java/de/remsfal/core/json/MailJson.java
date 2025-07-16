package de.remsfal.core.json;

import de.remsfal.core.model.CustomerModel;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Mail format")
public class MailJson {
    @Valid
    @Schema(description = "User information")
    private UserJson user;

    @Schema(description = "Locale information")
    private String locale;

    @Schema(description = "Type of email, e.g. registration or project membership")
    private String type;

    @Schema(description = "link")
    private String link;

    public void setUser(UserJson user) {
        this.user = user;
    }

    public CustomerModel getUser() {
        return user;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setLink(String link) {
        this.link = link;
    }
    public String getLink() {
        return link;
    }
}
