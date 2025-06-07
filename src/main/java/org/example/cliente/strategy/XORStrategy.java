package org.example.cliente.strategy;

public class XORStrategy implements CifradoStrategy {

    @Override
    public String encrypt(String message, String key) {

        return "2." + this.xor(message, key);
    }

    @Override
    public String decrypt(String message, String key) {
        String content = message.substring(2); // Skip "2."

        return  this.xor(content, key);// XOR again to decrypt
    }

    private String xor(String message, String key) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            sb.append((char)(message.charAt(i) ^ key.charAt(i % key.length())));
        }

        return sb.toString();
    }
}
