package de.remsfal.ticketing.entity.filter;

import java.util.UUID;

/**
 * Filter criteria for querying a user's activity feed, bundling the optional narrowing parameters
 * accepted by {@link de.remsfal.ticketing.control.ActivityFeedController} and
 * {@link de.remsfal.ticketing.entity.dao.ActivityFeedRepository#findByQuery}. Every field is an
 * optional equality filter: {@code null} means no restriction. Evaluated with {@code ALLOW
 * FILTERING} within the single {@code user_id} partition, which stays cheap since the partition
 * is always bounded to one recipient.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public record ActivityFeedFilter(
    UUID projectId,
    UUID issueId,
    UUID agreementId,
    UUID organizationId,
    UUID contractorId,
    UUID assigneeId) {
}
