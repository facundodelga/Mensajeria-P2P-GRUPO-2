package org.example.cliente.modelo;

import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.cliente.modelo.usuario.Usuario;

import java.util.*;

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
        return usuario.getConversaciones().get(contacto).getMensajes();
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

    public void agregarConversacion(Contacto contacto, Conversacion conversacion) {
        System.out.println("Agregando conversacion");
        usuario.getConversaciones().put(contacto, conversacion);
    }

    /**
     * Añade un mensaje entrante a la conversación correspondiente.
     * Si la conversación no existe, se crea una nueva.
     * @param mensaje El mensaje entrante que se desea añadir.
     */
    @Override
    public void addMensajeEntrante(Mensaje mensaje) {
        // Me fijo si la conversacion ya existe y si no, la creo (La linea me la recomendo IntelliJ jajaja)
        usuario.getConversaciones().computeIfAbsent(mensaje.getEmisor(), k -> new Conversacion());
        // agrego el mensaje a la conversacion
        usuario.getConversaciones().get(mensaje.getEmisor())
                .getMensajes().add(mensaje);
    }

    /**
     * Añade un mensaje saliente a la conversación correspondiente.
     * Si la conversación no existe, se crea una nueva.
     * @param contacto El contacto al que se envía el mensaje.
     * @param mensaje El mensaje saliente que se desea añadir.
     */
    @Override
    public void addMensajeSaliente(Contacto contacto, Mensaje mensaje) {
        // Me fijo si la conversacion ya existe y si no, la creo (La linea me la recomendo IntelliJ jajaja)
        usuario.getConversaciones().computeIfAbsent(contacto, k -> new Conversacion());
        // agrego el mensaje a la conversacion
        usuario.getConversaciones().get(contacto)
                .getMensajes().add(mensaje);
    }

    /**
     * Marca una conversación con un contacto específico como no pendiente.
     * @param contacto El contacto cuya conversación se desea marcar como no pendiente.
     */
    @Override
    public void setConversacionPendiente(Contacto contacto) {
        if(usuario.getConversaciones().get(contacto).isPendiente()){
            usuario.getConversaciones().get(contacto).setPendiente(false);
        }
    }

    public void setConversaciones(Map<Contacto, Conversacion> conversaciones) {
        usuario.setConversaciones(conversaciones);
    }

    public Map<Contacto, Conversacion> getConversaciones() {
        return usuario.getConversaciones();
    }

}