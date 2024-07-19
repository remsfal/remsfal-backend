package de.remsfal.service.control;

import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.project.RentModel;
import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.service.entity.dto.RentEntity;
import de.remsfal.service.entity.dto.TenancyEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class TenancyController {

    @Inject
    UserController userController;

    @Transactional(TxType.MANDATORY)
    public TenancyEntity updateTenancy(final String projectId, TenancyEntity entity, final TenancyModel tenancy) {
        if(entity == null) {
            entity = new TenancyEntity();
            entity.generateId();
            entity.setProjectId(projectId);
        }
        if(tenancy.getRent() != null) {
            updateRent(entity, tenancy.getRent());
        }
        if(tenancy.getTenant() != null) {
            updateTenant(entity, tenancy.getTenant());
        }
        if(tenancy.getStartOfRental() != null) {
            entity.setStartOfRental(tenancy.getStartOfRental());
        }
        if(tenancy.getEndOfRental() != null) {
            entity.setEndOfRental(tenancy.getEndOfRental());
        }
        return entity;
    }

    private void updateRent(TenancyEntity entity, final List<? extends RentModel> rent) {
        final List<RentEntity> list = new ArrayList<>();
        for (RentModel model : rent) {
            final RentEntity r = new RentEntity();
            r.generateId();
            r.setBillingCycle(model.getBillingCycle());
            r.setFirstPaymentDate(model.getFirstPaymentDate());
            r.setLastPaymentDate(model.getLastPaymentDate());
            r.setBasicRent(model.getBasicRent());
            r.setOperatingCostsPrepayment(model.getOperatingCostsPrepayment());
            r.setHeatingCostsPrepayment(model.getHeatingCostsPrepayment());
            list.add(r);
        }
        entity.setRent(list);
    }

    private void updateTenant(TenancyEntity entity, final CustomerModel tenant) {
        if(entity.getTenant() == null) {
            entity.setTenant(userController.findOrCreateUser(tenant));
        }
        userController.updateUser(entity.getTenant().getId(), tenant);
    }

}
