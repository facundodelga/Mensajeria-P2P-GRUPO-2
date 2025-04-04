package org.example.vista;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MensajePantalla {
    private String texto;
    private boolean esMio;
    private String hora;

    public MensajePantalla(String texto, boolean esMio, String hora) {
        this.texto = texto;
        this.esMio = esMio;
        this.hora = hora;
    }

    public String getTexto() {
        return texto;
    }

    public boolean esMio() {
        return esMio;
    }

    public String getHora() {
        return hora;
    }
}
