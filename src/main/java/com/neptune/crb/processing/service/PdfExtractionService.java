package com.neptune.crb.processing.service;

import java.util.regex.Pattern;


import com.neptune.crb.processing.model.CreditScoreData;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@Slf4j
//@Service
//public class PdfExtractionService {
//
//    private static final Pattern PRIMARY_METRO_PATTERN = Pattern.compile(
//            "(\\d+)\\s+(M\\d+)\\s+(\\d+\\s*%)",
//            Pattern.CASE_INSENSITIVE
//    );
//
//    private static final Pattern ALT_METRO_PATTERN = Pattern.compile(
//            "Metro-Score.*?(\\d+)\\s+(M\\d+)\\s+(\\d+\\s*%)",
//            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//    );
//
//    public CreditScoreData extract(String base64Pdf) {
//
//        try {
//            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);
//
//            String rawText;
//            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
//                PDFTextStripper stripper = new PDFTextStripper();
//                rawText = stripper.getText(document);
//            }
//
//            // 1. Normalize text (CRITICAL for PDF extraction consistency)
//            String text = normalize(rawText);
//
//            log.info("Normalized PDF Text Sample:\n{}",
//                    text.substring(0, Math.min(2000, text.length()))
//            );
//
//            CreditScoreData data = new CreditScoreData();
//
//            // 2. Run extraction pipeline
//            ExtractionResult result = extractWithConfidence(text);
//
//            if (result != null && result.confidence > 0) {
//                data.setMetroScore(result.metroScore);
//                data.setPpi(result.ppi);
//                data.setProbabilityOfDefault(result.pd);
//
//                log.info("Extraction SUCCESS | confidence={} | data={}",
//                        result.confidence, data);
//            } else {
//                log.warn("Extraction FAILED: No valid Metro Score pattern found");
//            }
//
//            return data;
//
//        } catch (Exception e) {
//            log.error("PDF extraction failed", e);
//            throw new RuntimeException("Failed to extract PDF fields", e);
//        }
//    }
//
//    // =========================
//    // NORMALIZATION
//    // =========================
//    private String normalize(String text) {
//        return text
//                .replace("\u00A0", " ")     // NBSP fix
//                .replaceAll("\\s+", " ")    // collapse whitespace
//                .trim();
//    }
//
//    // =========================
//    // EXTRACTION ENGINE
//    // =========================
//    private ExtractionResult extractWithConfidence(String text) {
//
//        List<ExtractionResult> candidates = new ArrayList<>();
//
//        // Strategy 1: Primary pattern (clean structure)
//        candidates.addAll(findMatches(text, PRIMARY_METRO_PATTERN, 0.9));
//
//        // Strategy 2: Context-aware fallback
//        candidates.addAll(findMatches(text, ALT_METRO_PATTERN, 0.7));
//
//        // Strategy 3: Loose scan fallback (last resort)
//        candidates.addAll(scanLooseTriplets(text));
//
//        // Select best candidate
//        return candidates.stream()
//                .max((a, b) -> Double.compare(a.confidence, b.confidence))
//                .orElse(null);
//    }
//
//    // =========================
//    // PATTERN MATCHING
//    // =========================
//    private List<ExtractionResult> findMatches(String text, Pattern pattern, double confidence) {
//
//        List<ExtractionResult> results = new ArrayList<>();
//        Matcher matcher = pattern.matcher(text);
//
//        while (matcher.find()) {
//
//            ExtractionResult r = new ExtractionResult();
//            r.metroScore = matcher.group(1);
//            r.ppi = matcher.group(2);
//            r.pd = matcher.group(3);
//            r.confidence = confidence;
//
//            log.debug("Pattern match found | score={} ppi={} pd={}",
//                    r.metroScore, r.ppi, r.pd
//            );
//
//            results.add(r);
//        }
//
//        return results;
//    }
//
//    // =========================
//    // FALLBACK STRATEGY
//    // =========================
//    private List<ExtractionResult> scanLooseTriplets(String text) {
//
//        List<ExtractionResult> results = new ArrayList<>();
//
//        Pattern loose = Pattern.compile(
//                "(\\d+)\\s+(M\\d+)\\s+(\\d+\\s*%)"
//        );
//
//        Matcher matcher = loose.matcher(text);
//
//        while (matcher.find()) {
//
//            ExtractionResult r = new ExtractionResult();
//            r.metroScore = matcher.group(1);
//            r.ppi = matcher.group(2);
//            r.pd = matcher.group(3);
//
//            // Lower confidence because context is unknown
//            r.confidence = 0.5;
//
//            results.add(r);
//        }
//
//        return results;
//    }
//
//    // =========================
//    // INTERNAL MODEL
//    // =========================
//    private static class ExtractionResult {
//        String metroScore;
//        String ppi;
//        String pd;
//        double confidence;
//    }
//}
@Slf4j
@Service
public class PdfExtractionService {

    // =========================
    // ENTRY POINT
    // =========================
    public CreditScoreData extract(String base64Pdf) {

        try {
            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);

            String text;
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(document);
            }

            text = normalize(text);
            CreditScoreData data = new CreditScoreData();

