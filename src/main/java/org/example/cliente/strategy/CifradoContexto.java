package org.example.cliente.strategy;

import java.io.BufferedReader;
import java.io.FileReader;

public class CifradoContexto implements ICifradoMensajes {
    private CifradoStrategy strategy;
    private final String key;

    public CifradoContexto() {
        try (BufferedReader br = new BufferedReader(new FileReader("cifradoConfig.txt"))) {
            String line = br.readLine();
            switch (line.trim()) {
                case "1":
                    strategy = new CesarStrategy();
                    break;
                case "2":
                    strategy = new XORStrategy();
                    break;
                case "3":
                    strategy = new AESStrategy();
                    break;
                default:
                    throw new IllegalArgumentException("Algoritmo no soportado: " + line);
            }
            key = br.readLine().trim();
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo cifrado.txt", e);
        }
    }

    @Override
    public String cifrar(String mensaje) {
        return strategy.encrypt(mensaje, key);
    }

    @Override
    public String descifrar(String mensaje) {
        char type = mensaje.charAt(0);
        switch (type) {
            case '1':
                strategy = new CesarStrategy();
                break;
            case '2':
                strategy = new XORStrategy();
                break;
            case '3':
                strategy = new AESStrategy();
                break;
            default:
                throw new IllegalArgumentException("Formato de mensaje inválido");
        }
        return strategy.decrypt(mensaje, key);
    }
}