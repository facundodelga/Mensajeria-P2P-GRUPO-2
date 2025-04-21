package org.example.cliente.modelo;

public class ContactoRepetidoException extends Exception{
    public ContactoRepetidoException(String message) {
        super(message);
    }

    public ContactoRepetidoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContactoRepetidoException(Throwable cause) {
        super(cause);
    }
}
