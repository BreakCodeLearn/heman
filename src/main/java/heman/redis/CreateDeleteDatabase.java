package heman.redis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONObject;

public class CreateDeleteDatabase {

    public static void main(String[] args) {
        // Database API URLs
        String dbApiUrl = "https://172.16.22.21:9443/v1/bdbs";

        // Authentication
        String username = "admin@rl.org";
        String password = "nFbiQlO";
        String encodedAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        try {
            trustAllCertificates();

            // Create Redis database
            int uid = createRedisDB(dbApiUrl, encodedAuth, "heman-new-db");
            System.out.println("\n");

            // Delete Redis database
                deleteRedisDB(dbApiUrl, encodedAuth, uid);
 

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
