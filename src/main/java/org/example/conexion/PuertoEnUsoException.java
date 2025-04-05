package org.example.conexion;

public class PuertoEnUsoException extends Exception {
    public PuertoEnUsoException(String message) {
        super(message);
    }

    public PuertoEnUsoException(String message, Throwable cause) {
        super(message, cause);
    }

    public PuertoEnUsoException(Throwable cause) {
        super(cause);
    }
}
