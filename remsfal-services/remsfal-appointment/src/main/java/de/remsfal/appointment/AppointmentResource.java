package de.remsfal.appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import de.remsfal.appointment.service.AppointmentService;
import de.remsfal.appointment.service.ICalendarService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * RESTful resource exposing appointment scheduling endpoints and calendar integration.
 * Delegates business logic to service layer and returns JSON responses (or ics files).
 */

@Path("/api/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Appointments", description = "Provider-independent appointment scheduling")
public class AppointmentResource {

    @Inject
    AppointmentService service;

    @Inject
    ICalendarService iCalendarService;

    @POST
    @Operation(summary = "Create an appointment request", description = "Creates a new appointment request with available time slots")
    public Response create(@Valid AppointmentRequest request) {
        UUID id = service.create(request);
        return Response.status(Response.Status.CREATED).entity(service.get(id)).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Get appointment details", description = "Retrieves the details of an appointment request")
    public AppointmentRequest get(@PathParam("id") UUID id) {
        return service.get(id);
    }

    @GET
    @Path("{id}/available-slots")
    @Operation(summary = "Get available slots", description = "Retrieves all available time slots for an appointment request")
    public SlotList getSlots(@PathParam("id") UUID id) {
        List<LocalDateTime> slots = service.computeAvailableSlots(id);
        return new SlotList(slots);
    }

    @POST
    @Path("{id}/book")
    @Operation(summary = "Book an appointment", description = "Confirms the appointment by selecting a specific time slot from the available slots")
    public Response book(@PathParam("id") UUID id, @Valid BookingRequest request) {
        try {
            service.confirmBooking(id, request != null ? request.slotStart : null);
            return Response.ok(service.get(id)).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorMessage(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("{id}/decline")
    @Operation(summary = "Decline an appointment", description = "Marks an appointment request as declined")
    public AppointmentRequest decline(@PathParam("id") UUID id) {
        service.decline(id);
        return service.get(id);
    }

    @POST
    @Path("{id}/cancel")
    @Operation(summary = "Cancel an appointment", description = "Cancels an open or confirmed appointment with an optional reason")
    public Response cancel(@PathParam("id") UUID id, @Valid CancellationRequest request) {
        try {
            service.cancel(id, request != null ? request.reason : null);
            return Response.ok(service.get(id)).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorMessage(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("{id}/ical")
    @Produces("text/calendar")
    @Operation(summary = "Export to iCalendar", description = "Downloads appointment as .ics file for calendar import")
    public Response exportToICal(@PathParam("id") UUID id) {
        try {
            AppointmentRequest appointment = service.get(id);
            
            if (appointment.status != BookingStatus.CONFIRMED) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorMessage("Only confirmed appointments can be exported"))
                        .build();
            }
            
            byte[] icalData = iCalendarService.generateICalendar(appointment);
            
            return Response.ok(icalData)
                    .header("Content-Disposition", "attachment; filename=\"appointment-" + id + ".ics\"")
                    .header("Content-Type", "text/calendar; charset=utf-8")
                    .build();
                    
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorMessage("Failed to generate calendar file: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("{id}/google-calendar")
    @Operation(summary = "Add to Google Calendar", description = "Generates a Google Calendar add link")
    public Response getGoogleCalendarLink(@PathParam("id") UUID id) {
        AppointmentRequest appointment = service.get(id);
        
        if (appointment.status != BookingStatus.CONFIRMED) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Only confirmed appointments can be added to calendar"))
                    .build();
        }
        
        String googleCalendarUrl = buildGoogleCalendarUrl(appointment);
        
        return Response.ok(new CalendarLink(googleCalendarUrl)).build();
    }

    private String buildGoogleCalendarUrl(AppointmentRequest appointment) {
        String baseUrl = "https://calendar.google.com/calendar/render?action=TEMPLATE";
        
        String title = appointment.type + " Appointment";
        
        String details = "Type: " + appointment.type + "\\n" +
                        "Duration: " + appointment.durationMinutes + " minutes\\n" +
                        "Craftsman: " + appointment.craftsmanId + "\\n" +
                        "Resource: " + appointment.resourceId + "\\n" +
                        "Timezone: " + appointment.timezone;
        
        // Date format: yyyyMMddTHHmmss (Google Calendar format)
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        
        String startDate = appointment.confirmedStart.format(formatter);
        String endDate = appointment.confirmedEnd.format(formatter);
        
        String location = "Resource: " + appointment.resourceId;
        
        try {
            String url = baseUrl +
                    "&text=" + java.net.URLEncoder.encode(title, "UTF-8") +
                    "&dates=" + startDate + "/" + endDate +
                    "&details=" + java.net.URLEncoder.encode(details, "UTF-8") +
                    "&location=" + java.net.URLEncoder.encode(location, "UTF-8");
            
            // Add timezone parameter (ctz = calendar timezone)
            url += "&ctz=" + java.net.URLEncoder.encode(appointment.timezone, "UTF-8");
            
            return url;
        } catch (Exception e) {
            return baseUrl;
        }
    }

    public static class ErrorMessage {
        public String error;

        public ErrorMessage() {
        }

        public ErrorMessage(String error) {
            this.error = error;
        }
    }

    public static class CalendarLink {
        public String url;
        
        public CalendarLink() {}
        
        public CalendarLink(String url) {
            this.url = url;
        }
    }
}