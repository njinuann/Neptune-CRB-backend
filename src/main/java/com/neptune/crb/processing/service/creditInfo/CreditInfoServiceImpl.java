package com.neptune.crb.processing.service.creditInfo;


import com.neptune.crb.processing.config.CreditInfoConfig;
import com.neptune.crb.processing.model.creditInfo.CompanyCreditRequest;
import com.neptune.crb.processing.model.creditInfo.IndividualCreditRequest;
import com.neptune.crb.processing.model.creditInfo.IprsBeginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditInfoServiceImpl implements CreditInfoService {

    private static final String INDIVIDUAL_STRATEGY =
            "/api/strategies/54e60d15-d8e6-4f09-930a-604e3bf11c00";

    private static final String COMPANY_STRATEGY =
            "/api/strategies/55df15e5-b7ba-4012-bdf5-71ee40bba346";

    private static final String IPRS_STRATEGY =
            "/api/strategies/6170594f-3633-4b8f-bfee-2b89a39efa6a";

    private final CreditInfoConfig config;
    private final RestTemplate restTemplate;

    // =========================
    // INDIVIDUAL CREDIT REPORT
    // =========================
    @Override
    public Object beginIndividualCreditReport(IndividualCreditRequest request) {

        Map<String, Object> connectorRequest = buildIndividualConnectorRequest(request);

        Map<String, Object> payload = new HashMap<>();
        payload.put("ConnectorRequest", connectorRequest);
        payload.put("Consent", request.getConsent());

        return executePost(INDIVIDUAL_STRATEGY, payload);
    }

    // =========================
    // COMPANY CREDIT REPORT
    // =========================
    @Override
    public Object beginCompanyCreditReport(CompanyCreditRequest request) {

        Map<String, Object> connectorRequest = new HashMap<>();
        connectorRequest.put("RegistrationNumber", request.getRegistrationNumber());

        Map<String, Object> payload = new HashMap<>();
        payload.put("ConnectorRequest", connectorRequest);
        payload.put("Consent", request.getConsent());

        return executePost(COMPANY_STRATEGY, payload);
    }

    // =========================
    // IPRS BEGIN QUERY
    // =========================
    @Override
    public Object beginIprsQuery(IprsBeginRequest request) {

        Map<String, Object> customFields = new HashMap<>();
        customFields.put("IdentifierType", request.getIdentifierType().getValue());
        customFields.put("IdentifierNumber", request.getIdentifierNumber());

        Map<String, Object> payload = new HashMap<>();
        payload.put("CustomFields", customFields);
        payload.put("Consent", request.getConsent());

        return executePost(IPRS_STRATEGY, payload);
    }

    // =========================
    // END QUERY (JSON)
    // =========================
    @Override
    public String endQuery(String token) {

        String url = buildStrategyUrl(token);

        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            log.error("EndQuery failed. status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());

            throw new RuntimeException("End query failed: " + ex.getResponseBodyAsString(), ex);
        }
    }

    // =========================
    // END QUERY (PDF)
    // =========================
    @Override
    public byte[] endQueryPdf(String token) {

        String url = buildStrategyUrl(token);

        HttpEntity<Void> entity =
                new HttpEntity<>(createHeaders(MediaType.APPLICATION_PDF));

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("PDF fetch failed: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (HttpStatusCodeException ex) {
            log.error("PDF fetch failed. status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());

            throw new RuntimeException("PDF generation failed", ex);
        }
    }

    // =========================
    // CORE HTTP EXECUTION
    // =========================
    private Object executePost(String strategyPath, Map<String, Object> payload) {

        String url = config.getBaseUrl() + strategyPath;

        log.info("Calling CreditInfo API: {}", url);
        log.debug("Payload: {}", payload);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(payload, createHeaders(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getBody();

        } catch (HttpStatusCodeException ex) {

            log.error("CreditInfo error. status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());

            throw new RuntimeException(
                    "CreditInfo request failed: " + ex.getResponseBodyAsString(),
                    ex
            );
        }
    }

    // =========================
    // HEADERS
    // =========================
    private HttpHeaders createHeaders(MediaType mediaType) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(
                config.getUsername(),
                config.getPassword()
        );

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(mediaType));

        return headers;
    }

    // =========================
    // INDIVIDUAL BUILDER
    // =========================
    private Map<String, Object> buildIndividualConnectorRequest(IndividualCreditRequest request) {

        Map<String, Object> connectorRequest = new HashMap<>();

        if (request.getNationalId() != null) {
            connectorRequest.put("NationalId", request.getNationalId());
        } else if (request.getAlienId() != null) {
            connectorRequest.put("AlienId", request.getAlienId());
        } else if (request.getServiceId() != null) {
            connectorRequest.put("ServiceId", request.getServiceId());
        } else if (request.getPassportNumber() != null) {
            connectorRequest.put("PassportNumber", request.getPassportNumber());
        } else {
            throw new IllegalArgumentException(
                    "One of NationalId, AlienId, ServiceId or PassportNumber is required"
            );
        }

        return connectorRequest;
    }

    // =========================
    // STRATEGY URL
    // =========================
    private String buildStrategyUrl(String token) {
        return config.getBaseUrl() + "/api/strategies/" + token;
    }
}
