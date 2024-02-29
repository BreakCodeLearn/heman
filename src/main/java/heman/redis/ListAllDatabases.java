package heman.redis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class ListAllDatabases {

    public static void main(String[] args) {
        String apiUrl = "https://172.16.22.21:9443/v1/bdbs";
        String username = "admin@rl.org";
        String password = "nFbiQlO";

        try {
            // Encode username and password for basic authentication
            String authString = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());

            // Create HTTP connection
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // If the connection is HTTPS, cast to HttpsURLConnection and configure SSL
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                configureSSL(httpsConnection);
            }

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Send the request
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read and parse the JSON response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Process the JSON response
                processResponse(response.toString());
            } else {
                System.out.println("Failed to get databases. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void configureSSL(HttpsURLConnection connection) throws Exception {
        // Create a TrustManager that trusts all certificates
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
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
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
        // Disable hostname verification
        connection.setHostnameVerifier((hostname, session) -> true);
    }

    private static void processResponse(String jsonResponse) {
        JSONArray databases = new JSONArray(jsonResponse);
        for (int i = 0; i < databases.length(); i++) {
            JSONObject database = databases.getJSONObject(i);
            String dbName = database.getString("name");
            String status = database.getString("status");
            int uid = database.getInt("uid"); // Extract uid as integer directly

            int port = database.getJSONArray("endpoints").getJSONObject(0).getInt("port");
            System.out.println("Database Name: " + dbName);
            System.out.println("Status: " + status);
            System.out.println("Port: " + port);
            System.out.println("UID: " + uid);
            // Extract and print other relevant information as needed
            System.out.println();
        }
    }

}
