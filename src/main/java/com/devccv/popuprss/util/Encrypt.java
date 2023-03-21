package com.devccv.popuprss.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

public final class Encrypt {
    private static final java.util.Base64.Encoder base64Encoder = java.util.Base64.getEncoder();
    private static final java.util.Base64.Decoder base64Decoder = java.util.Base64.getDecoder();

    public static String encrypt(String content, String key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        //CBC模式需要生成一个16 bytes的initialization vector
        SecureRandom sr = SecureRandom.getInstanceStrong();
        byte[] iv = sr.generateSeed(16);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] data = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        //IV不需要保密，把IV和密文一起返回
        return base64Encoder.encodeToString(iv) + base64Encoder.encodeToString(data);
    }

    public static String decrypt(String input, String key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //把input分割成IV和密文
        String iv = input.substring(0, 24);
        String data = input.substring(24);
        //解密
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(base64Decoder.decode(iv)));
        return new String(cipher.doFinal(base64Decoder.decode(data)), StandardCharsets.UTF_8);
    }

    public static String encryptWithUserName(String content) throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return encrypt(content, getKey());
    }

    public static String decryptWithUserName(String content) throws InvalidAlgorithmParameterException, NoSuchPaddingException,
            IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return decrypt(content, getKey());
    }

    private static String getKey() {
        String username = System.getenv("USERNAME");
        String key = Objects.requireNonNullElse(username, "default");
        StringBuilder sb = new StringBuilder("PopRSS" + key);
        if (sb.length() < 32) {
            int i = 0;
            while (sb.length() < 32) {
                sb.append(sb.charAt(i++));
                if (i >= sb.length()) i = 0;
            }
        } else if (sb.length() > 32) {
            return sb.substring(0, 32);
        }
        return sb.toString();
    }
}
