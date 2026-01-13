package de.remsfal.notification.control.parameter;

/**
 * Maps issue statuses to badge colors used in email templates.
 * backgroundColor is applied to the status badge, textColor ensures sufficient contrast.
 */
public enum StatusColor {
    PENDING("#F1C40F", "#333333"),
    OPEN("#185D8C", "#ffffff"),
    IN_PROGRESS("#AE5C13", "#ffffff"),
    CLOSED("#1D8147", "#ffffff"),
    REJECTED("#DA2E1B", "#ffffff");

    public final String backgroundColor;
    public final String textColor;

    StatusColor(String backgroundColor, String textColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }
}
