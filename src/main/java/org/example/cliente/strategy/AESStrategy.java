package org.example.cliente.strategy;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESStrategy implements CifradoStrategy {

    @Override
    public String encrypt(String message, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = aesCipher.doFinal(message.getBytes());
            return "3." + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar con AES", e);
        }
    }

    @Override
    public String decrypt(String message, String key) {
        try {
            String content = message.substring(2); // Skip "3."
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = aesCipher.doFinal(Base64.getDecoder().decode(content));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar con AES", e);
        }
    }
}
