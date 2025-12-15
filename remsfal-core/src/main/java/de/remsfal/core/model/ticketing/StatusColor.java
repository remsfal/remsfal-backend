package de.remsfal.core.model.ticketing;

public enum StatusColor {
    PENDING("#F1C40F", "#333333"),
    OPEN("#3498DB", "#ffffff"),
    IN_PROGRESS("#E67E22", "#ffffff"),
    CLOSED("#2ECC71", "#ffffff"),
    REJECTED("#E74C3C", "#ffffff");

    public final String backgroundColor;
    public final String textColor;

    StatusColor(String backgroundColor, String textColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }
}
