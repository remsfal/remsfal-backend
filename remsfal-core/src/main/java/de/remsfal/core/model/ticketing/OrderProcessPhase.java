package de.remsfal.core.model.ticketing;

/**
 * Identifies which order-management process phase an {@link OrderAttachmentModel} belongs to.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public enum OrderProcessPhase {
    QUOTATION_REQUEST,
    QUOTATION,
    ORDER_PLACEMENT
}
