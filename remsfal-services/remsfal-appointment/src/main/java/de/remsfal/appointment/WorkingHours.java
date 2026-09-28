package de.remsfal.appointment;

import java.time.LocalTime;
import java.util.List;

/**
 * Represents the working hours of a craftsman, including start and end times and any breaks.
 */

public class WorkingHours {
    public LocalTime start;
    public LocalTime end;
    public List<Break> breaks;

    public boolean isWithin(LocalTime slotStart, LocalTime slotEnd) {
        if (slotStart.isBefore(start) || slotEnd.isAfter(end)) {
            return false;
        }
        if (breaks == null) {
            return true;
        }
        for (Break b : breaks) {
            if (slotStart.isBefore(b.end) && slotEnd.isAfter(b.start)) {
                return false;
            }
        }
        return true;
    }

    public static class Break {
        public LocalTime start;
        public LocalTime end;
    }
}