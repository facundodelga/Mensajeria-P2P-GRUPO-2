package org.example.modelo;

import org.example.modelo.conversacion.Conversacion;
import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.Usuario;
import org.example.modelo.usuario.UsuarioDTO;

import java.util.List;

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
    public List<Mensaje> getMensajes(UsuarioDTO contacto){
        return usuario.getConversaciones().get(contacto).getMensajes();
    }

    /**
     * Agrega una nueva conversación con un contacto específico.
     * @param contacto El contacto con el que se desea iniciar una conversación.
     */
    @Override
    public void agregarConversacion(UsuarioDTO contacto) {
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
    public void addMensajeSaliente(UsuarioDTO contacto, Mensaje mensaje) {
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
    public void setConversacionPendiente(UsuarioDTO contacto) {
        if(usuario.getConversaciones().get(contacto).isPendiente()){
            usuario.getConversaciones().get(contacto).setPendiente(false);
        }
    }
}