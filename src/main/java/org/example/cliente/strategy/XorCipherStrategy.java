package org.example.cliente.strategy;

public class XorCipherStrategy implements CipherStrategy {

    @Override
    public String encrypt(String message, String key) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            sb.append((char)(message.charAt(i) ^ key.charAt(i % key.length())));
        }
        return "2." + sb.toString();
    }

    @Override
    public String decrypt(String message, String key) {
        String content = message.substring(2); // Skip "2."
        return encrypt(content, key); // XOR again to decrypt
    }
}
