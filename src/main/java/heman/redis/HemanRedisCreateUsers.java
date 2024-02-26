package heman.redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HemanRedisCreateUsers {

    public static void main(String[] args) {
        // API URLs
        String usersApiUrl = "https://172.16.22.21:9443/v1/users";

        // Authentication
        String username = "admin@rl.org";
        String password = "nFbiQlO";
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        try {
            trustAllCertificates();
            // Create admin user
            createUser(usersApiUrl, encodedAuth, "cary.johnson@example.com", "Cary Johnson", "admin");

            // Create db_member user
            //createUser(usersApiUrl, encodedAuth, "mike.smith@example.com", "Mike Smith", "db_member");

            // Create db_viewer user
             //createUser(usersApiUrl, encodedAuth, "john.doe@example.com", "John Doe", "db_viewer");

            // Display all users
            displayAllUsers(usersApiUrl, encodedAuth);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createUser(String apiUrl, String encodedAuth, String email, String name, String role) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setDoOutput(true);
    
            // Create JSON data for the new user
            String userData = String.format("{\"email\": \"%s\", \"name\": \"%s\", \"role\": \"%s\", \"password\": \"password\"}", email, name, role);
    
            // Send the request
            connection.getOutputStream().write(userData.getBytes("UTF-8"));
    
            // Get the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("User created successfully: " + email);
            } else {
                System.out.println("Failed to create user " + email + ". Response code: " + responseCode);
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
    

    private static void displayAllUsers(String apiUrl, String encodedAuth) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
    
            // Get the response
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
    
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Print each user data line by line
                        System.out.println(line);
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
    
     private static void trustAllCertificates() throws Exception {
        TrustManager[] trustAllCerts = { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        } };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}