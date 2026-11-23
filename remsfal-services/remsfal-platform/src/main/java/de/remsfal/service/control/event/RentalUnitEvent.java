package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;

/**
 * Extension point for changes of the rental units of a project (property, site, building, apartment, storage and
 * commercial), see {@link ProjectEvent} for the semantics.
 */
public sealed interface RentalUnitEvent {

    UUID projectId();

    /** Snapshot of the rental unit, taken within the transaction (for a deletion the last state). */
    RentalUnitJson unit();

    UserJson actingUser();

    default UnitType unitType() {
        return unit().getType();
    }

    record Created(UUID projectId, RentalUnitJson unit, UserJson actingUser) implements RentalUnitEvent {
    }

    record Updated(UUID projectId, RentalUnitJson unit, UserJson actingUser) implements RentalUnitEvent {
    }

    record Deleted(UUID projectId, RentalUnitJson unit, UserJson actingUser) implements RentalUnitEvent {
    }

}
