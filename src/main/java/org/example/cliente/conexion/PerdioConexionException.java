package org.example.cliente.conexion;

public class PerdioConexionException extends Exception {
    public PerdioConexionException(String message) {
        super(message);
    }

    public PerdioConexionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PerdioConexionException(Throwable cause) {
        super(cause);
    }
}
