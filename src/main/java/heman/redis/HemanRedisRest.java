package heman.redis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class HemanRedisRest {

    private static final String API_ENDPOINT = "https://re-cluster1.ps-redislabs.org:9443";
    private static final String USERNAME = "admin@rl.org";
    private static final String PASSWORD = "nFbiQlO";

    public static void main(String[] args) {
        String databaseId = createDatabase();
        if (databaseId != null) {
            System.out.println("New database created with ID: " + databaseId);
        } else {
            System.out.println("Failed to create database.");
        }
    }

    private static String createDatabase() {
        try {
            URL url = new URL(API_ENDPOINT + "/v1/bdbs");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Basic " +  getEncodedCredentials());
            conn.setDoOutput(true);

            String requestBody = "{}";
            conn.getOutputStream().write(requestBody.getBytes());

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();
        } catch (Exception e) {
            System.out.println("Error creating database: " + e.getMessage());
            return null;
        }
    }

    private static String getEncodedCredentials() {
        String credentials = USERNAME + ":" + PASSWORD;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
