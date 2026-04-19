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

public class CustomerLicenseGenerator {

    private String privateKeyPath;
    private String publicKeyPath;

    public CustomerLicenseGenerator(String privateKeyPath, String publicKeyPath) {
        this.privateKeyPath = privateKeyPath;
        this.publicKeyPath = publicKeyPath;
    }

    public void generateCustomerLicense(String customerName, String licenseType,
                                        int durationYears, int maxUsers,
                                        String[] features, String outputPath) throws Exception {

        // Load private key using the proper method
        String privateKeyBase64 = KeyGeneratorUtils.readKeyFromFile(privateKeyPath);
        PrivateKey privateKey = KeyGeneratorUtils.base64ToPrivateKey(privateKeyBase64);

        // Load public key using the proper method
        String publicKeyBase64 = KeyGeneratorUtils.readKeyFromFile(publicKeyPath);
        PublicKey publicKey = KeyGeneratorUtils.base64ToPublicKey(publicKeyBase64);

        // Create license
        License license = new License();
        license.setLicenseId("CRB-" + customerName.toUpperCase().replaceAll("[^A-Z0-9]", "_") + "-" + System.currentTimeMillis());
        license.setCustomerName(customerName);
        license.setIssueDate(LocalDate.now());

        // Set license type and expiration
        boolean isPerpetual = "perpetual".equalsIgnoreCase(licenseType);
        license.setPerpetual(isPerpetual);

        if (isPerpetual) {
            license.setExpirationDate(LocalDate.now().plusYears(100));
        } else {
            license.setExpirationDate(LocalDate.now().plusYears(durationYears));
        }

        license.setFeatures(Arrays.asList(features));
        license.setMaxUsers(maxUsers);
        license.setVersion("CRB-" + licenseType.toUpperCase() + "-2.0");

        // Add public key to license
        license.setPublicKey(publicKeyBase64);

        // Create signable data and sign it
        String signableData = license.toSignableString();
        String signature = SignatureUtils.sign(signableData, privateKey);
        license.setSignature(signature);

        // Save license to file
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), license);

        System.out.println("✓ Customer license generated successfully!");
        System.out.println("✓ Customer: " + customerName);
        System.out.println("✓ License Type: " + (isPerpetual ? "PERPETUAL" : durationYears + " YEARS"));
        System.out.println("✓ Max Users: " + maxUsers);
        System.out.println("✓ Features: " + String.join(", ", features));
        System.out.println("✓ Output: " + outputPath);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No arguments provided. Using default values for Fortune Sacco...");
            String[] defaultArgs = {
                    "Fortune Sacco",
                    "1",
                    "200",
                    "DATA_ANALYTICS,REPORTING,API_ACCESS,ADVANCED_SECURITY,USER_MANAGEMENT",
//                    "wakulima_sacco_private.key",
                    "crb_private.key",
                    "src/main/resources/fortune_sacco.lic"
            };
            args = defaultArgs;
        }
        System.out.println(Arrays.toString(args));
        if (args.length < 6) {
            System.out.println("Usage: CustomerLicenseGenerator <customerName> <perpetual|years> <maxUsers> <features> <privateKey> <outputFile>");
            System.out.println("Example: CustomerLicenseGenerator \"ABC Corporation\" 5 50 \"FEAT1,FEAT2,FEAT3\" abc_private.key abc_license.lic");
            System.out.println("Example: CustomerLicenseGenerator \"XYZ Ltd\" perpetual 100 \"ALL_FEATURES\" xyz_private.key xyz_perpetual.lic");
            return;
        }

        String customerName = args[0];
        String licenseType = args[1];
        int maxUsers = Integer.parseInt(args[2]);
        String[] features = args[3].split(",");
        String privateKeyPath = args[4];
        String outputPath = args[5];

        // Derive public key path from private key path
        String publicKeyPath = privateKeyPath.replace("_private.", "_public.");

        try {
            CustomerLicenseGenerator generator = new CustomerLicenseGenerator(privateKeyPath, publicKeyPath);
            int durationYears = "perpetual".equalsIgnoreCase(licenseType) ? 0 : Integer.parseInt(licenseType);

            generator.generateCustomerLicense(
                    customerName,
                    licenseType,
                    durationYears,
                    maxUsers,
                    features,
                    outputPath
            );

        } catch (Exception e) {
            System.err.println("Error generating customer license: " + e.getMessage());
            e.printStackTrace();
        }
    }
}