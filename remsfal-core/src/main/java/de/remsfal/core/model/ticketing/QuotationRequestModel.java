package de.remsfal.core.model.ticketing;

import de.remsfal.core.model.RentalUnitModel.UnitType;
import jakarta.annotation.Nullable;

import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface QuotationRequestModel extends OrderProcessModel {

    enum RequestStatus {
        REQUESTED,
        WITHDRAWN,
        VIEWING_REQUIRED,
        CONSULTATION_REQUIRED,
        REJECTED,
        SUBMITTED
    }

    UUID getInitiatorId();

    String getInitiatedBy();

    RequestStatus getStatus();

    @Nullable
    String getScopeOfWork();

    @Nullable
    String getPlaceOfPerformance1();

    @Nullable
    String getPlaceOfPerformance2();

    @Nullable
    String getPlaceOfPerformance3();

    @Nullable
    String getPlaceOfPerformanceRentalUnit();

    @Nullable
    UnitType getRentalUnitType();

    @Nullable
    String getRentalUnitTitle();

}
