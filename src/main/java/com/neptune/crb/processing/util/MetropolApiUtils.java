package com.neptune.crb.processing.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class MetropolApiUtils {
    private final Environment env;
    private final String PRIVATE_KEY; // Replace with the assigned private key
    private final String PUBLIC_KEY;   // Replace with the assigned public API key


    @Autowired
    public MetropolApiUtils(Environment env) {
        this.env = env;
        this.PRIVATE_KEY =env.getProperty("crb.privateKey");
        this.PUBLIC_KEY =env.getProperty("crb.publicKey");
    }

    /**
     * Generates the current timestamp in the required format.
     *
     * @return the formatted UTC timestamp
     */

    public  String generateTimestamp() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
    }

    /**
     * Generates the SHA-256 hash for the API request.
     *
     * @param jsonObject the JSON payload to be sent
     * @param timestamp  the timestamp used in the request
     * @return the generated hash as a hexadecimal string
     * @throws Exception if the hashing fails
     */
    public  String generateHash(String jsonObject, String timestamp) throws Exception {
        String input = PRIVATE_KEY + jsonObject + PUBLIC_KEY + timestamp;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        // Convert byte array to hexadecimal
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
