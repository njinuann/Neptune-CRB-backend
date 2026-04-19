package com.neptune.crb.processing.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.neptune.crb.processing.config.SignatureUtils;
import com.neptune.crb.processing.model.License;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.Arrays;

import java.nio.file.Files;
import java.nio.file.Paths;

public class LicenseGenerator {

    private String privateKeyPath;
    private String publicKeyPath;
    private String outputLicensePath;

    public LicenseGenerator(String privateKeyPath, String publicKeyPath, String outputLicensePath) {
        this.privateKeyPath = privateKeyPath;
        this.publicKeyPath = publicKeyPath;
        this.outputLicensePath = outputLicensePath;
    }

    public void generateLicense(String customerName, String duration, int maxUsers,
                                String[] features, String version) throws Exception {

        // Load private key from file
        System.out.println(new File(privateKeyPath).getAbsolutePath());
        String privateKeyBase64 = new String(Files.readAllBytes(Paths.get(privateKeyPath))).trim();
        PrivateKey privateKey = KeyUtils.base64ToPrivateKey(privateKeyBase64);

        // Load public key from file
        String publicKeyBase64 = new String(Files.readAllBytes(Paths.get(publicKeyPath))).trim();
        PublicKey publicKey = KeyUtils.base64ToPublicKey(publicKeyBase64);

        // Create license
        License license = new License();
        license.setLicenseId("CRB-LIC-" + System.currentTimeMillis());
        license.setCustomerName(customerName);
        license.setIssueDate(LocalDate.now());

        // Set expiration based on duration
        if ("perpetual".equalsIgnoreCase(duration)) {
            license.setPerpetual(true);
            license.setExpirationDate(LocalDate.now().plusYears(100)); // Far future date
        } else {
            int years = Integer.parseInt(duration);
            license.setPerpetual(false);
            license.setExpirationDate(LocalDate.now().plusYears(years));
        }

        license.setFeatures(Arrays.asList(features));
        license.setMaxUsers(maxUsers);
        license.setVersion(version);

        // Add public key to license (optional - can be loaded from separate file)
        license.setPublicKey(publicKeyBase64);

        // Create signable data and sign it
        String signableData = license.toSignableString();
        String signature = SignatureUtils.sign(signableData, privateKey);
        license.setSignature(signature);

        // Save license to file
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputLicensePath), license);

        System.out.println("✓ License generated successfully!");
        System.out.println("✓ File: " + outputLicensePath);
        System.out.println("✓ License Type: " + (license.isPerpetual() ? "PERPETUAL" : "TERM"));
        System.out.println("✓ Expiration: " + license.getExpirationDate());
        System.out.println("✓ Features: " + String.join(", ", features));
        System.out.println("✓ Max Users: " + maxUsers);
    }

    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Usage: LicenseGenerator <customerName> <duration> <maxUsers> <features> <version> <outputFile>");
            System.out.println("Example: LicenseGenerator \"CRB Customer\" 5 50 \"FEAT1,FEAT2,FEAT3\" \"2.0\" license.lic");
            System.out.println("Example: LicenseGenerator \"CRB Customer\" perpetual 100 \"ALL_FEATURES\" \"ENTERPRISE\" enterprise.lic");
            System.out.println("");
            System.out.println("Required configuration files:");
            System.out.println("  - private.key: Contains the private key for signing");
            System.out.println("  - public.key: Contains the public key for verification");
            return;
        }

        String customerName = args[0];
        String duration = args[1];
        int maxUsers = Integer.parseInt(args[2]);
        String[] features = args[3].split(",");
        String version = args[4];
        String outputFile = args[5];

        try {
            LicenseGenerator generator = new LicenseGenerator(
                    "private.key",
                    "public.key",
                    outputFile
            );

            generator.generateLicense(customerName, duration, maxUsers, features, version);

        } catch (Exception e) {
            System.err.println("Error generating license: " + e.getMessage());
            e.printStackTrace();
        }
    }
}