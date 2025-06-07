package org.example.cliente.modelo;

import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.cliente.modelo.usuario.Usuario;

import java.util.List;
import java.util.Map; // Necesario para trabajar con el mapa de conversaciones

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
        // Asegúrate de que la conversación existe antes de intentar obtener mensajes.
        // Si no existe, devuelve una lista vacía o maneja el error según tu lógica.
        Conversacion conv = usuario.getConversaciones().get(contacto);
        if (conv != null) {
            return conv.getMensajes();
        }
        return new java.util.ArrayList<>(); // Devuelve una lista vacía si no hay conversación
    }

    /**
     * Agrega una nueva conversación con un contacto específico.
     * @param contacto El contacto con el que se desea iniciar una conversación.
     */
    @Override
    public void agregarConversacion(Contacto contacto) {
        System.out.println("Agregando conversacion");
        // Solo agrega si no existe ya para evitar sobrescribir una existente
        usuario.getConversaciones().putIfAbsent(contacto, new Conversacion());
    }

    /**
     * Añade un mensaje entrante a la conversación correspondiente.
     * Si la conversación no existe, se crea una nueva.
     * @param mensaje El mensaje entrante que se desea añadir.
     */
    @Override
    public void addMensajeEntrante(Mensaje mensaje) {
        // Necesitamos encontrar el objeto Contacto correspondiente al emisor del mensaje.
        // Si tu Contacto tiene un constructor solo con el nombre, lo usamos.
        // Asumo que el Contacto tiene un buen `equals` y `hashCode` basado en el nombre.
        Contacto emisorContacto = new Contacto(mensaje.getEmisor(), null, 0); // Crea un Contacto "temporal" con el nombre

        // Me fijo si la conversacion ya existe y si no, la creo
        // Usa el objeto Contacto como clave
        usuario.getConversaciones().computeIfAbsent(emisorContacto, k -> new Conversacion());

        // Agrego el mensaje a la conversación
        usuario.getConversaciones().get(emisorContacto) // Usa el objeto Contacto para obtener la Conversacion
                .getMensajes().add(mensaje);

        // Opcional: Marcar la conversación como pendiente si es un mensaje entrante
        usuario.getConversaciones().get(emisorContacto).setPendiente(true);
    }

    /**
     * Añade un mensaje saliente a la conversación correspondiente.
     * Si la conversación no existe, se crea una nueva.
     * @param contacto El contacto al que se envía el mensaje.
     * @param mensaje El mensaje saliente que se desea añadir.
     */
    @Override
    public void addMensajeSaliente(Contacto contacto, Mensaje mensaje) {
        // Me fijo si la conversacion ya existe y si no, la creo
        // Usa el objeto Contacto como clave
        usuario.getConversaciones().computeIfAbsent(contacto, k -> new Conversacion());
        // agrego el mensaje a la conversacion
        usuario.getConversaciones().get(contacto) // Usa el objeto Contacto para obtener la Conversacion
                .getMensajes().add(mensaje);
    }

    /**
     * Marca una conversación con un contacto específico como no pendiente.
     * @param contacto El contacto cuya conversación se desea marcar como no pendiente.
     */
    @Override
    public void setConversacionPendiente(Contacto contacto) {
        Conversacion conv = usuario.getConversaciones().get(contacto);
        if (conv != null) {
            conv.setPendiente(false);
        }
    }
}