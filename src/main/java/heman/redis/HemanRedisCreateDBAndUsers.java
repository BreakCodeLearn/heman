package heman.redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class HemanRedisCreateDBAndUsers {

    public static void main(String[] args) {
        // Database API URLs
        String dbApiUrl = "https://172.16.22.21:9443/v1/bdbs";
        // Users API URLs
        String usersApiUrl = "https://172.16.22.21:9443/v1/users";

        // Authentication
        String username = "admin@rl.org";
        String password = "nFbiQlO";
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        try {
            trustAllCertificates();
            System.out.println("\n");

            // Create Redis database
            int uid = createRedisDB(dbApiUrl, encodedAuth, "heman-new-db");

            System.out.println("\n");

            // Create admin user
            createUser(usersApiUrl, encodedAuth, "cary.johnson@example.com", "Cary Johnson", "admin");

            System.out.println("\n");

            // Display all users
            displayAllUsers(usersApiUrl, encodedAuth);

            // Add a line space
            System.out.println("\n");

            // Delete Redis database
            if (uid != -1) {
                deleteRedisDB(dbApiUrl, encodedAuth, uid);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int createRedisDB(String apiUrl, String encodedAuth, String databaseName) {
        try {
            // Create HTTP connection
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // If the connection is HTTPS, configure SSL
            if (connection instanceof HttpsURLConnection) {
                trustAllCertificates((HttpsURLConnection) connection);
            }

            // Set up the request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setDoOutput(true);

            // Create JSON data for the new database
            String postData = "{\"name\": \"" + databaseName + "\", \"memory_size\": 20480000}";

            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData.getBytes("utf-8"));
            }

            // Get the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Database creation request was successful (Response code: " + responseCode + ")");

                // Read the response body
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String dbName = jsonResponse.getString("name");
                    int uid = jsonResponse.getInt("uid");

                    // Print UID
                    System.out.println("New database name " + dbName + " UID: " + uid);

                    return uid;
                }
            } else {
                System.out.println("Failed to create database. Response code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
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
            String userData = String.format(
                    "{\"email\": \"%s\", \"name\": \"%s\", \"role\": \"%s\", \"password\": \"password\"}", email, name,
                    role);

            // Send the request
            connection.getOutputStream().write(userData.getBytes("UTF-8"));

            // Get the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("User created successfully: " + email);
            } else {
                System.out.println(
                        "Failed to create user as it already exists " + email + ". Response code: " + responseCode);
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

    private static void deleteRedisDB(String apiUrl, String encodedAuth, int uid) {
        int maxRetries = 3;
        int retryIntervalMillis = 5000; // 5 seconds

        for (int i = 0; i < maxRetries; i++) {
            try {
                // Construct the DELETE URL
                String deleteUrl = apiUrl + "/" + uid;

                // Create HTTP connection
                URL url = new URL(deleteUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // If the connection is HTTPS, configure SSL
                if (connection instanceof HttpsURLConnection) {
                    trustAllCertificates((HttpsURLConnection) connection);
                }

                // Set up the request
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

                // Get the response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                    System.out.println("Database with UID " + uid + " is busy. Retrying in " + retryIntervalMillis
                            + " milliseconds...");
                    Thread.sleep(retryIntervalMillis); // Wait before retrying
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("Database with UID " + uid + " deleted successfully.");
                    return; // Exit the function if deletion is successful
                }
            } catch (Exception e) {
                e.printStackTrace();
                return; // Exit the function if an exception occurs
            }
        }
        System.out.println("Max retries exceeded. Failed to delete database with UID " + uid);
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

    private static void trustAllCertificates(HttpsURLConnection httpsConnection) {
        try {
            // Create a TrustManager that trusts all certificates
            TrustManager[] trustAllCerts = { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            } };

            // Create SSLContext with the TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Set the SSLContext on the connection
            httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            // Disable hostname verification
            httpsConnection.setHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
