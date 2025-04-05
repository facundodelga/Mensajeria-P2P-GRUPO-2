package org.example.conexion;

public class EnviarMensajeException extends Exception {
    public EnviarMensajeException(String message) {
        super(message);
    }

    public EnviarMensajeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnviarMensajeException(Throwable cause) {
        super(cause);
    }
}
