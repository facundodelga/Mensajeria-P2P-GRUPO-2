// src/main/java/org/example/util/cifrado/ClaveUtil.java
package org.example.util.cifrado;

import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ClaveUtil {
    private static final String ALGORITHM_AES = "AES";
    private static final int KEY_SIZE_AES = 256;

    private static final String ALGORITHM_DH = "DiffieHellman";

    public static SecretKey generarClaveAes() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM_AES);
        keyGen.init(KEY_SIZE_AES, new SecureRandom());
        return keyGen.generateKey();
    }

    public static String claveAString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static SecretKey stringAClave(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, ALGORITHM_AES);
    }

    public static KeyPair generarParClavesDH() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_DH);
        keyPairGenerator.initialize(2048, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    public static SecretKey derivarClaveSecretaAES(PrivateKey privateKeyPropia, PublicKey publicKeyOtro)
            throws NoSuchAlgorithmException, InvalidKeyException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance(ALGORITHM_DH);
        keyAgreement.init(privateKeyPropia);
        keyAgreement.doPhase(publicKeyOtro, true);

        byte[] sharedSecret = keyAgreement.generateSecret();
        // Usar SHA-256 del secreto compartido para asegurar que la clave AES tenga el tamaño correcto
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] aesKeyBytes = sha256.digest(sharedSecret);
            return new SecretKeySpec(aesKeyBytes, 0, KEY_SIZE_AES / 8, ALGORITHM_AES);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar SHA-256 para la clave AES", e);
        }
    }

    public static String publicKeyAString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static PublicKey stringAPublicKey(String encodedPublicKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encodedPublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_DH);
        return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
    }
}