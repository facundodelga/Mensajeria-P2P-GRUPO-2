package org.example.cliente.strategy;

public interface CifradoStrategy {
    String encrypt(String message, String key);
    String decrypt(String message, String key);
}
