package de.remsfal.service.entity.dao;

import de.remsfal.service.entity.dto.OrganizationEmployeeEntity;
import de.remsfal.service.entity.dto.OrganizationEntity;
import de.remsfal.service.entity.dto.ProjectMembershipEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrganizationRepository extends AbstractRepository<OrganizationEntity> {

    //TODO: Repository implementieren
    public List<OrganizationEmployeeEntity> findOrganizationEmployeesByOrganizationId(Long organizationId) {
        return null;
    }

}
