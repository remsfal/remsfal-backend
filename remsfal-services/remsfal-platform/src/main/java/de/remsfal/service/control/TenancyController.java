package de.remsfal.service.control;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.project.TenancyModel;
import de.remsfal.service.entity.dto.TenancyEntity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@RequestScoped
public class TenancyController {

    @Inject
    UserController userController;

    public List<TenancyModel> getTenancies(RemsfalPrincipal principal) {
        // TODO Auto-generated method stub
        return null;
    }

    public TenancyModel getTenancy(final UserModel tenant, final String tenancyId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Transactional(TxType.MANDATORY)
    public TenancyEntity updateTenancy(final String projectId, TenancyEntity entity, final TenancyModel tenancy) {
        if(entity == null) {
            entity = new TenancyEntity();
            entity.generateId();
            entity.setProjectId(projectId);
        }
        if(tenancy.getStartOfRental() != null) {
            entity.setStartOfRental(tenancy.getStartOfRental());
        }
        if(tenancy.getEndOfRental() != null) {
            entity.setEndOfRental(tenancy.getEndOfRental());
        }
        return entity;
    }

}
