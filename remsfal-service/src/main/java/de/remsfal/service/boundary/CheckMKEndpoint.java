package de.remsfal.service.boundary;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface CheckMKEndpoint {



    static final String HOST_NAME = "localhost";
    static final String SITE_NAME = "test_site";
    static final String PROTO = "8080";
    static final String API_URL = String.format("http://localhost:8080/test_site/check_mk/api/1.0", PROTO, HOST_NAME, SITE_NAME);
    static final String COOKIE_VALUE = "ibo";  // Ersetze dies mit dem tatsächlichen Cookie-Wert
    public static void main(String[] args) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/objects/host/example.com"))
                    .header("Cookie", "auth_" + SITE_NAME + "=" + COOKIE_VALUE)
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                System.out.println(response.body());
            } else {
                throw new RuntimeException("Failed : HTTP error code : " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


