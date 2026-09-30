package com.neptune.crb.processing.service;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 23-Mar-26
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neptune.crb.processing.config.CRBConfig;
import com.neptune.crb.processing.model.*;
import com.neptune.crb.processing.repository.CRBDelinquencyRepository;
import com.neptune.crb.processing.util.AXWorker;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.neptune.crb.processing.util.Logger.logEvent;

@Slf4j
@Service
public class CRBDelinquencyService {

    private final CRBDelinquencyRepository repository;
    private final MetropolService metropolClient;
    private final AXWorker axWorker;

    private final CRBConfig crbConfig;
    private final PdfExtractionService pdfExtractionService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> inFlightRequests = ConcurrentHashMap.newKeySet();

    private String fingerprint(String id, int amount) {
        return id + "-" + amount;
    }

    public CRBDelinquencyService(MetropolService metropolClient, CRBDelinquencyRepository repository, AXWorker axWorker, CRBConfig crbConfig, PdfExtractionService pdfExtractionService) {
        this.metropolClient = metropolClient;
        this.repository = repository;
        this.axWorker = axWorker;
        this.crbConfig = crbConfig;
        this.pdfExtractionService = pdfExtractionService;
    }

    @Transactional
    public CRBDelinquencyResponseWrapper checkDelinquencyById(CRBDelinquencyRequest request, String userId) {
        logEvent("delinquency-check ", "Delinquency check request - Identity: " + request.getIdentityNumber());
        logEvent("delinquency-check ", "Delinquency check request - Amount: " + request.getLoanAmount());

        CRBDelinquencySearch delinquentCheckRequest = new CRBDelinquencySearch();
        delinquentCheckRequest.setIdentityNumber(request.getIdentityNumber());
        delinquentCheckRequest.setLoanAmount(new BigDecimal(request.getLoanAmount()));
        delinquentCheckRequest.setIdentityType(request.getIdentityType());

        CRBDelinquencyReport report = processAndHandleFallback(delinquentCheckRequest, userId);

        return new CRBDelinquencyResponseWrapper(report, null);
    }

    public CRBDelinquencyResponseWrapper checkDelinquency(DelinquentCheckRequest request, String userId) {

        CreditApplicationData creditApplicationData;
        if (request.getCreditApplicationId().intValue() == 123456) {
            creditApplicationData = new CreditApplicationData();
            creditApplicationData.setIdNumber("770000077");
            creditApplicationData.setLoanAmount(new BigDecimal(5000));
        } else {

            creditApplicationData = repository.fetchLoanDataByCreditAppID(request.getCreditApplicationId());
            if (creditApplicationData == null) {
                throw new RuntimeException(
                        "Credit application not found: "
                                + request.getCreditApplicationId()
                );
            }

        }
        return executeWithMDC(() -> {

            CRBDelinquencySearch search = buildSearch(
                    creditApplicationData.getIdNumber(),
                    creditApplicationData.getLoanAmount(),
                    "001"
            );

            CRBDelinquencyReport report = processAndHandleFallback(search, userId);

            // 🔥 READ PDF HERE
            ReportData reportData = new ReportData();
            String base64 = report.getPdfReport();
            if (!axWorker.isBlank(base64)) {
                reportData.setReferenceNumber(generateTransactionId());
                reportData.setCrbReport(base64);
                reportData.setDocumentName(generateTransactionId() + "-" + request.getCreditApplicationId() + ".pdf");
                reportData.setDocumentType(63L);
            }
//            else {
//                // TODO : Remove this bit to have once live
//                base64 = encodePdfFromResources("crbreport.pdf");
//                reportData.setReferenceNumber(generateTransactionId());
//                reportData.setCrbReport(base64);
//                reportData.setDocumentName(generateTransactionId() + "-" + request.getCreditApplicationId() + ".pdf");
//                reportData.setDocumentType(63L);
//                // report.setPdfReport(base64);
//            }
            report.setPdfReport("");
            return new CRBDelinquencyResponseWrapper(report, reportData);

        }, creditApplicationData.getIdNumber(), request.getCrbVendorType(), request.getCreditApplicationId());
    }

    private CRBDelinquencyReport processAndHandleFallback(CRBDelinquencySearch request, String userId) {
        try {
            CRBDelinquencyReport report = processCRB(request, userId);
            return report;

        } catch (Exception e) {
            log.error("CRB processing failed", e);
            throw new RuntimeException("Unable to process CRB request", e);
        }
    }

