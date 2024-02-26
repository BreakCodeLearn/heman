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

public class HemanRedisCreateDB {

    public static void main(String[] args) {

        String apiUrl = "https://172.16.22.21:9443/v1/bdbs";
        String username = "admin@rl.org";
        String password = "nFbiQlO";
        String databaseName = "heman-new-db";

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
                // Trust all certificates
                trustAllCertificates(httpsConnection);
            }

            // Set up the request
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setDoOutput(true);

            // Create JSON data for the new database
            String postData = "{\"name\": \"" + databaseName + "\", \"memory_size\": 20480000}";

            // Send the request
            connection.getOutputStream().write(postData.getBytes("UTF-8"));

            // Get the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Database \"" + databaseName + "\" created successfully.");
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Request was successful (Response code: " + responseCode + ")");
            } else {
                System.out.println("Failed to create database. Response code: " + responseCode);
                // Print response message for debugging
                BufferedReader reader = null;
                try {
                    if (connection.getErrorStream() != null) {
                        reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    } else {
                        System.out.println("Error stream is null.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
