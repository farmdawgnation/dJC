/*
 * dJC: The dAmn Java Client
 * Crypto.java
 * ©2005 The dAmn Java Project
 *
 * This software and it's source code are distributed under the terms and conditions of the GNU
 * General Public License, Version 2. A copy of this license has been provided.
 * If you do not agree with the terms of this license then please erase all copies
 * of this program and it's source. Thank you.
 */

package client;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.NullCipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * This utility class provides string encryption and decryption methods.
 * @author Eric Olander
 */
final class Crypto {
    
    static private AlgorithmParameterSpec _algParams;
    static private Cipher _cipher;
    static private Key _key;
    static private final String SALT = "e2^9hCUw";
    static private final String PW = "gI7@ei,SI9w3;w97&$";
    static private final char[] HEX_CHARS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    
    static {
        try {
            _cipher = Cipher.getInstance("PBEWithMD5AndDES");
            PBEKeySpec pks = new PBEKeySpec(PW.toCharArray());
            _key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(pks);
            _algParams = new PBEParameterSpec(SALT.getBytes(), 20);
        } catch (Exception e) {
            e.printStackTrace();
            _cipher = new NullCipher();
        }
    }
    
    /** Creates a new instance of Crypto */
    private Crypto() {
    }
    
    /**
     * Converts an array of bytes to a string of hex values
     * @param bytes 
     * @return 
     */
    private static String hexToString(byte[] bytes) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            int hi = (bytes[i] & 0xf0) >> 4;
            int lo = bytes[i] & 0x0f;
            buff.append(HEX_CHARS[hi]);
            buff.append(HEX_CHARS[lo]);
        }
        return buff.toString();
    }
    
    /**
     * Converts a string consisting of hex values to the corresponding byte array
     * @param str 
     * @return 
     */
    private static byte[] stringToHex(String str) {
        byte[] result = new byte[str.length()/2];
        for (int i = 0; i < str.length(); i+=2) {
            int hi = Character.digit(str.charAt(i), 16);
            int lo = Character.digit(str.charAt(i+1), 16);
            result[i/2] = (byte)((hi << 4) | lo);
        }
        return result;
    }
    
    /**
     * Encrypts a string using triple DES 
     * @param str The string to be encrypted
     * @return the encrypted string or the original string if an exception occurred
     */
    public static String encrypt(String str) {
        try {
        _cipher.init(Cipher.ENCRYPT_MODE, _key, _algParams);
        byte[] ciphertext = _cipher.doFinal(str.getBytes());
        return hexToString(ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }
    
    /**
     * Decrypts a previously encrypted string
     * @param str String to be decrypted
     * @return The decrypted string or the original string if an exception occurred.
     */
    public static String decrypt(String str) {
        try {
            _cipher.init(Cipher.DECRYPT_MODE, _key, _algParams);
            byte[] plaintext = _cipher.doFinal(stringToHex(str));
            return new String(plaintext);
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }
}
