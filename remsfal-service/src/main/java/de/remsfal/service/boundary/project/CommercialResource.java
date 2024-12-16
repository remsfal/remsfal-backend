package de.remsfal.service.boundary.project;

import de.remsfal.core.api.project.CommercialEndpoint;
import de.remsfal.core.json.project.CommercialJson;
import de.remsfal.service.boundary.ProjectResource;
import jakarta.ws.rs.core.Response;

public class CommercialResource extends ProjectResource implements CommercialEndpoint {


    /**
     * @param projectId
     * @param buildingId
     * @param commercial
     * @return
     */
    @Override
    public Response createCommercial(String projectId, String buildingId, CommercialJson commercial) {
        return null;
    }

    /**
     * @param projectId
     * @param buildingId
     * @param commercialId
     * @return
     */
    @Override
    public CommercialJson getCommercial(String projectId, String buildingId, String commercialId) {
        return null;
    }

    /**
     * @param projectId
     * @param buildingId
     * @return
     */
    @Override
    public Response getCommercials(String projectId, String buildingId) {
        return null;
    }

    /**
     * @param projectId
     * @param buildingId
     * @param commercialId
     * @param commercial
     * @return
     */
    @Override
    public CommercialJson updateCommercial(String projectId, String buildingId, String commercialId, CommercialJson commercial) {
        return null;
    }

    /**
     * @param projectId
     * @param buildingId
     * @param commercialId
     */
    @Override
    public void deleteCommercial(String projectId, String buildingId, String commercialId) {

    }
}
