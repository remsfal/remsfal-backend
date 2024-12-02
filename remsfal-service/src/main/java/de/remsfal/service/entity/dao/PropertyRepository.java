package de.remsfal.service.entity.dao;

import de.remsfal.core.model.ProjectTreeNodeModel;
import de.remsfal.service.entity.dto.*;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class PropertyRepository extends AbstractRepository<PropertyEntity> {

    public List<PropertyEntity> findPropertiesByProjectId(final String projectId, final int offset, final int limit) {
        return getEntityManager().createNamedQuery("PropertyEntity.findByProjectId", PropertyEntity.class)
                .setParameter(PARAM_PROJECT_ID, projectId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public Optional<PropertyEntity> findPropertyById(final String projectId, final String propertyId) {
        return find("id = :id and projectId = :projectId",
                Parameters.with("id", propertyId).and(PARAM_PROJECT_ID, projectId))
                .singleResultOptional();
    }

    public long countPropertiesByProjectId(final String projectId) {
        return count(PARAM_PROJECT_ID, projectId);
    }

    public long deletePropertyById(final String projectId, final String propertyId) {
        return delete("id = :id and projectId = :projectId",
                Parameters.with("id", propertyId).and(PARAM_PROJECT_ID, projectId));
    }

    public List<ProjectTreeNodeModel> findProjectTreeByProjectId(final String projectId, final int offset, final int limit) {
        // Fetch properties for the project
        List<PropertyEntity> properties = getEntityManager()
                .createNamedQuery("PropertyEntity.findByProjectId", PropertyEntity.class)
                .setParameter(PARAM_PROJECT_ID, projectId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        List<ProjectTreeNodeModel> projectTree = new ArrayList<>();
        for (PropertyEntity property : properties) {
            // Fetch buildings for the property
            List<BuildingEntity> buildings = getEntityManager()
                    .createQuery("SELECT b FROM BuildingEntity b WHERE b.propertyId = :propertyId", BuildingEntity.class)
                    .setParameter("propertyId", property.getId())
                    .getResultList();

            ProjectTreeNode propertyNode = new ProjectTreeNode(property.getId(), "Property", property);

            for (BuildingEntity building : buildings) {
                // Fetch apartments and garages for the building
                List<ApartmentEntity> apartments = getEntityManager()
                        .createQuery("SELECT a FROM ApartmentEntity a WHERE a.buildingId = :buildingId", ApartmentEntity.class)
                        .setParameter("buildingId", building.getId())
                        .getResultList();

                List<GarageEntity> garages = getEntityManager()
                        .createQuery("SELECT g FROM GarageEntity g WHERE g.buildingId = :buildingId", GarageEntity.class)
                        .setParameter("buildingId", building.getId())
                        .getResultList();

                ProjectTreeNode buildingNode = new ProjectTreeNode(building.getId(), "Building", building);

                for (ApartmentEntity apartment : apartments) {
                    ProjectTreeNode apartmentNode = new ProjectTreeNode(apartment.getId(), "Apartment", apartment);
                    buildingNode.addChild(apartmentNode);
                }

                for (GarageEntity garage : garages) {
                    ProjectTreeNode garageNode = new ProjectTreeNode(garage.getId(), "Garage", garage);
                    buildingNode.addChild(garageNode);
                }

                propertyNode.addChild(buildingNode);
            }

            projectTree.add(propertyNode);
        }

        return projectTree;
    }
}