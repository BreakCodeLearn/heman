package heman.redis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestCreateListUsers {

    public static void main(String[] args) {
    // Users API URLs
    String usersApiUrl = "https://172.16.22.21:9443/v1/users";

    // Authentication
    String encodedAuth = "YWRtaW5Acmwub3JnOm5GYmlRbE8=";
        try {
            // Setup custom truststore
            setupCustomTruststore();

            // Display all users
            displayAllUsers(usersApiUrl, encodedAuth);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupCustomTruststore() throws Exception {
        // Load your custom truststore
        String truststoreFile = "/home/coder/hemantruststore.jks"; // Adjust this path
        String truststorePassword = "heman007"; // Adjust this password
    
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream inputStream = new FileInputStream(truststoreFile)) {
            trustStore.load(inputStream, truststorePassword.toCharArray());
        }
    
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
    
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
    
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }
    
    private static void displayAllUsers(String apiUrl, String encodedAuth) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Get the response
            int responseCode = connection.getResponseCode();
            System.out.println("Display users list " + "Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder usersData = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Append each line to the usersData StringBuilder
                        usersData.append(line);
                    }

                    // Parse JSON array of users
                    JSONArray usersArray = new JSONArray(usersData.toString());

                    // Iterate through the users and print required fields
                    for (int i = 0; i < usersArray.length(); i++) {
                        JSONObject user = usersArray.getJSONObject(i);
                        String name = user.getString("name");
                        String role = user.getString("role");
                        String email = user.getString("email");

                        // Print user details in the specified format
                        System.out.println("Name: " + name + ", Role: " + role + ", Email: " + email);
                    }
                }
            } else {
                System.out.println("Failed to fetch users. Response code: " + responseCode);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }

            connection.disconnect(); // Close the connection

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
