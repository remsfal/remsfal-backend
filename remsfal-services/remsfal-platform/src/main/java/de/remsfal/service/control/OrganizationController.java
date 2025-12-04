package de.remsfal.service.control;

import de.remsfal.service.entity.dao.OrganizationRepository;
import jakarta.enterprise.context.RequestScoped;
import org.jboss.logging.Logger;

import javax.inject.Inject;


@RequestScoped
public class OrganizationController {

    @Inject
    Logger logger;

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    AddressController addressController;

    //TODO: Methoden einfügen
}
