package com.neptune.crb.processing.service;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 23-Mar-26
 */

import com.neptune.crb.processing.model.*;
import com.neptune.crb.processing.repository.CRBDelinquencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.neptune.crb.processing.util.Logger.logEvent;

@Slf4j
@Service
public class CRBDelinquencyService
{

    private final CRBMetropolClient metropolClient;
    private final CRBDelinquencyRepository repository;

    public CRBDelinquencyService(CRBMetropolClient metropolClient, CRBDelinquencyRepository repository)
    {
        this.metropolClient = metropolClient;
        this.repository = repository;
    }

    @Transactional
    public CRBDelinquencyReport checkDelinquencyById(CRBDelinquencyRequest request, String userId)
    {
        logEvent("delinquency-check ", "Delinquency check request - Identity: " +
                request.getIdentityNumber(), request.getLoanAmount());
        logEvent("delinquency-check ", "Delinquency check request -   Amount: %s" +
                request.getLoanAmount());

        CRBDelinquencySearch delinquentCheckRequest = new CRBDelinquencySearch();
        delinquentCheckRequest.setIdentityNumber(request.getIdentityNumber());
        delinquentCheckRequest.setLoanAmount(new BigDecimal(request.getLoanAmount()));
        delinquentCheckRequest.setIdentityType(request.getIdentityType());

        CRBDelinquencyReport report = processCRB(delinquentCheckRequest, userId);
        /* Mock response Data */
        if (report.getHasError())
        {
            return mockedData(delinquentCheckRequest);
        }
        /* end of sMock response Data */
        return report;
    }

    @Transactional
    public CRBDelinquencyReport checkDelinquency(DelinquentCheckRequest delinquentCheckRequest, String userId)
    {
        //fetch id and amount from credit app
        CRBDelinquencySearch request = new CRBDelinquencySearch();

        /* Mock response Data */
        request.setIdentityNumber("550000055");
        request.setLoanAmount(new BigDecimal(50000));
        /* Mock response Data */

        logEvent("delinquency-check ", "Delinquency check request - Identity: " +
                request.getIdentityNumber(), request.getLoanAmount());
        logEvent("delinquency-check ", "Delinquency check request -   Amount: %s" +
                request.getLoanAmount());

        CRBDelinquencyReport report = processCRB(request, userId);
        /* Mock response Data */
        if (report.getHasError())
        {
            return mockedData(request);
        }
        /* end of sMock response Data */


        return report;
    }

    private CRBDelinquencyReport mockedData(CRBDelinquencySearch request)
    {
        CRBDelinquencyReport crbDelinquencyReport = new CRBDelinquencyReport();
        crbDelinquencyReport.setCreditScore("303");
        crbDelinquencyReport.setDelinquencyCode("005");
        crbDelinquencyReport.setDelinquencySummary("Historical delinquency True ~ " + getScoreDescription(303));
        crbDelinquencyReport.setDelinquencyDescription(getDelinquencyCode("005"));
        crbDelinquencyReport.setHasError(false);
        crbDelinquencyReport.setIdentityType("National Id");
        crbDelinquencyReport.setIdentityTypeDescription("National Id");
        crbDelinquencyReport.setIdentityNumber(request.getIdentityNumber());
        crbDelinquencyReport.setTransactionId("e9e0991e-d35c-4884-a793-dadf2a52cf41");

        crbDelinquencyReport.setIsGuarantor("false");
        crbDelinquencyReport.setHasFraud("false");
        crbDelinquencyReport.setIsListed(crbDelinquencyReport.getDelinquencyCode().contains("005") || crbDelinquencyReport.getDelinquencyCode().contains("004"));
        crbDelinquencyReport.setListingStatus(crbDelinquencyReport.getIsListed() ? "listed" : "not listed");
        // delinquencyReport.add(crbDelinquencyReport);


        return crbDelinquencyReport;
    }

    public String getScoreDescription(int score)
    {

        if (score >= 725)
        {
            return "Very Good (Low risk, high chance of loan approval)";
        }
        else if (score >= 660)
        {
            return "Good (Most common)";
        }
        else if (score >= 560)
        {
            return "Fair/Moderate Risk (High debt, late payments)";
        }
        else if (score >= 400)
        {
            return "Very Poor (Missed payments, defaults)";
        }
        else
        {
            return "High default risk/Defaulter";
        }
    }

