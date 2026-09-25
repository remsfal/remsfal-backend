package de.remsfal.core.model.ticketing;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import jakarta.annotation.Nullable;

import java.util.List;
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
    String getPlaceOfPerformanceAddress1();

    @Nullable
    String getPlaceOfPerformanceAddress2();

    @Nullable
    String getPlaceOfPerformanceAddress3();

    @Nullable
    UnitType getRentalUnitType();

    @Nullable
    String getRentalUnitTitle();

    @Nullable
    String getRentalUnitLocation();

    @Nullable
    List<UserJson> getTenants();

}