    private CRBDelinquencyReport processCRB(CRBDelinquencySearch request, String userId) {

        String transactionId = generateTransactionId();

        log.info("Processing CRB request. txnId={}, identity={}", transactionId, request.getIdentityNumber());
        JSONObject response = safeCall(request.getIdentityNumber(), request.getLoanAmount().intValue());

        log.info("Metropol response: {}", response.toString());
        CRBDelinquencyReport report = mapToReport(response);
        report.setTransactionId(transactionId);
        log.info("Mapped response");

        try {
            saveRecord(request, report, mapper.writeValueAsString(request), response.toString(),
                    userId, LocalDateTime.now(), getResponseTime(response));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return report;
    }

    private JSONObject safeCall(String id, int amount) {

        String key = fingerprint(id, amount);
        System.out.println(key);
        if (!inFlightRequests.add(key)) {
            log.warn("Duplicate request blocked for {}", key);
            throw new RuntimeException("Duplicate request in progress");
        }

        try {
            return metropolClient.getReport(id, amount);
        } finally {
            log.info("key removed {} ", key);
            inFlightRequests.remove(key);
        }
    }

    private String encodePdfFromResources(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource("report/" + fileName);

            try (InputStream inputStream = resource.getInputStream()) {
                byte[] bytes = inputStream.readAllBytes();
                return Base64.getEncoder().encodeToString(bytes);
            }

        } catch (Exception e) {
            log.error("Failed to read PDF from resources", e);
            throw new RuntimeException("PDF processing failed");
        }
    }

    private CRBDelinquencySearch buildSearch(String identity, Number amount, String type) {
        CRBDelinquencySearch search = new CRBDelinquencySearch();
        search.setIdentityNumber(identity);
        search.setLoanAmount(new BigDecimal(amount.toString()));
        search.setIdentityType(type);
        return search;
    }

//    private CRBDelinquencyReport mockedData(CRBDelinquencySearch request) {
//
//        boolean listed = true;
//
//        CRBDelinquencyReport report = new CRBDelinquencyReport();
//        report.setCreditScore("303");
//        report.setDelinquencyCode("005");
//        report.setDelinquencySummary("Historical delinquency True ~ " + getScoreDescription(303));
//        report.setDelinquencyDescription(getDelinquencyCode("005"));
//        report.setHasError(false);
//        report.setIdentityType("National Id");
//        report.setIdentityTypeDescription("National Id");
//        report.setIdentityNumber(request.getIdentityNumber());
//        report.setTransactionId(generateTransactionId());
//
//        report.setIsGuarantor("false");
//        report.setHasFraud("false");
//        report.setListed("yes");
//        report.setListingStatus("listed");
//
//        return report;
//    }

    private CRBDelinquencyReport mapToReport(JSONObject json) {

        String encoding = "base64";

        String apiCode = json.optString("api_code", "");
        String apiCodeDescription = json.optString("api_code_description", "");

        String code = json.optString("delinquency_code", json.optString("delinquencyCode", ""));
        String creditScore = json.optString("credit_score", "0");

        String delinquencySummary = json.optString("delinquency_summary", "");
        String pdfData = json.optString("report", "");

        // ==========================================================
        // PDF FALLBACK
        // ==========================================================
        int score = 0;
        if (!axWorker.isBlank(pdfData) && (axWorker.isBlank(code) || axWorker.isBlank(creditScore))) {

            try {
                CreditScoreData pdfDataResult = pdfExtractionService.extract(pdfData);
                if (pdfDataResult != null) {
                    if (pdfDataResult.getPpi() != null && pdfDataResult.getMetroScore() != null) {
                        if (axWorker.isBlank(code)) {
                            code = pdfDataResult.getPpi();
                        }

                        if (axWorker.isBlank(creditScore)) {
                            creditScore = pdfDataResult.getMetroScore();
                        }

                        if (axWorker.isBlank(delinquencySummary)) {
                            try {
                                score = Integer.parseInt(creditScore);
                            } catch (Exception ignored) {
                            }
                            // delinquencySummary = getDelinquencyDescription(code) + " ~ " + getScoreDescription(score);
                            delinquencySummary = pdfDataResult.getPpi() + " ~ " + pdfDataResult.getPpiInterpretation();
                        }
                    } else {
                        //fallback if pdf doesn't contain data needed indicating non-listed person or new first time borrower
                        creditScore = "0";
                        code = "M0";
                        delinquencySummary = "M0" + " ~ " + "NO DATA FOUND RELEVANT TO SCORE";
                    }
                }

            } catch (Exception e) {

                log.error("Failed extracting PDF report", e);
            }
        }
        try {
            score = Integer.parseInt(axWorker.isBlank(creditScore) ? "0" : creditScore);
        } catch (Exception ignored) {
        }

        String rating = getScoreDescription(score);
        boolean listed = "004".equals(code) || "005".equals(code);

        return new CRBDelinquencyReport(
                apiCode,
                apiCodeDescription,
                code,
                getDelinquencyDescription(code),
                delinquencySummary,
                json.optBoolean("has_error", false),
                json.optString("identity_number", ""),
                json.optString("identity_type", ""),
                getIdentityTypeDescription(json.optString("identity_type", "")),
                json.has("loan_amount") ? json.optLong("loan_amount") : null,
                listed ? "listed" : "not listed",
                json.optString("trx_id", ""),
                creditScore,
                json.optString("is_gurantor", ""),
                json.optString("has_fraud", ""),
                listed ? "yes" : "no",
                rating,
                pdfData,
                axWorker.isBlank(pdfData) ? "" : encoding
        );
    }

