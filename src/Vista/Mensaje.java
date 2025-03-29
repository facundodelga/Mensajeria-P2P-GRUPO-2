package Vista;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Mensaje {
    private String texto;
    private boolean esMio;
    private String hora;

    public Mensaje(String texto, boolean esMio) {
        this.texto = texto;
        this.esMio = esMio;
        this.hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
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
