package de.remsfal.service.boundary.authentication;

import java.io.IOException;
import java.net.URI;
import javax.enterprise.context.ApplicationScoped;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.model.UserModel;
import org.json.JSONObject;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
@ApplicationScoped
public class TokenValidator {

    public TokenInfo validate(final String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        String responseString = "";
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v1/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            responseString = response.body();

            System.out.println(response.statusCode());
            System.out.println("Google API response: " + response.body());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JSONObject responseJson = new JSONObject(responseString);
        System.out.println("Google API response: " + responseJson.toString());
        String id = responseJson.getString("id");
        String userName = responseJson.getString("name");
        String userEmail = responseJson.getString("email");

        final UserModel user = ImmutableUserJson.builder()
                .id(id)
                .email(userEmail)
                .name(userName)
                .build();
        return new TokenInfo(user);
    }
}