    private <T> T executeWithMDC(SupplierWithException<T> action,
                                 Object identity,
                                 Object type,
                                 Object amount) {

        MDC.put("requestId", UUID.randomUUID().toString().substring(0, 8));
        if (identity != null) MDC.put("identityNumber", identity.toString());
        if (type != null) MDC.put("type", type.toString());
        if (amount != null) MDC.put("amount", amount.toString());

        try {
            return action.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            MDC.clear();
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }

    // ================= LOOKUPS =================

    public String getScoreDescription(int score) {
        if (score == 0) {
            return "No Score Data Found";
        }
        if (score >= 725) return "Very Good (Low risk)";
        if (score >= 660) return "Good";
        if (score >= 560) return "Moderate Risk";
        if (score >= 400) return "Very Poor";
        return "High default risk";
    }

    public String getDelinquencyCode(String code) {
        return switch (code) {
            case "001" -> "ID not found";
            case "002" -> "No credit info";
            case "003" -> "No NPA";
            case "004" -> "Currently delinquent";
            case "005" -> "Historical NPA";
            case "M0" -> ("Customer does not currently have information in their Credit Profile");
            case "M1" -> ("Excellent (0–10 days late)");
            case "M2" -> ("Good (11–20 days late)");
            case "M3" -> ("Moderate (21–30 days late)");
            case "M4", "M5", "M6", "M7", "M8", "M9" -> ("High risk payment delays");
            default -> "Unknown";
        };
    }

    private void extractPpiMeaning(String text, CreditScoreData data) {

        if (data.getPpi() == null) return;

        switch (data.getPpi()) {
            case "M0" ->
                    data.setPpiInterpretation("This customer does not currently have information in their Credit Profile");
            case "M1" -> data.setPpiInterpretation("Excellent (0–10 days late)");
            case "M2" -> data.setPpiInterpretation("Good (11–20 days late)");
            case "M3" -> data.setPpiInterpretation("Moderate (21–30 days late)");
            case "M4", "M5", "M6", "M7", "M8", "M9" -> data.setPpiInterpretation("High risk payment delays");
        }
    }

    private String getDelinquencyDescription(String code) {
        log.info("This code for Delinquency {}", code);
        return getDelinquencyCode(code);
    }

    private String getIdentityTypeDescription(String type) {
        return switch (type) {
            case "001" -> "National ID";
            case "002" -> "Passport";
            case "003" -> "Service ID";
            case "004" -> "Alien Registration";
            case "005" -> "Business Registration";
            default -> "Unknown";
        };
    }

    private String generateTransactionId() {
        return "CRB-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();
    }

    private void saveRecord(CRBDelinquencySearch request, CRBDelinquencyReport report,
                            String rawRequest, String rawResponse, String userId,
                            LocalDateTime queryTime, int responseTimeMs) {
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

    private String buildRawRequest(CRBDelinquencySearch request) {
        JSONObject json = new JSONObject();
        json.put("identity_number", request.getIdentityNumber());
        json.put("loan_amount", request.getLoanAmount());
        json.put("identity_type", request.getIdentityType() != null ? request.getIdentityType() : "001");
        return json.toString();
    }

    private int getResponseTime(JSONObject json) {
        return json.has("_response_time_ms") ? json.getInt("_response_time_ms") : 0;
    }
}