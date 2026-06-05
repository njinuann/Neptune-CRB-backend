package com.neptune.crb.processing.component;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.spec.KeySpec;
import java.util.Base64;

import static com.neptune.crb.processing.util.Logger.logError;
import static com.neptune.crb.processing.util.Logger.logEvent;

@Component
public class AXCrypt {

    private Cipher cipher;
    private final byte[] salt =
            {
                    -87, -101, -56, 50, 86, 53, -29, 3
            };
    private static final int ITERATION_COUNT = 1024;
    private static final int KEY_STRENGTH = 128;
    private static SecretKey key;
    private static final byte[] iv =
            {
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            };
    private static final String DEFAULT_NEW_PASS_PHRASE = "Rubikon Universal Core Banking System";

    public AXCrypt()

    {
        try
        {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

            KeySpec spec = new PBEKeySpec(DEFAULT_NEW_PASS_PHRASE.toCharArray(), this.salt, 1024, 128);

            SecretKey tmp = factory.generateSecret(spec);

            key = new SecretKeySpec(tmp.getEncoded(), "AES");

            this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (Exception ex)
        {
            logError("initiateProcess",ex);
        }
    }

    protected AXCrypt(String passPhrase)
    {
        try
        {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

            KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), this.salt, 1024, 128);

            SecretKey tmp = factory.generateSecret(spec);

            key = new SecretKeySpec(tmp.getEncoded(), "AES");

            this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (Exception ex)
        {
            logError("initiateProcess",ex);
        }
    }

    public String encrypt(String plainStr)
    {
        String encrypted = null;
        try
        {
            if (plainStr == null)
            {
                throw new IllegalArgumentException("The argument 'plainStr' is null");
            }

            IvParameterSpec ivspec = new IvParameterSpec(iv);

            this.cipher.init(1, key, ivspec);

            byte[] utf8EncryptedData = this.cipher.doFinal(plainStr.getBytes());

            String base64EncryptedData = Base64.getEncoder().encodeToString(utf8EncryptedData);
            encrypted = base64EncryptedData;
        }
        catch (Exception ex)
        {
            logError("initiateProcess",ex);
        }
        return encrypted;
    }

    public String decrypt(String encryptedStr)
    {
        String decryted = "";
        try
        {
            if (encryptedStr == null)
            {
                throw new IllegalArgumentException("The argument 'encryptedStr' is null");
            }

            IvParameterSpec ivspec = new IvParameterSpec(iv);

            this.cipher.init(2, key, ivspec);

            byte[] decryptedData = Base64.getDecoder().decode(encryptedStr);

            byte[] utf8 = this.cipher.doFinal(decryptedData);

            decryted = new String(utf8, "UTF8");

        }
        catch (Exception ex)
        {
            logError("initiateProcess",ex);

        }
        return decryted;
    }

    public static void main(String[] args)
    {
        try
        {
            System.err.println(".. " + new AXCrypt(DEFAULT_NEW_PASS_PHRASE).decrypt("6l45KR3t5PljvVAxpfvCyA=="));
            System.err.println(".. " + new AXCrypt(DEFAULT_NEW_PASS_PHRASE).encrypt("training"));
            //   System.err.println(".. "+new BRCryptNew(DEFAULT_NEW_PASS_PHRASE).encrypt("proxy_password"));

        }
        catch (Exception ex)
        {
           ex.printStackTrace();
        }
    }
}
