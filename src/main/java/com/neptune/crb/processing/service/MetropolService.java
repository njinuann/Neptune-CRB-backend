package com.neptune.crb.processing.service;

import com.neptune.crb.processing.config.CRBConfig;
import com.neptune.crb.processing.util.HashUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MetropolService
{

    private static final Logger logger = LoggerFactory.getLogger(MetropolService.class);

    private final CRBConfig crbConfig;
    private final RestTemplate restTemplate;

    public MetropolService(CRBConfig crbConfig, RestTemplate restTemplate)
    {
        this.crbConfig = crbConfig;
        this.restTemplate = restTemplate;
    }

    public JSONObject getReport(String identityNumber, int loanAmount)
    {

        String url = crbConfig.getUrl();
        String requestBody = buildRequestBody(identityNumber, loanAmount);
        String timestamp = generateTimestamp();

        String signature = generateSignature(requestBody, timestamp);
        HttpHeaders headers = buildHeaders(signature, timestamp);
        logger.info("requestBody {}", requestBody);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        try
        {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return new JSONObject(response.getBody());

        } catch (Exception e)
        {
            logger.error("Metropol API call failed", e);
            throw new RuntimeException("Metropol API call failed", e);
        }
    }

    private HttpHeaders buildHeaders(String signature, String timestamp)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-METROPOL-REST-API-KEY", crbConfig.getPublicKey());
        headers.set("X-METROPOL-REST-API-HASH", signature);
        headers.set("X-METROPOL-REST-API-TIMESTAMP", timestamp);
        return headers;
    }

    private String generateSignature(String requestBody, String timestamp)
    {

        String hashInput =
                crbConfig.getPrivateKey()
                        + requestBody
                        + crbConfig.getPublicKey()
                        + timestamp;

        return HashUtil.generateSHA256Hash(hashInput).toLowerCase();
    }

    public static String generateTimestamp()
    {
        return java.time.format.DateTimeFormatter
                .ofPattern("yyyyMMddHHmmssSSSSSS")
                .format(java.time.LocalDateTime.now(java.time.Clock.systemUTC()));
    }

    private String buildRequestBody(String identityNumber, int loanAmount)
    {

        return "{"
                + "\"report_type\":" + crbConfig.getReportType() + ","
                + "\"identity_number\":\"" + identityNumber + "\","
                + "\"identity_type\":\"" + crbConfig.getIdentityType() + "\","
                + "\"loan_amount\":" + loanAmount + ","
                + "\"report_reason\":1"
                + "}";
    }
}