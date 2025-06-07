package org.example.cliente.strategy;

import java.io.BufferedReader;
import java.io.FileReader;

public class CipherContext {
    private CipherStrategy strategy;
    private final String key;

    public CipherContext(String key) {
        this.key = key;
        updateStrategy();
    }

    public void updateStrategy() {
        try (BufferedReader br = new BufferedReader(new FileReader("cifrado.txt"))) {
            String line = br.readLine();
            switch (line.trim()) {
                case "1":
                    strategy = new CaesarCipherStrategy();
                    break;
                case "2":
                    strategy = new XorCipherStrategy();
                    break;
                case "3":
                    strategy = new AESCipherStrategy();
                    break;
                default:
                    throw new IllegalArgumentException("Algoritmo no soportado: " + line);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo cifrado.txt", e);
        }
    }

    public String encrypt(String message) {
        return strategy.encrypt(message, key);
    }

    public String decrypt(String message) {
        char type = message.charAt(0);
        switch (type) {
            case '1':
                strategy = new CaesarCipherStrategy();
                break;
            case '2':
                strategy = new XorCipherStrategy();
                break;
            case '3':
                strategy = new AESCipherStrategy();
                break;
            default:
                throw new IllegalArgumentException("Formato de mensaje inválido");
        }
        return strategy.decrypt(message, key);
    }
}