package org.example.modelo;

import org.example.modelo.conversacion.Conversacion;
import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.Usuario;
import org.example.modelo.usuario.UsuarioDTO;

import java.util.List;

public class ConversacionServicio implements IConversacion {
    private Usuario usuario;

    public ConversacionServicio(Usuario usuario){
        this.usuario = usuario;
    }
    @Override
    public List<Mensaje> getMensajes(UsuarioDTO contacto){
        return usuario.getConversaciones().get(contacto).getMensajes();
    }

    @Override
    public void agregarConversacion(UsuarioDTO contacto) {
        System.out.println("Agregando conversacion");
        usuario.getConversaciones().put(contacto, new Conversacion());

    }

    @Override
    public void addMensajeEntrante(Mensaje mensaje) {
        //Me fijo si la conversacion ya existe y si no, la creo (La linea me la recomendo IntelliJ jajaja)
        usuario.getConversaciones().computeIfAbsent(mensaje.getEmisor(), k -> new Conversacion());
        //agrego el mensaje a la conversacion
        usuario.getConversaciones().get(mensaje.getEmisor())
                .getMensajes().add(mensaje);
    }

    @Override
    public void addMensajeSaliente(UsuarioDTO contacto, Mensaje mensaje) {
        //Me fijo si la conversacion ya existe y si no, la creo (La linea me la recomendo IntelliJ jajaja)
        usuario.getConversaciones().computeIfAbsent(contacto, k -> new Conversacion());
        //agrego el mensaje a la conversacion
        usuario.getConversaciones().get(contacto)
                .getMensajes().add(mensaje);
    }

    @Override
    public void setConversacionPendiente(UsuarioDTO contacto) {
        if(usuario.getConversaciones().get(contacto).isPendiente()){
            usuario.getConversaciones().get(contacto).setPendiente(false);
        }
    }
}
