package de.remsfal.appointment.service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import de.remsfal.appointment.AppointmentRequest;
import jakarta.enterprise.context.ApplicationScoped;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

/**
 * Service component generating iCalendar (.ics) files for appointments.
 */

@ApplicationScoped
public class ICalendarService {

    public byte[] generateICalendar(AppointmentRequest appointment) throws Exception {

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Remsfal//Appointment Service//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(Method.REQUEST);

        ZoneId zoneId = appointment.getZoneId();
        ZonedDateTime startZoned = ZonedDateTime.of(appointment.confirmedStart, zoneId);
        ZonedDateTime endZoned = ZonedDateTime.of(appointment.confirmedEnd, zoneId);
        
        Date startDate = Date.from(startZoned.toInstant());
        Date endDate = Date.from(endZoned.toInstant());
        
        VEvent event = new VEvent(
            new DateTime(startDate),
            new DateTime(endDate),
            getEventTitle(appointment)
        );

        event.getProperties().add(new Uid(appointment.id.toString()));
        event.getProperties().add(new Description(getEventDescription(appointment)));
        event.getProperties().add(new Status("CONFIRMED"));
        event.getProperties().add(new Transp("OPAQUE")); // Shows as busy
        
        if (appointment.resourceId != null) {
            event.getProperties().add(new Location("Resource: " + appointment.resourceId));
        }

        event.getProperties().add(new Organizer("MAILTO:noreply@remsfal.de"));

        Attendee attendee = new Attendee("MAILTO:craftsman@remsfal.de");
        attendee.getParameters().add(new Cn(appointment.craftsmanId));
        attendee.getParameters().add(PartStat.ACCEPTED);
        event.getProperties().add(attendee);

        // Add alarm (reminder 15 minutes before)
        VAlarm reminder = new VAlarm(java.time.Duration.ofMinutes(-15));
        reminder.getProperties().add(Action.DISPLAY);
        reminder.getProperties().add(new Description("Appointment reminder"));
        event.getAlarms().add(reminder);

        calendar.getComponents().add(event);

        // Convert to bytes for output
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(calendar, out);
        
        return out.toByteArray();
    }

    private String getEventTitle(AppointmentRequest appointment) {
        return appointment.type.toString() + " Appointment";
    }

    private String getEventDescription(AppointmentRequest appointment) {
        StringBuilder desc = new StringBuilder();
        desc.append("Type: ").append(appointment.type).append("\n");
        desc.append("Duration: ").append(appointment.durationMinutes).append(" minutes\n");
        desc.append("Craftsman: ").append(appointment.craftsmanId).append("\n");
        desc.append("Resource: ").append(appointment.resourceId).append("\n");
        desc.append("Status: ").append(appointment.status);
        return desc.toString();
    }
}
