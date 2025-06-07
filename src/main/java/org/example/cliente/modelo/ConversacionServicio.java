package org.example.cliente.modelo;

import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.cliente.modelo.usuario.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList; // Necesario si usas new ArrayList<>() para List.of() en Java 8

/**
 * Clase que proporciona servicios relacionados con las conversaciones de un usuario.
 * Implementa la interfaz IConversacion.
 */
public class ConversacionServicio implements IConversacion {
    private Usuario usuario;

    /**
     * Constructor de la clase ConversacionServicio.
     * @param usuario El usuario al que pertenecen las conversaciones.
     */
    public ConversacionServicio(Usuario usuario){
        this.usuario = usuario;
    }

    /**
     * Obtiene los mensajes de una conversación con un contacto específico.
     * @param contacto El contacto cuya conversación se desea obtener.
     * @return Una lista de mensajes de la conversación con el contacto.
     */
    @Override
    public List<Mensaje> getMensajes(Contacto contacto){
        Conversacion conversacion = usuario.getConversaciones().get(contacto);
        if (conversacion != null) {
            return conversacion.getMensajes();
        }
        // Si no hay conversación, devuelve una lista vacía para evitar NullPointerException.
        // Usa List.of() para Java 9+ o new ArrayList<>() para versiones anteriores.
        return new ArrayList<>(); // Para compatibilidad con Java 8 o si prefieres Listas mutables
    }

    /**
     * Agrega una nueva conversación con un contacto específico.
     * @param contacto El contacto con el que se desea iniciar una conversación.
     */
    @Override
    public void agregarConversacion(Contacto contacto) {
        System.out.println("Agregando conversacion");
        usuario.getConversaciones().put(contacto, new Conversacion());
    }

    /**
     * Añade un mensaje entrante a la conversación correspondiente.
     * Si la conversación no existe, se crea una nueva.
     * @param mensaje El mensaje entrante que se desea añadir.
     */
    @Override
    public void addMensajeEntrante(Mensaje mensaje) {
        // CORRECCIÓN: Buscamos el objeto Contacto real en la lista de contactos del usuario.
        // Asumimos que usuario.getContactos() devuelve la List<Contacto> del usuario.
        Optional<Contacto> emisorContactoOptional = usuario.getContactos()
                .stream()
                .filter(c -> c.getNombre().equals(mensaje.getEmisor()))
                .findFirst();

        emisorContactoOptional.ifPresent(contactoEncontrado -> {
            usuario.getConversaciones().computeIfAbsent(contactoEncontrado, k -> new Conversacion());
            usuario.getConversaciones().get(contactoEncontrado).getMensajes().add(mensaje);
        });

        // Si el emisor no está en el directorio del usuario, el mensaje no se añade a ninguna conversación.
        // Esto puede ocurrir si un usuario desconocido te envía un mensaje.
        if (emisorContactoOptional.isEmpty()) {
            System.err.println("Advertencia: Mensaje recibido de un emisor (" + mensaje.getEmisor() + ") que no está en el directorio del usuario. Mensaje no añadido a ninguna conversación.");
            // Aquí podrías decidir si quieres agregar el Contacto automáticamente o notificar al usuario.
        }
    }

    /**
     * Añade un mensaje saliente a la conversación correspondiente.
     * Si la conversación no existe, se crea una nueva.
     * @param contacto El contacto al que se envía el mensaje.
     * @param mensaje El mensaje saliente que se desea añadir.
     */
    @Override
    public void addMensajeSaliente(Contacto contacto, Mensaje mensaje) {
        // Si la conversación no existe, se crea una nueva automáticamente.
        usuario.getConversaciones().computeIfAbsent(contacto, k -> new Conversacion());
        // Agrega el mensaje a la conversación
        usuario.getConversaciones().get(contacto)
                .getMensajes().add(mensaje);
    }

    /**
     * Marca una conversación con un contacto específico como no pendiente.
     * @param contacto El contacto cuya conversación se desea marcar como no pendiente.
     */
    @Override
    public void setConversacionPendiente(Contacto contacto) {
        Conversacion conversacion = usuario.getConversaciones().get(contacto);
        // Solo marca como no pendiente si la conversación existe y está pendiente.
        if (conversacion != null && conversacion.isPendiente()){
            conversacion.setPendiente(false);
        }
    }
}