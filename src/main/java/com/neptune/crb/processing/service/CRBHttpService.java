//package com.ryleq.crb.crb.processing.service;
//
//import config.com.neptune.crb.processing.CRBConfig;
//import config.com.neptune.crb.processing.WebConfig;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.*;
//import org.apache.commons.codec.binary.Hex;
//import org.json.JSONObject;
//import org.springframework.stereotype.Service;
//
//import javax.net.ssl.*;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.security.cert.X509Certificate;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.TimeZone;
//import java.util.concurrent.TimeUnit;
//
//import static util.com.neptune.crb.processing.Logger.logError;
//import static util.com.neptune.crb.processing.Logger.logEvent;
//
//@Slf4j
//@Service
//public class CRBHttpService
//{
//
//    private final CRBConfig crbConfig;
//    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmsss");
//    private final WebConfig webConfig;
//
//    public CRBHttpService(CRBConfig crbConfig, WebConfig webConfig)
//    {
//        this.crbConfig = crbConfig;
//        this.webConfig = webConfig;
//    }
//
//    public String connectToService(String identityNumber, Integer loanAmount)
//    {
//        try
//        {
//            String timestamp = getUTCdatetimeAsString();
//            String jsonData = buildJsonData(identityNumber, loanAmount);
//
//            // Generate hash using Apache Commons Codec
//            String hashString = generateHash(jsonData, timestamp);
//
//            RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json"));
//            Request request = new Request.Builder()
//                    .url(crbConfig.getUrl())
//                    .post(body)
//                    .addHeader("Content-Type", "application/json")
//                    .addHeader("X-METROPOL-REST-API-KEY", crbConfig.getPublicKey())
//                    .addHeader("X-METROPOL-REST-API-HASH", hashString)
//                    .addHeader("X-METROPOL-REST-API-TIMESTAMP", timestamp)
//                    .build();
//
//            logEvent("Request URL: {}", crbConfig.getUrl());
//            logEvent("Request body: {}", jsonData);
//
//            try (Response response = webConfig.okHttpClient().newCall(request).execute())
//            {
//                if (response.isSuccessful() && response.body() != null)
//                {
//                    String responseBody = response.body().string();
//                    logEvent("CRB Response: {}", responseBody);
//                    return responseBody;
//                }
//                else
//                {
//                    logError("CRB request failed with code: {}"+response.code());
//                    if (response.body() != null)
//                    {
//                        logEvent("Error response: {}", response.body().string());
//                    }
//                    return null;
//                }
//            }
//
//        } catch (Exception e)
//        {
//            logError("Error connecting to CRB service", e);
//            return null;
//        }
//    }
//
//    private String buildJsonData(String identityNumber, Integer loanAmount)
//    {
//        JSONObject json = new JSONObject();
//        json.put("report_type", 2);
//        json.put("identity_number", identityNumber);
//        json.put("identity_type", "001");
//        json.put("loan_amount", loanAmount);
//
//        return json.toString();
//    }
//
//    private String generateHash(String jsonData, String timestamp)
//    {
//        String hashInput = crbConfig.getPrivateKey() + jsonData + crbConfig.getPublicKey() + timestamp;
//        logEvent("Hash input: {}", hashInput);
//        return getSHA256Hash(hashInput).toLowerCase();
//    }
//
//    private String getSHA256Hash(String data)
//    {
//        try
//        {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(data.getBytes());
//            // Using Apache Commons Codec Hex
//            return Hex.encodeHexString(hash);
//        } catch (NoSuchAlgorithmException e)
//        {
//            logError("Hashing error", e);
//            return null;
//        }
//    }
//
//    private String getUTCdatetimeAsString()
//    {
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//        return sdf.format(new Date());
//    }
//
//    private TrustManager[] getTrustManager()
//    {
//        return new TrustManager[]{
//                new X509TrustManager()
//                {
//                    public X509Certificate[] getAcceptedIssuers()
//                    {
//                        return null;
//                    }
//
//                    public void checkClientTrusted(X509Certificate[] certs, String t)
//                    {
//                    }
//
//                    public void checkServerTrusted(X509Certificate[] certs, String t)
//                    {
//                    }
//                }
//        };
//    }
//}
