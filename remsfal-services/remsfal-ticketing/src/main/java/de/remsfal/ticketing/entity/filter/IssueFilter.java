package de.remsfal.ticketing.entity.filter;

import java.util.List;
import java.util.UUID;

import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;

/**
 * Filter criteria for querying issues, bundling the scalar/list filter parameters accepted by
 * {@link de.remsfal.ticketing.control.IssueController} and
 * {@link de.remsfal.ticketing.entity.dao.IssueRepository#findByQuery}.
 * {@code isVisibleToTenants} is an optional equality filter like the others: {@code null} means no
 * restriction, {@code true}/{@code false} restricts to issues with that exact visibility. Pagination
 * ({@code cursor}, {@code limit}) is kept as a separate parameter since it is orthogonal to what is
 * being filtered.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public record IssueFilter(
    UUID projectId,
    UUID assigneeId,
    UUID agreementId,
    UnitType rentalUnitType,
    UUID rentalUnitId,
    List<IssueType> type,
    List<IssueStatus> status,
    Boolean isVisibleToTenants) {
}
