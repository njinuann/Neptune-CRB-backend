package com.neptune.crb.processing.util;

import org.springframework.stereotype.Component;

@Component
public class RegexUtil
{
    public static String extractDigitsOnPhoneNo(String phoneNumber)
    {
        return phoneNumber.replaceAll("[^\\d]", "");
    }

    public static String keeNoWithOptionalDecimal(String str)
    {
        return str.replaceAll("[^\\d.]", "").replaceAll("(\\.)(?=.*\\.)", "");
    }

    public static String removeHtmlTags(String str)
    {
        return str.replaceAll("<[^>]*>", "");
    }

    public static String extractEmail(String str)
    {
        return str.replaceAll("[^a-zA-Z0-9@._]", "");
    }

    public static String keepLettersOnly(String str)
    {
        return str.replaceAll("[^a-zA-Z]", "");
    }

    public static String removeWhiteSpaces(String str)
    {
        return str.replaceAll("\\s+", "");
    }

    public static String keepOnlyDigits(String str)
    {
        return str.replaceAll("[^\\d]", "");
    }

    public static boolean isValidInteger(String str)
    {
        return str.matches("-?\\d+");
    }

    public static boolean isDecimalValue(String str)
    {
        return str.matches("[-+]?\\d*\\.?\\d+");
    }

    public static boolean isPositiveNUmber(String str)
    {
        return str.matches("[-+]?[1-9]\\d*|0");
    }

    public static boolean isDecimalOrInteger(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public static boolean isACurrencyNo(String str)
    {
        return str.matches("\\$?\\d{1,3}(,\\d{3})*(\\.\\d{2})?");
    }

    public static boolean isScientificAnnotated(String str)
    {
        return str.matches("[+-]?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");
    }

    public static boolean isFloatingValue(String str)
    {
        return str.matches("[+-]?\\d*(\\.\\d+)?");
    }

    public static boolean hasOneDecimalPoint(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public static boolean isValidAccountNo(String str)
    {
        return str.matches("^\\d{6,12}$");
    }

    public static boolean isIBanNo(String str)
    {
        return str.matches("^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$");
    }

    public static boolean isValidSwiftCode(String str)
    {
        return str.matches("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    }

    public static boolean isValidTxnDate(String str)
    {
        return str.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    public static boolean isValidCreditCardNo(String str)
    {
        return str.matches("^\\d{4}-?\\d{4}-?\\d{4}-?\\d{4}$");
    }

    public static boolean isValidAmount(String amount) {
        return amount != null && amount.matches("^\\d{1,3}(,\\d{3})*(\\.\\d{2})?$");
    }
    public static boolean isValidMpesaCode(String code) {
        return code != null && code.matches("^[A-Z0-9]{10}$"); // Adjust if format varies
    }
    public static boolean isValidKenyanPhone(String phone) {
        return phone != null && phone.matches("^(\\+2547\\d{8}|07\\d{8})$");
    }
    public static String sanitizeAmount(String amount) {
        if (amount == null) return null;
        String cleaned = amount.replaceAll("[^\\d.]", "");
        return cleaned.replaceAll("(\\.)(?=.*\\.)", ""); // keep only last dot
    }
    public static String normalizeAmount(String amount) {
        if (amount == null) return null;
        return amount.replaceAll("[^\\d.]", "");
    }
}
