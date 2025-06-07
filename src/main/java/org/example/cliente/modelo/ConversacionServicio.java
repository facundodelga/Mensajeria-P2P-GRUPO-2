package org.example.cliente.modelo;

import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.cliente.modelo.usuario.Usuario;

import java.util.List;

/**
 * Clase que proporciona servicios relacionados con las conversaciones de un usuario.
 * Implementa la interfaz IConversacion.
 */
public class ConversacionServicio implements IConversacion {
    private Usuario usuario; // Assumes Usuario has Map<Contacto, Conversacion> getConversaciones()

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
        // Ensure conversation exists before trying to get messages
        if (usuario.getConversaciones().containsKey(contacto)) {
            return usuario.getConversaciones().get(contacto).getMensajes();
        }
        // Return an empty list if no conversation exists for the contact
        return new java.util.ArrayList<>();
    }

    /**
     * Agrega una nueva conversación con un contacto específico.
     * @param contacto El contacto con el que se desea iniciar una conversación.
     */
    @Override
    public void agregarConversacion(Contacto contacto) {
        System.out.println("Agregando conversacion con: " + contacto.getNombre());
        // Only add if it doesn't already exist to avoid overwriting
        usuario.getConversaciones().putIfAbsent(contacto, new Conversacion());
    }

    /**
     * Añade un mensaje entrante a la conversación correspondiente.
     * Si la conversación no existe, se crea una nueva.
     * @param mensaje El mensaje entrante que se desea añadir.
     */
    @Override
    public void addMensajeEntrante(Mensaje mensaje) {
        // Find or create the Contacto object corresponding to the emitter string
        // This logic should ideally mirror how Contacto objects are managed in AgendaServicio or Controlador.
        // For simplicity here, we'll create a Contacto based on the emitter name.
        // IMPORTANT: This Contacto *must* be the same instance (or `equals()` to)
        // the one stored as a key in `usuario.getConversaciones()`.
        // If not, you'll end up with duplicate conversation entries.
        // It's generally better to get the Contacto object from the main contact list (AgendaServicio).

        // Option 1: Retrieve Contacto from Usuario's known contacts (if you have a way to search by name/ID in Usuario)
        // This is the most robust approach.
        Contacto emisorContacto = null;
        // Assuming Usuario has access to its agenda or a method to find Contacto by name
        // (You might need to pass agendaServicio to ConversacionServicio or modify Usuario)

        // For now, let's create a temporary Contacto for map lookup based on name.
        // This requires Contacto's equals/hashCode to properly compare based on name.
        emisorContacto = new Contacto(mensaje.getEmisor(), null, 0); // Create a Contacto object with the name

        // Use computeIfAbsent with the Contacto object
        usuario.getConversaciones().computeIfAbsent(emisorContacto, k -> {
            System.out.println("Creando nueva conversacion para: " + k.getNombre() + " (mensaje entrante)");
            return new Conversacion();
        });

        // Add the message to the found/created conversation
        usuario.getConversaciones().get(emisorContacto)
                .getMensajes().add(mensaje);
        System.out.println("Mensaje entrante de " + mensaje.getEmisor() + " añadido a la conversación.");
    }

    /**
     * Añade un mensaje saliente a la conversación correspondiente.
     * Si la conversación no existe, se crea una nueva.
     * @param contacto El contacto al que se envía el mensaje.
     * @param mensaje El mensaje saliente que se desea añadir.
     */
    @Override
    public void addMensajeSaliente(Contacto contacto, Mensaje mensaje) {
        // Use computeIfAbsent with the Contacto object
        usuario.getConversaciones().computeIfAbsent(contacto, k -> {
            System.out.println("Creando nueva conversacion para: " + k.getNombre() + " (mensaje saliente)");
            return new Conversacion();
        });
        // agrego el mensaje a la conversacion
        usuario.getConversaciones().get(contacto)
                .getMensajes().add(mensaje);
        System.out.println("Mensaje saliente a " + contacto.getNombre() + " añadido a la conversación.");
    }

    /**
     * Marca una conversación con un contacto específico como no pendiente.
     * @param contacto El contacto cuya conversación se desea marcar como no pendiente.
     */
    @Override
    public void setConversacionPendiente(Contacto contacto) {
        if (usuario.getConversaciones().containsKey(contacto)) { // Check if conversation exists
            if(usuario.getConversaciones().get(contacto).isPendiente()){
                usuario.getConversaciones().get(contacto).setPendiente(false);
                System.out.println("Conversación con " + contacto.getNombre() + " marcada como no pendiente.");
            }
        } else {
            System.out.println("No se encontró conversación con " + contacto.getNombre() + " para marcar como no pendiente.");
        }
    }
}