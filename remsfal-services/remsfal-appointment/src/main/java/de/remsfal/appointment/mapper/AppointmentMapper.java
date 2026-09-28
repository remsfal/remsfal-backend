package de.remsfal.appointment.mapper;

import java.util.stream.Collectors;

import de.remsfal.appointment.AppointmentRequest;
import de.remsfal.appointment.WorkingHours;
import de.remsfal.appointment.entity.AppointmentEntity;
import de.remsfal.appointment.entity.BreakEntity;
import de.remsfal.appointment.entity.WorkingHoursEntity;

/**
 * Maps appointment data between AppointmentRequest DTOs and AppointmentEntity objects.
 */

public class AppointmentMapper {

    public static AppointmentEntity toEntity(AppointmentRequest dto) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.id = dto.id;
        entity.craftsmanId = dto.craftsmanId;
        entity.resourceId = dto.resourceId;
        entity.type = dto.type;
        entity.durationMinutes = dto.durationMinutes;
        entity.from = dto.from;
        entity.to = dto.to;
        entity.status = dto.status;
        entity.confirmedStart = dto.confirmedStart;
        entity.confirmedEnd = dto.confirmedEnd;
        entity.timezone = dto.timezone;
        entity.cancellationReason = dto.cancellationReason;

        if (dto.workingHours != null) {
            WorkingHoursEntity whEntity = new WorkingHoursEntity();
            whEntity.appointment = entity;
            whEntity.start = dto.workingHours.start;
            whEntity.end = dto.workingHours.end;

            if (dto.workingHours.breaks != null) {
                whEntity.breaks = dto.workingHours.breaks.stream()
                    .map(b -> {
                        BreakEntity breakEntity = new BreakEntity();
                        breakEntity.workingHours = whEntity;
                        breakEntity.start = b.start;
                        breakEntity.end = b.end;
                        return breakEntity;
                    })
                    .collect(Collectors.toList());
            }

            entity.workingHours = whEntity;
        }

        return entity;
    }

    public static AppointmentRequest toDto(AppointmentEntity entity) {
        AppointmentRequest dto = new AppointmentRequest();
        dto.id = entity.id;
        dto.craftsmanId = entity.craftsmanId;
        dto.resourceId = entity.resourceId;
        dto.type = entity.type;
        dto.durationMinutes = entity.durationMinutes;
        dto.from = entity.from;
        dto.to = entity.to;
        dto.status = entity.status;
        dto.confirmedStart = entity.confirmedStart;
        dto.confirmedEnd = entity.confirmedEnd;
        dto.timezone = entity.timezone;
        dto.cancellationReason = entity.cancellationReason;

        if (entity.workingHours != null) {
            WorkingHours wh = new WorkingHours();
            wh.start = entity.workingHours.start;
            wh.end = entity.workingHours.end;

            if (entity.workingHours.breaks != null) {
                wh.breaks = entity.workingHours.breaks.stream()
                    .map(b -> {
                        WorkingHours.Break breakDto = new WorkingHours.Break();
                        breakDto.start = b.start;
                        breakDto.end = b.end;
                        return breakDto;
                    })
                    .collect(Collectors.toList());
            }

            dto.workingHours = wh;
        }

        return dto;
    }
}
