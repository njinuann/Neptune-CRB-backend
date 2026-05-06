package com.neptune.crb.processing.service;

import com.neptune.crb.processing.config.CRBConfig;
import com.neptune.crb.processing.config.WebConfig;
import com.neptune.crb.processing.util.HashUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.neptune.crb.processing.util.Logger.logError;
import static com.neptune.crb.processing.util.Logger.logEvent;

@Slf4j
@Service
public class CRBMetropolClient
{

    private final CRBConfig crbConfig;
    private final WebConfig webConfig;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmsss");

    public CRBMetropolClient(CRBConfig crbConfig, WebConfig webConfig)
    {
        this.crbConfig = crbConfig;
        this.webConfig = webConfig;
    }

    @PostConstruct
    public void init()
    {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private OkHttpClient createHttpClient()
    {
        try
        {
            // Create a trust manager that accepts all certificates (for development)
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager()
                    {
                        public X509Certificate[] getAcceptedIssuers()
                        {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType)
                        {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType)
                        {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(crbConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(crbConfig.getReadTimeout(), TimeUnit.MILLISECONDS)
                    .build();
        } catch (Exception e)
        {
            logError("Error creating HTTP client", e);
            return new OkHttpClient.Builder()
                    .connectTimeout(crbConfig.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(crbConfig.getReadTimeout(), TimeUnit.MILLISECONDS)
                    .build();
        }
    }

    public JSONObject queryDelinquency(String identityNumber, int loanAmount)
    {
        long startTime = System.currentTimeMillis();

        try
        {
            String timestamp = generateTimestamp();//getUTCTimestamp();
            String requestBody = buildRequestBody(identityNumber, loanAmount);
            String signature = generateSignature(requestBody, timestamp);


            logEvent("Timestamp ", timestamp);
            logEvent("requestBody ", requestBody);
            logEvent("signature ", signature);

            logEvent("REQUEST BODY: [{}]", requestBody);
            logEvent("REQUEST LENGTH: {}", requestBody.length());

            Request request = new Request.Builder()
                    .url(crbConfig.getUrl())
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json; charset=UTF-8")))
//                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .addHeader("X-METROPOL-REST-API-KEY", crbConfig.getPublicKey())
                    .addHeader("X-METROPOL-REST-API-HASH", signature)
                    .addHeader("X-METROPOL-REST-API-TIMESTAMP", timestamp)
                    .build();


            logEvent("Request to Metropol: {}", requestBody);
            System.out.println("==== REQUEST HEADERS ====");
            request.headers().forEach(header ->
                    System.out.println(header.getFirst() + ": " + header.getSecond())
            );

            try (Response response = createHttpClient().newCall(request).execute())
            {
                long responseTime = System.currentTimeMillis() - startTime;
                logEvent("Metropol API responded in {}ms", responseTime);

                if (!response.isSuccessful())
                {
                    logEvent("Metropol API error: {} - {}" + response.code() + " , " + response.message());
                    JSONObject errorResponse = new JSONObject();
                    errorResponse.put("has_error", true);
                    errorResponse.put("api_code", String.valueOf(response.code()));
                    errorResponse.put("api_code_description", "HTTP Error: " + response.message());
                    return errorResponse;
                }

                String responseBody = response.body().string();
                logEvent("Metropol response: {}", responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                jsonResponse.put("_response_time_ms", responseTime);
                return jsonResponse;

            }
        } catch (Exception e)
        {
            logError("Error calling Metropol API", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("has_error", true);
            errorResponse.put("api_code", "99");
            errorResponse.put("api_code_description", "Service error: " + e.getMessage());
            return errorResponse;
        }
    }

//    private String buildRequestBody(String identityNumber, int loanAmount)
//    {
////        JSONObject json = new JSONObject();
////        json.put("report_type", crbConfig.getReportType());
////        json.put("identity_number", identityNumber);
////        json.put("identity_type", crbConfig.getIdentityType());
////        json.put("loan_amount", loanAmount);
////        return json.toString();
//        Map<String, Object> map = new LinkedHashMap<>();
//        map.put("report_type", crbConfig.getReportType());
//        map.put("identity_number", identityNumber);
//        map.put("identity_type", crbConfig.getIdentityType());
//        map.put("loan_amount", loanAmount);
//
//        return new JSONObject(map).toString();
//    }

private String buildRequestBody(String identityNumber, int loanAmount) {
    //use this tp preserve the order and maintain the signature
        return String.format(
            "{\"report_type\":\"%d\",\"identity_number\":\"%s\",\"identity_type\":\"%s\",\"loan_amount\":%d}",
            crbConfig.getReportType(),
            identityNumber,
            crbConfig.getIdentityType(),
            loanAmount
    );
}

    private String generateSignature(String requestBody, String timestamp) {

        String hashInput =
                crbConfig.getPrivateKey()
                        + requestBody
                        + crbConfig.getPublicKey()
                        + timestamp;

        logEvent("HASH INPUT: {}", hashInput);

        String signature = HashUtil.generateSHA256Hash(hashInput).toLowerCase();

        logEvent("SIGNATURE: {}", signature);

        return signature;
    }
//    private String generateSignature(String requestBody, String timestamp)
//    {
//        String hashInput = crbConfig.getPrivateKey() + requestBody + crbConfig.getPublicKey() + timestamp;
//        logEvent("Hash input length: {}", hashInput.length());
//        return HashUtil.generateSHA256Hash(hashInput);
//    }

    private String getUTCTimestamp()
    {
        return dateFormat.format(new Date());
    }

    public static String generateTimestamp() {
        return java.time.format.DateTimeFormatter
                .ofPattern("yyyyMMddHHmmssSSSSSS")
                .format(java.time.LocalDateTime.now(java.time.Clock.systemUTC()));
    }
}