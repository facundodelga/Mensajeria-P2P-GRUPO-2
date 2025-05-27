package org.example.cliente.modelo.conversacion;

import org.example.util.cifrado.ICifradorMensajes;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto; // Necesario si quieres guardar los contactos directamente
import org.example.util.ClaveUtil; // Nueva utilidad para manejar claves

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors; // Para facilitar el manejo de participantes

public class Conversacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id; // ID único de la conversación (ej. combinación de IDs de participantes)
    private List<Mensaje> mensajes = new ArrayList<>();
    private boolean pendiente = true; // Mantener tu atributo original
    private String claveSecretaCodificada; // La clave se guarda codificada para serialización
    private transient SecretKey claveSecreta; // No se serializa directamente, se reconstruye
    private List<String> participantesIds; // IDs de los participantes para facilitar la persistencia
    private transient ICifradorMensajes cifradorMensajes; // No se serializa, se inyecta

    // Constructor para crear una nueva conversación
    public Conversacion(String id, List<Contacto> participantes, SecretKey claveSecreta) {
        this.id = id;
        this.participantesIds = participantes.stream()
                .map(Contacto::getNombre)
                .collect(Collectors.toList());
        this.claveSecreta = claveSecreta;
        this.claveSecretaCodificada = ClaveUtil.claveAString(claveSecreta);
        this.mensajes = new ArrayList<>();
        this.pendiente = true; // O false, según tu lógica inicial
    }

    // Constructor para cargar una conversación desde persistencia
    // La clave secreta ya viene codificada
    public Conversacion(String id, List<String> participantesIds, String claveSecretaCodificada) {
        this.id = id;
        this.participantesIds = participantesIds;
        this.claveSecretaCodificada = claveSecretaCodificada;
        this.claveSecreta = ClaveUtil.stringAClave(claveSecretaCodificada);
        this.mensajes = new ArrayList<>(); // Los mensajes se cargarán por separado o se añadirán después
        this.pendiente = true; // Se puede cargar el estado pendiente también si se persiste
    }

    // Constructor vacío para serialización
    public Conversacion() {
    }

    // Método para ser llamado después de la deserialización para reconstruir el SecretKey
    // y establecer el cifrador si se cargó la conversación de la persistencia.
    public void postDeserialization(ICifradorMensajes cifrador) {
        if (this.claveSecreta == null && this.claveSecretaCodificada != null) {
            this.claveSecreta = ClaveUtil.stringAClave(this.claveSecretaCodificada);
        }
        this.cifradorMensajes = cifrador;
    }

    public List<Mensaje> getMensajes() {
        return mensajes;
    }

    public void agregarMensaje(Mensaje mensaje) {
        this.mensajes.add(mensaje);
    }

    public boolean isPendiente() {
        return pendiente;
    }

    public void setPendiente(boolean pendiente) {
        this.pendiente = pendiente;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SecretKey getClaveSecreta() {
        // Asegurarse de que la clave esté disponible si se deserializó
        if (this.claveSecreta == null) {
            this.claveSecreta = ClaveUtil.stringAClave(this.claveSecretaCodificada);
        }
        return claveSecreta;
    }

    public String getClaveSecretaCodificada() {
        return claveSecretaCodificada;
    }

    public void setClaveSecretaCodificada(String claveSecretaCodificada) {
        this.claveSecretaCodificada = claveSecretaCodificada;
        this.claveSecreta = ClaveUtil.stringAClave(claveSecretaCodificada); // Reconstruir la clave
    }

    public List<String> getParticipantesIds() {
        return participantesIds;
    }

    public void setParticipantesIds(List<String> participantesIds) {
        this.participantesIds = participantesIds;
    }

    public ICifradorMensajes getCifradorMensajes() {
        return cifradorMensajes;
    }

    public void setCifradorMensajes(ICifradorMensajes cifradorMensajes) {
        this.cifradorMensajes = cifradorMensajes;
    }

    // Setter para mensajes, útil al cargar la conversación de persistencia
    public void setMensajes(List<Mensaje> mensajes) {
        this.mensajes = mensajes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversacion that = (Conversacion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Conversacion{" +
                "id='" + id + '\'' +
                ", participantes=" + participantesIds +
                ", numMensajes=" + mensajes.size() +
                ", pendiente=" + pendiente +
                '}';
    }
}