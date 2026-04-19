//package com.ryleq.crb.crb.processing.service;
//
//import okhttp3.*;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.Base64;
//import java.util.Objects;
//
//import org.springframework.beans.factory.annotation.Value;
//
//import static util.com.neptune.crb.processing.Logger.logEvent;
//
//@Service
//public class AuthenticationService {
//    @Value("${metropol.api.public-key}")
//    private String publicKey;
//
//    @Value("${metropol.api.private-key}")
//    private String privateKey;
//
//    @Value("${metropol.api.base-url}")
//    private String baseUrl;
//
//    @Value("${metropol.api.port}")
//    private String port;
//
//    @Value("${metropol.api.version}")
//
//    private String apiVersion;
//
//    /**
//     * Authenticate and get an OAuth access token.
//     *
//     * @return the access token
//     * @throws IOException if the request fails
//     */
//    public String authenticate() throws IOException {
//        // Create Basic Auth token
//        String authUrl = String.format("%s:%s/%s/datasubmission/authenticate?grant_type=client_credentials",
//                baseUrl, port, apiVersion);
//
//        String authString = publicKey + ":" + privateKey;
//        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
//
//        // Build the request
//        OkHttpClient client = new OkHttpClient();
//        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(authUrl)).newBuilder();
//        urlBuilder.addQueryParameter("grant_type", "client_credentials");
//
//        Request request = new Request.Builder()
//                .url(urlBuilder.build())
//                .addHeader("Authorization", "Basic " + encodedAuth)
//                .addHeader("cache-control", "no-cache")
//                .post(RequestBody.create("", MediaType.parse("application/json"))) // Empty JSON body
//                .build();
//
//        // Execute the request
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                logEvent(response.body().string());
//                throw new IOException("Unexpected code " + response);
//            }
//
//            // Parse the response
//            String responseBody = response.body().string();
//            System.out.println("Response: " + responseBody);
//
//            // Extract access token (simplified, replace with a proper JSON parser if needed)
//            String accessToken = responseBody.split("\"access_token\":\"")[1].split("\"")[0];
//            return accessToken;
//        }
//    }
//}
//
package com.neptune.crb.processing.service;

import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;

import static com.neptune.crb.processing.util.Logger.logEvent;

@Service
public class AuthenticationService
{
    @Value("${metropol.api.public-key}")
    private String publicKey;

    @Value("${metropol.api.private-key}")
    private String privateKey;

    @Value("${metropol.api.base-url}")
    private String baseUrl;

    @Value("${metropol.api.port}")
    private String port;

    @Value("${metropol.api.version}")
    private String apiVersion;

    public String authenticate() throws IOException
    {
        String authUrl = String.format("%s:%s/%s/datasubmission/authenticate?grant_type=client_credentials",
                baseUrl, port, apiVersion);

        String authString = publicKey + ":" + privateKey;
        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());

        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(authUrl)).newBuilder();
        urlBuilder.addQueryParameter("grant_type", "client_credentials");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Authorization", "Basic " + encodedAuth)
                .addHeader("cache-control", "no-cache")
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                logEvent(response.body().string());
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            System.out.println("Response: " + responseBody);

            String accessToken = responseBody.split("\"access_token\":\"")[1].split("\"")[0];
            return accessToken;
        }
    }
}