            // =========================
            // CORE METRICS
            // =========================
            extractMetroBlock(text, data);
            extractPpiMeaning(text, data);

            // =========================
            // IDENTITY BLOCK
            // =========================
            data.setCustomerName(extractFirst(text, "Reported Names?:\\s*(.+)"));
            data.setRegistrationNumber(extractFirst(text, "Company Reg No:\\s*([A-Z0-9/\\-]+)"));

            data.setReportDate(extractFirst(text, "Report Date\\s*([0-9\\-: ]+)"));
            data.setReferenceNumber(extractFirst(text, "Reference Number\\s*([A-Z0-9\\-]+)"));
            data.setRequestingInstitution(extractFirst(text, "Requested By\\s*(.+?)\\s+Report Date"));

            // =========================
            // CREDIT BEHAVIOR TABLE
            // =========================
            extractCreditBehavior(text, data);

            // =========================
            // DERIVED FIELDS
            // =========================
            enrichDerivedFields(data);

            log.info("FINAL EXTRACTED PROFILE = {}", data);

            return data;

        } catch (Exception e) {
            log.error("PDF extraction failed", e);
            throw new RuntimeException(e);
        }
    }

    // =========================
    // NORMALIZATION
    // =========================
    private String normalize(String text) {
        return text
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // =========================
    // METRO BLOCK
    // =========================
    private void extractMetroBlock(String text, CreditScoreData data) {

        Pattern p = Pattern.compile("(\\d+)\\s+(M\\d+)\\s+(\\d+\\s*%)");

        Matcher m = p.matcher(text);

        if (m.find()) {
            data.setMetroScore(m.group(1));
            data.setPpi(m.group(2));
            data.setProbabilityOfDefault(m.group(3));
        }
        if(data.getMetroScore()==null){
            extractMetroScore(text,data);
        }
        if(data.getPpi()==null){
            extractPpi(text,data);
        }
        if(data.getProbabilityOfDefault()==null){
            extractProbabilityOfDefault(text,data);
        }
    }

    private void extractMetroScore(String text, CreditScoreData data) {
//        Pattern p = Pattern.compile("Metro-Score\\s*[©]?\\s*(\\d+)",
//                Pattern.CASE_INSENSITIVE);
        Pattern p =    Pattern.compile(
                "Metro-Score\\s*[©]?\\s*(\\d+)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher m = p.matcher(text);

        if (m.find()) {
            data.setMetroScore(m.group(1));
        }
    }
    private void extractPpi(String text, CreditScoreData data) {

//        Pattern p = Pattern.compile("PPI\\s*[©]?\\s*(M\\d+)",
//                Pattern.CASE_INSENSITIVE);
        Pattern p =  Pattern.compile(
                "PPI\\s*[©]?\\s*(M\\d+)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher m = p.matcher(text);

        if (m.find()) {
            data.setPpi(m.group(1));
        }
    }
    private void extractProbabilityOfDefault(String text, CreditScoreData data) {

        Pattern p = Pattern.compile(
                "Probability\\s+of\\s+Default\\s*(\\d+\\s*%)",
                Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(text);

        if (m.find()) {
            data.setProbabilityOfDefault(m.group(1));
        }
    }
    // =========================
    // PPI INTERPRETATION
    // =========================
    private void extractPpiMeaning(String text, CreditScoreData data) {

        if (data.getPpi() == null) return;

        switch (data.getPpi()) {
            case "M1" -> data.setPpiInterpretation("Excellent (0–10 days late)");
            case "M2" -> data.setPpiInterpretation("Good (11–20 days late)");
            case "M3" -> data.setPpiInterpretation("Moderate (21–30 days late)");
            case "M4", "M5", "M6", "M7", "M8", "M9" -> data.setPpiInterpretation("High risk payment delays");
        }
    }

    // =========================
    // CREDIT BEHAVIOR TABLE
    // =========================
    private void extractCreditBehavior(String text, CreditScoreData data) {

        Pattern rowPattern = Pattern.compile("Bank\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);

        Matcher m = rowPattern.matcher(text);
        int total = 0, npl = 0, defaultHist = 0, clean = 0;

        while (m.find()) {
            total += safeInt(m.group(1));
            npl += safeInt(m.group(2));
            defaultHist += safeInt(m.group(3));
            clean += safeInt(m.group(4));
        }

        data.setTotalAccounts(total);
        data.setNonPerformingAccounts(npl);
        data.setPerformingWithDefaultHistory(defaultHist);
        data.setPerformingCleanAccounts(clean);
    }

    // =========================
    // DERIVED FIELDS
    // =========================
    private void enrichDerivedFields(CreditScoreData data) {

        // Risk Grade
        int score = safeInt(data.getMetroScore());

        if (score >= 70) data.setRiskGrade("LOW");
        else if (score >= 50) data.setRiskGrade("MEDIUM");
        else data.setRiskGrade("HIGH");
    }

    // =========================
    // HELPERS
    // =========================
    private String extractFirst(String text, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    private int safeInt(String val) {
        try {
            if (val == null) return 0;
            return Integer.parseInt(val.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}