    private CRBDelinquencyReport processCRB(CRBDelinquencySearch request, String userId)
    {
        /* start process heere here */
        String transactionId = generateTransactionId();
        LocalDateTime queryTime = LocalDateTime.now();

        logEvent("Processing delinquency check - Transaction: {}, Identity: {}",
                transactionId, request.getIdentityNumber());

        // Prepare request data for logging
        String rawRequest = buildRawRequest(request);

        // Call Metropol API
        JSONObject metropolResponse = metropolClient.queryDelinquency(
                request.getIdentityNumber(),
                Integer.parseInt(request.getLoanAmount().toPlainString())
        );

        // Map response to report
        CRBDelinquencyReport report = mapToReport(metropolResponse);
        report.setTransactionId(transactionId);

        // Save record
        saveRecord(request, report, rawRequest, metropolResponse.toString(),
                userId, queryTime, getResponseTime(metropolResponse));
        return report;
    }

    public String getDelinquencyCode(String code)
    {

        return switch (code)
        {
            case "001" -> "ID number not found in the database ";
            case "002" -> "ID number has been found but it has no credit information";
            case "003" -> "ID Number has credit info with No NPA ";
            case "004" -> "Currently delinquent";
            case "005" -> "ID Number has credit information with at least one Historical NPA ";
            default -> "Code not defined";
        };
    }

    private CRBDelinquencyReport mapToReport(JSONObject json)
    {
        String delinquencyCode =  json.optString("delinquency_code", json.optString("delinquencyCode", ""));

        return new CRBDelinquencyReport(
                json.optString("api_code", ""),
                json.optString("api_code_description", ""),
                delinquencyCode, getDelinquencyDescription(delinquencyCode),
                json.optString("delinquency_summary", ""),
                json.optBoolean("has_error", false),
                json.optString("identity_number", ""),
                json.optString("identity_type", ""),
                getIdentityTypeDescription(json.optString("identity_type", "")),
                json.has("loan_amount") ? json.getLong("loan_amount") : null,
                "004".equals(delinquencyCode) || "005".equals(delinquencyCode) ? "listed" : "not listed",
                json.has("trx_id") ? json.getString("trx_id") : "",
                json.has("credit_score") ? json.getString("credit_score") : "",
                json.has("is_gurantor") ? json.getString("is_gurantor") : "",
                json.has("has_fraud") ? json.getString("has_fraud") : "",
                "004".equals(delinquencyCode) || "005".equals(delinquencyCode)

        );
    }

    private void saveRecord(CRBDelinquencySearch request, CRBDelinquencyReport report,
                            String rawRequest, String rawResponse, String userId,
                            LocalDateTime queryTime, int responseTimeMs)
    {
        CRBDelinquencyRecord record = new CRBDelinquencyRecord();
        record.setTransactionId(report.getTransactionId());
        record.setIdentityNumber(request.getIdentityNumber());
        record.setIdentityType(request.getIdentityType() != null ? request.getIdentityType() : "001");
        record.setLoanAmount(Integer.parseInt(request.getLoanAmount().toPlainString()));
        record.setApiCode(report.getApiCode());
        record.setApiCodeDescription(report.getApiCodeDescription());
        record.setDelinquencyCode(report.getDelinquencyCode());
        record.setDelinquencyDescription(report.getDelinquencyDescription());
        record.setDelinquencySummary(report.getDelinquencySummary());
        record.setListingStatus(report.getListingStatus());
        record.setHasError(report.getHasError());
        record.setRawRequest(rawRequest);
        record.setRawResponse(rawResponse);
        record.setUserId(userId);
        record.setQueryTime(queryTime);
        record.setResponseTimeMs(responseTimeMs);

        repository.save(record);
    }

    private String buildRawRequest(CRBDelinquencySearch request)
    {
        JSONObject json = new JSONObject();
        json.put("identity_number", request.getIdentityNumber());
        json.put("loan_amount", request.getLoanAmount());
        json.put("identity_type", request.getIdentityType() != null ? request.getIdentityType() : "001");
        return json.toString();
    }

    private int getResponseTime(JSONObject json)
    {
        return json.has("_response_time_ms") ? json.getInt("_response_time_ms") : 0;
    }

    private String generateTransactionId()
    {
        return "CRB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String getDelinquencyDescription(String code)
    {
        if (code == null || code.isEmpty()) return "";

        return switch (code)
        {
            case "001" -> "ID number has not been found in the database";
            case "002" -> "ID number has been found but it has no credit information";
            case "003" -> "ID Number has credit info with No NPA";
            case "004" -> "Currently delinquent";
            case "005" -> "ID Number has credit information with at least one Historical NPA";
            default -> "Unknown status code: " + code;
        };
    }

    private String getIdentityTypeDescription(String type)
    {
        if (type == null || type.isEmpty()) return "";

        return switch (type)
        {
            case "001" -> "National ID";
            case "002" -> "Passport";
            case "003" -> "Service ID";
            case "004" -> "Alien Registration";
            case "005" -> "Company/Business Registration";
            default -> "Unknown Type";
        };
    }
}