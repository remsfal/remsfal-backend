package de.remsfal.appointment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a list of available time slots for appointments.
 */

public class SlotList {
    public List<LocalDateTime> slots;

    public SlotList() {
    }

    public SlotList(List<LocalDateTime> slots) {
        this.slots = slots;
    }
}
