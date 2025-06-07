package org.example.cliente.strategy;

public class CesarStrategy implements CifradoStrategy {
    private static final int SHIFT = 3;

    @Override
    public String encrypt(String message, String key) {
        StringBuilder sb = new StringBuilder();
        for (char c : message.toCharArray()) {
            sb.append((char)(c + SHIFT));
        }
        return "1." + sb.toString();
    }

    @Override
    public String decrypt(String message, String key) {
        String content = message.substring(2); // Skip "1."
        StringBuilder sb = new StringBuilder();
        for (char c : content.toCharArray()) {
            sb.append((char)(c - SHIFT));
        }
        return sb.toString();
    }
}
