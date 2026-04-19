package com.neptune.crb.processing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.neptune.crb.processing.component.KeyUtils;
import com.neptune.crb.processing.exception.LicenseException;
import com.neptune.crb.processing.config.SignatureUtils;
import com.neptune.crb.processing.model.License;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LicenseService {

    @Value("${license.file.path:classpath:crb-license.lic}")
    private String licenseFilePath;

    @Value("${license.public.key.path:classpath:public.key}")
    private String publicKeyPath;

    @Value("${license.public.key:}")
    private String publicKeyString;

    @Value("${license.grace.period.days:7}")
    private int gracePeriodDays;

    private License currentLicense;
    private PublicKey publicKey;

    @PostConstruct
    public void initialize() {
        try {
            System.out.println("=== LICENSE SERVICE INITIALIZATION ===");
            System.out.println("License file path: " + licenseFilePath);
            System.out.println("Public key path: " + publicKeyPath);

            loadPublicKey();
            loadAndValidateLicense();
            startPeriodicChecks();

            System.out.println("✓ License validation successful!");
            System.out.println("Customer: " + currentLicense.getCustomerName());
            System.out.println("License ID: " + currentLicense.getLicenseId());
            System.out.println("Type: " + (currentLicense.isPerpetual() ? "PERPETUAL" : "EXPIRING"));
            if (!currentLicense.isPerpetual()) {
                System.out.println("Expires: " + currentLicense.getExpirationDate());
            }
        } catch (LicenseException e) {
            shutdownApplication(e.getMessage());
        }
    }

    private void loadPublicKey() throws LicenseException {
        try {
            System.out.println("Attempting to load public key from: " + publicKeyPath);

            // Option 1: Load from external file path (file: prefix)
            if (publicKeyPath != null && publicKeyPath.startsWith("file:")) {
                String filePath = publicKeyPath.substring("file:".length());
                System.out.println("Loading from file path: " + filePath);

                File publicKeyFile = new File(filePath);
                System.out.println("Public key file exists: " + publicKeyFile.exists());
                System.out.println("Public key file absolute path: " + publicKeyFile.getAbsolutePath());

                if (publicKeyFile.exists()) {
                    String publicKeyBase64 = new String(Files.readAllBytes(publicKeyFile.toPath())).trim();
                    System.out.println("Public key content length: " + publicKeyBase64.length());
                    publicKey = KeyUtils.base64ToPublicKey(publicKeyBase64);
                    System.out.println("✓ Public key loaded successfully from file");
                    return;
                } else {
                    System.out.println("❌ Public key file not found: " + filePath);
                }
            }

            // Option 2: Load from classpath (classpath: prefix)
            if (publicKeyPath != null && publicKeyPath.startsWith("classpath:")) {
                String resourcePath = publicKeyPath.substring("classpath:".length());
                System.out.println("Loading from classpath: " + resourcePath);

                Resource resource = new ClassPathResource(resourcePath);
                System.out.println("Classpath resource exists: " + resource.exists());

                if (resource.exists()) {
                    String publicKeyBase64 = new String(resource.getInputStream().readAllBytes()).trim();
                    publicKey = KeyUtils.base64ToPublicKey(publicKeyBase64);
                    System.out.println("✓ Public key loaded successfully from classpath");
                    return;
                } else {
                    System.out.println("❌ Public key not found in classpath: " + resourcePath);
                }
            }

            // Option 3: Direct file path (no prefix)
            if (publicKeyPath != null && !publicKeyPath.contains(":")) {
                System.out.println("Loading from direct file path: " + publicKeyPath);

                File publicKeyFile = new File(publicKeyPath);
                System.out.println("Public key file exists: " + publicKeyFile.exists());
                System.out.println("Public key file absolute path: " + publicKeyFile.getAbsolutePath());

                if (publicKeyFile.exists()) {
                    String publicKeyBase64 = new String(Files.readAllBytes(publicKeyFile.toPath())).trim();
                    publicKey = KeyUtils.base64ToPublicKey(publicKeyBase64);
                    System.out.println("✓ Public key loaded successfully from direct path");
                    return;
                } else {
                    System.out.println("❌ Public key file not found: " + publicKeyPath);
                }
            }

            // Option 4: Inline public key in properties
            if (publicKeyString != null && !publicKeyString.trim().isEmpty()) {
                System.out.println("Loading inline public key");
                publicKey = KeyUtils.base64ToPublicKey(publicKeyString);
                System.out.println("✓ Public key loaded successfully from inline property");
                return;
            }

            // Option 5: Public key embedded in license file (try later after license is loaded)
            System.out.println("Will try to load public key from license file after license is loaded");

            throw new LicenseException("No public key available for signature verification");

        } catch (Exception e) {
            System.err.println("ERROR loading public key: " + e.getMessage());
            e.printStackTrace();
            throw new LicenseException("Failed to load public key: " + e.getMessage());
        }
    }

    public void loadAndValidateLicense() throws LicenseException {
        try {
            currentLicense = loadLicenseFromFile();
            System.out.println("License loaded successfully: " + currentLicense.getLicenseId());

            // If public key not loaded yet, try loading from license
            if (publicKey == null && currentLicense.getPublicKey() != null &&
                    !currentLicense.getPublicKey().trim().isEmpty()) {
                System.out.println("Attempting to load public key from license file content");
                publicKey = KeyUtils.base64ToPublicKey(currentLicense.getPublicKey());
                System.out.println("✓ Public key loaded from license file");
            }

            // Final check if public key is available
            if (publicKey == null) {
                throw new LicenseException("Public key still not available after loading license");
            }

            validateLicense(currentLicense);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LicenseException("Failed to load or validate license: " + e.getMessage());
        }
    }

    private License loadLicenseFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        System.out.println("Loading license from: " + licenseFilePath);

        if (licenseFilePath.startsWith("classpath:")) {
            String resourcePath = licenseFilePath.substring("classpath:".length());
            System.out.println("Loading from classpath: " + resourcePath);

            Resource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                throw new IOException("License file not found in classpath: " + resourcePath);
            }
            License license = mapper.readValue(resource.getInputStream(), License.class);
            System.out.println("✓ License loaded from classpath");
            return license;

        } else if (licenseFilePath.startsWith("file:")) {
            String filePath = licenseFilePath.substring("file:".length());
            System.out.println("Loading from file: " + filePath);

            File licenseFile = new File(filePath);
            System.out.println("License file exists: " + licenseFile.exists());
            System.out.println("License file absolute path: " + licenseFile.getAbsolutePath());

            if (!licenseFile.exists()) {
                throw new IOException("License file not found: " + filePath);
            }
            License license = mapper.readValue(licenseFile, License.class);
            System.out.println("✓ License loaded from file");
            return license;

        } else {
            // Assume it's a direct file path
            System.out.println("Loading from direct file path: " + licenseFilePath);

            File licenseFile = new File(licenseFilePath);
            System.out.println("License file exists: " + licenseFile.exists());
            System.out.println("License file absolute path: " + licenseFile.getAbsolutePath());

            if (!licenseFile.exists()) {
                throw new IOException("License file not found: " + licenseFilePath);
            }
            License license = mapper.readValue(licenseFile, License.class);
            System.out.println("✓ License loaded from direct path");
            return license;
        }
    }

    private void validateLicense(License license) throws LicenseException {
        System.out.println("Validating license...");

        // Check expiration (skip if perpetual)
        if (!license.isPerpetual()) {
            LocalDate currentDate = LocalDate.now();
            LocalDate expirationDate = license.getExpirationDate();
            LocalDate graceDate = expirationDate.plusDays(gracePeriodDays);

            System.out.println("Current date: " + currentDate);
            System.out.println("Expiration date: " + expirationDate);
            System.out.println("Grace period end: " + graceDate);

            if (currentDate.isAfter(graceDate)) {
                throw new LicenseException("License expired on " + expirationDate + ". Grace period ended on " + graceDate);
            } else if (currentDate.isAfter(expirationDate)) {
                // Log warning but don't throw exception during grace period
                System.err.println("WARNING: License expired on " + expirationDate +
                        ". Grace period ends on " + graceDate);
            } else {
                System.out.println("✓ License expiration validation passed");
            }
        } else {
            System.out.println("✓ Perpetual license - no expiration check needed");
        }

        // Verify signature
        System.out.println("Verifying license signature...");
        if (!verifySignature(license)) {
            throw new LicenseException("Invalid license signature - possible tampering detected");
        }
        System.out.println("✓ License signature verified");

        // Additional validations
        validateLicenseContents(license);
        System.out.println("✓ License content validation passed");
    }

    private boolean verifySignature(License license) {
        try {
            if (publicKey == null) {
                throw new LicenseException("Public key not initialized for signature verification");
            }

            // Create the signable string and verify signature
            String signableData = license.toSignableString();
            System.out.println("Signable data: " + signableData);
            System.out.println("Signature: " + license.getSignature());

            boolean isValid = SignatureUtils.verify(signableData, license.getSignature(), publicKey);
            System.out.println("Signature valid: " + isValid);

            return isValid;
        } catch (Exception e) {
            System.err.println("Signature verification error: " + e.getMessage());
            throw new LicenseException("Signature verification failed: " + e.getMessage());
        }
    }

    private void validateLicenseContents(License license) throws LicenseException {
        // Validate required fields
        if (license.getLicenseId() == null || license.getLicenseId().trim().isEmpty()) {
            throw new LicenseException("Invalid license: missing license ID");
        }

        if (license.getCustomerName() == null || license.getCustomerName().trim().isEmpty()) {
            throw new LicenseException("Invalid license: missing customer name");
        }

        if (!license.isPerpetual() && license.getExpirationDate() == null) {
            throw new LicenseException("Invalid license: missing expiration date for non-perpetual license");
        }

        // Validate issue date is before expiration date (if both exist)
        if (license.getIssueDate() != null && license.getExpirationDate() != null &&
                license.getIssueDate().isAfter(license.getExpirationDate())) {
            throw new LicenseException("Invalid license: issue date after expiration date");
        }

        // Validate features
        if (license.getFeatures() == null || license.getFeatures().isEmpty()) {
            throw new LicenseException("Invalid license: no features specified");
        }
    }

    private void startPeriodicChecks() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::periodicLicenseCheck, 1, 1, TimeUnit.HOURS);
        System.out.println("✓ Periodic license checks scheduled (every hour)");
    }

    public void periodicLicenseCheck() {
        try {
            System.out.println("Performing periodic license check...");
            validateLicense(currentLicense);
            System.out.println("✓ Periodic license check passed");
        } catch (LicenseException e) {
            shutdownApplication("Periodic check failed: " + e.getMessage());
        }
    }

    public License getCurrentLicense() {
        return currentLicense;
    }

    public boolean isFeatureAllowed(String feature) {
        return currentLicense != null &&
                currentLicense.getFeatures() != null &&
                currentLicense.getFeatures().contains(feature);
    }

    public boolean isLicenseValid() {
        try {
            validateLicense(currentLicense);
            return true;
        } catch (LicenseException e) {
            return false;
        }
    }

    private void shutdownApplication(String message) {
        System.err.println("FATAL: " + message);
        System.err.println("Application shutting down due to license violation");
        System.exit(1);
    }
}