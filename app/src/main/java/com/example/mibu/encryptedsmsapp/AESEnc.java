package com.example.mibu.encryptedsmsapp;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Mibu on 27-Apr-16.
 */
public class AESEnc {

    public static final String TAG= "AESEnc";

    // 8 char pass = +68 msgchars

    // Cipher Transformation
    public static final String AES_MODE = "AES/CBC/PKCS5Padding";
    public static final String CHARSET = "UTF-8";
    private static final int PBE_ITERATION_COUNT = 4000;
    private static final int AES_KEY_LENGTH_BITS = 128;
    private static final String HASH_ALGORITHM = "SHA1PRNG";
    public static boolean DEBUG_LOG_ENABLED = false;

    // Generates a random Initialization Vector
    public static byte[] generateIv() throws GeneralSecurityException {
        SecureRandom randomiv = new SecureRandom();
        byte[] ivBytes = new byte[16];
        randomiv.nextBytes(ivBytes);
        return ivBytes;
    }

    public static byte[] iv;

    public static byte[] getIV() {
        return iv;
    }

    // Generates a random 160 bit Salt to use for the key generation
    public static byte[] generateSalt() throws GeneralSecurityException {
        SecureRandom randomsalt = SecureRandom.getInstance(HASH_ALGORITHM);
        byte[] saltBytes = new byte[20];
        randomsalt.nextBytes(saltBytes);
        return saltBytes;
    }

    public static byte[] salt;

    public static byte[] getSalt() {
        return salt;
    }

    // Method to generate 128 bit encryption key based on the given password
    public static byte[] deriveKey (String password, byte[] salt, int iterations, int length) throws NoSuchAlgorithmException, Exception {
        PBEKeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, iterations, length);
        SecretKeyFactory seckeyfac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return seckeyfac.generateSecret(keyspec).getEncoded();
    }

    public static String encrypt (String message, String password) throws Exception, UnsupportedEncodingException {

        // Calls the Initialization Vector Generation method
        byte[] iv = generateIv();
        // Calls the Salt Generation method
        byte[] salt = generateSalt();

        // Generates 128 bit encryption key (calls the deriveKey method)
        byte[] derkey = deriveKey(password, salt, PBE_ITERATION_COUNT, AES_KEY_LENGTH_BITS);

        // Performs the Encryption
        SecretKeySpec enckeyspec = new SecretKeySpec(derkey, "AES");
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, enckeyspec, new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal(message.getBytes(CHARSET));

        // Output message that is an Array "Salt + Ciphertext + IV"
        byte[] outputmsg = new byte[20 + ciphertext.length + 16];
        System.arraycopy(salt, 0, outputmsg, 0, 20);
        System.arraycopy(ciphertext, 0, outputmsg, 20, ciphertext.length);
        System.arraycopy(iv, 0 , outputmsg, 20+ciphertext.length, 16);

        // Encodes the output message to a Base64 String
        String encodedmsg = Base64.encodeToString(outputmsg, Base64.NO_WRAP);

        return encodedmsg;
    }

    public static String decrypt(String encoutmsg, String password) throws Exception {
        encoutmsg.toCharArray();

        // Get's the Array of the output message by decoding Base64 String
        byte[] outputmsg = Base64.decode(encoutmsg, Base64.NO_WRAP);

        // Recovers salt ciphertext and initialization vector (checks minimum length 20byte salt +16 IV)
        if (outputmsg.length>36) {
            byte[] salt = Arrays.copyOfRange(outputmsg, 0, 20);
            byte[] ciphertext = Arrays.copyOfRange(outputmsg, 20, outputmsg.length - 16);
            byte[] iv = Arrays.copyOfRange(outputmsg, outputmsg.length - 16, outputmsg.length);

            // Get's the encryption key from the output salt
            byte[] derkey = deriveKey(password, salt, 4000, 128);

            // Performs the Decryption
            SecretKeySpec enckeyspec = new SecretKeySpec(derkey, "AES");
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.DECRYPT_MODE, enckeyspec, new IvParameterSpec(iv));
            byte[] decmsg = cipher.doFinal(ciphertext);

            // Returns the Decrypted String
            return new String (decmsg, CHARSET);
        }
        throw new Exception();
    }



    private static void log(String what, byte[] bytes) {
        if (DEBUG_LOG_ENABLED)
            Log.d(TAG, what + "[" + bytes.length + "] [" + bytesToHex(bytes) + "]");
    }

    private static void log(String what, String value) {
        if (DEBUG_LOG_ENABLED)
            Log.d(TAG, what + "[" + value.length() + "] [" + value + "]");
    }



    // Converts byte array to hexidecimal useful for logging and fault finding
    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private AESEnc() {
    }
}
