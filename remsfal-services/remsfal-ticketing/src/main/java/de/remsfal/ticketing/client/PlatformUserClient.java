package de.remsfal.ticketing.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.remsfal.core.json.UserJson;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST client for communicating with the Platform service to retrieve user information.
 *
 */
@RegisterRestClient(configKey = "platform-api")
@Path("/api/v1/user")
public interface PlatformUserClient {

    /**
     * Retrieve basic information for multiple users by their IDs.
     *
     * @param userIds List of user UUIDs to retrieve
     * @return Map of user ID (as String) to UserJson
     */
    @GET
    @Path("/batch")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, UserJson> getUsersBatch(@QueryParam("ids") List<UUID> userIds);

}
