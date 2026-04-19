package com.neptune.crb.processing.component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtils {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public static String keyToBase64(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }


public static PublicKey base64ToPublicKey(String base64Key) throws GeneralSecurityException {
    byte[] keyBytes = Base64.getDecoder().decode(base64Key);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(spec);
}

    public static PrivateKey base64ToPrivateKey(String base64Key) throws GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    public static void saveKeyPairToFiles(KeyPair keyPair, String privateKeyPath, String publicKeyPath)
            throws IOException
    {

        // Save private key
        String privateKeyBase64 = keyToBase64(keyPair.getPrivate());
        Files.write(Paths.get(privateKeyPath), privateKeyBase64.getBytes());

        // Save public key
        String publicKeyBase64 = keyToBase64(keyPair.getPublic());
        Files.write(Paths.get(publicKeyPath), publicKeyBase64.getBytes());

        System.out.println("✓ Key pair generated and saved:");
        System.out.println("  Private key: " + privateKeyPath);
        System.out.println("  Public key: " + publicKeyPath);
        System.out.println("  Keep the private key secure and NEVER distribute it!");
    }

    public static void main(String[] args) throws Exception {
        // Generate and save a new key pair
        KeyPair keyPair = generateKeyPair();
        saveKeyPairToFiles(keyPair, "private.key", "public.key");
    }
}
