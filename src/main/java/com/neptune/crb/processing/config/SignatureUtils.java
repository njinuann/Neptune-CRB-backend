package com.neptune.crb.processing.config;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class SignatureUtils
{

    private static final String SIGNING_ALGORITHM = "SHA256withRSA";

    public static String sign(String data, PrivateKey privateKey) throws Exception
    {
        Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    public static boolean verify(String data, String signature, PublicKey publicKey) throws Exception
    {
        Signature sig = Signature.getInstance(SIGNING_ALGORITHM);
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return sig.verify(signatureBytes);
    }

    public static PublicKey stringToPublicKey(String publicKeyString) throws Exception
    {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
        java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
}