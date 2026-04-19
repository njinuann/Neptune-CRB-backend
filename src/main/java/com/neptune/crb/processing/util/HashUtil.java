package com.neptune.crb.processing.util;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashUtil
{

    public static String generateSHA256Hash(String input)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Failed to generate SHA-256 hash", e);
        }
    }

    public static String generateSignature(String payload)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e)
        {
            throw new RuntimeException("Signature generation failed", e);
        }
    }

    public static String generateSignatureHmac(String valueToHash)
    {
        try
        {
            //String valueToHash = privateKey + payload + publicKey + timestamp;

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes =
                    digest.digest(
                            valueToHash.getBytes(StandardCharsets.UTF_8)
                    );

            return bytesToHex(hashBytes);
        } catch (Exception e)
        {
            throw new RuntimeException(
                    "Unable to generate Metropol hash",
                    e
            );
        }
    }


    private static String bytesToHex(byte[] bytes)
    {
        StringBuilder hex =
                new StringBuilder(bytes.length * 2);

        for (byte b : bytes)
        {
            String s =
                    Integer.toHexString(0xff & b);

            if (s.length() == 1)
            {
                hex.append('0');
            }

            hex.append(s);
        }

        return hex.toString();
    }
}
