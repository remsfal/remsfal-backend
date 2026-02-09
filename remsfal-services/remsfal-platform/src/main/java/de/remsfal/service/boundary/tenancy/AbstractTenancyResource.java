package de.remsfal.service.boundary.tenancy;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import java.util.UUID;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.service.control.RentalAgreementController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class AbstractTenancyResource {

    @Context
    protected UriInfo uri;

    @Inject
    protected RemsfalPrincipal principal;

    @Inject
    protected RentalAgreementController agreementController;

    public boolean checkReadPermissions(final UUID tenancyId) {
        if (agreementController.getRentalAgreement(principal, tenancyId) == null) {
            throw new NotFoundException("Unable to find tenancy for tenant");
        } else {
            return true;
        }

    }

}