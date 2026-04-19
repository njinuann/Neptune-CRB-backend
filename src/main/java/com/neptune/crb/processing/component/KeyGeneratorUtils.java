package com.neptune.crb.processing.component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyGeneratorUtils {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public static String keyToBase64(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static PublicKey base64ToPublicKey(String base64Key) throws GeneralSecurityException {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key.trim());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new GeneralSecurityException("Failed to decode public key: " + e.getMessage(), e);
        }
    }

    public static PrivateKey base64ToPrivateKey(String base64Key) throws GeneralSecurityException {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key.trim());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new GeneralSecurityException("Failed to decode private key: " + e.getMessage(), e);
        }
    }

    public static void generateAndSaveKeyPair(String customerName) throws Exception {
        // Generate key pair
        KeyPair keyPair = generateKeyPair();

        // Create customer-specific filenames
        String baseFilename = customerName.toLowerCase().replaceAll("[^a-z0-9]", "_");
        String privateKeyFile = baseFilename + "_private.key";
        String publicKeyFile = baseFilename + "_public.key";

        // Save keys to files with proper Base64 encoding
        saveKeyToFile(keyPair.getPrivate(), privateKeyFile, "PRIVATE");
        saveKeyToFile(keyPair.getPublic(), publicKeyFile, "PUBLIC");

        System.out.println("✓ Key pair generated for: " + customerName);
        System.out.println("✓ Private key: " + privateKeyFile);
        System.out.println("✓ Public key: " + publicKeyFile);
        System.out.println("✓ Keep the private key SECURE and NEVER distribute it!");

        // Display public key for application.properties
        System.out.println("\n=== PUBLIC KEY FOR application.properties ===");
        System.out.println("license.public.key=" + keyToBase64(keyPair.getPublic()));
    }

    private static void saveKeyToFile(Key key, String filename, String keyType) throws Exception {
        String keyBase64 = keyToBase64(key);
        // Add header for better readability
        String keyContent = "-----BEGIN " + keyType + " KEY-----\n" +
                keyBase64.replaceAll("(.{64})", "$1\n") +
                "\n-----END " + keyType + " KEY-----";

        Files.write(Paths.get(filename), keyContent.getBytes());
    }

    public static String readKeyFromFile(String filename) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        // Extract Base64 content between headers
        content = content.replaceAll("-----BEGIN (.*) KEY-----", "")
                .replaceAll("-----END (.*) KEY-----", "")
                .replaceAll("\\s+", ""); // Remove all whitespace
        return content.trim();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No arguments provided. Using default values for Fortune Sacco...");
            String[] defaultArgs = {
                    "Fortune Sacco",
                    "perpetual",
                    "200",
                    "DATA_ANALYTICS,REPORTING,API_ACCESS,ADVANCED_SECURITY,USER_MANAGEMENT,LOAN_MANAGEMENT,SAVINGS_MANAGEMENT",
                    "wakulima_sacco_private.key",
                    "src/main/resources/Fortune.lic"
            };
            args = defaultArgs;
        }

        if (args.length == 0) {
            System.out.println("Usage: java KeyGeneratorUtils <customerName>");
            System.out.println("Example: java KeyGeneratorUtils \"Fortune_Sacco\"");
            return;
        }

        String customerName = args[0];

        try {
            generateAndSaveKeyPair(customerName);
        } catch (Exception e) {
            System.err.println("Error generating keys: " + e.getMessage());
            e.printStackTrace();
        }
    }
}