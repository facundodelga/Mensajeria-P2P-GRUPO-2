package org.example.modelo;

import org.example.modelo.conversacion.Conversacion;
import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.Usuario;
import org.example.modelo.usuario.UsuarioDTO;

import java.util.List;

public class UsuarioDAO implements IUsuarioDAO {
    private Usuario usuario;
    public UsuarioDAO(Usuario usuario){
        this.usuario = usuario;
    }

    @Override
    public String getNombre() {
        return usuario.getNombre();
    }

    @Override
    public String getIp() {
        return usuario.getIp();
    }

    @Override
    public int getPuerto() {
        return usuario.getPuerto();
    }

    @Override
    public void addContacto(UsuarioDTO contacto) {
        usuario.getContactos().add(contacto);
    }

    @Override
    public void addMensaje(Mensaje mensaje) {
        //Me fijo si la conversacion ya existe y si no, la creo (La linea me la recomendo IntelliJ jajaja)
        usuario.getConversaciones().computeIfAbsent(mensaje.getEmisor(), k -> new Conversacion());
        //agrego el mensaje a la conversacion
        usuario.getConversaciones().get(mensaje.getEmisor())
                .getMensajes().add(mensaje);
    }
    @Override
    public List<Mensaje> getMensajes(UsuarioDTO contacto){
        return usuario.getConversaciones().get(contacto).getMensajes();
    }

    @Override
    public void setConversacionPendiente(UsuarioDTO contacto) {
        if(usuario.getConversaciones().get(contacto).isPendiente()){
            usuario.getConversaciones().get(contacto).setPendiente(false);
        }
    }




}
