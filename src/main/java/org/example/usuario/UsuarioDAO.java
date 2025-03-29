package org.example.usuario;

import org.example.conversacion.Conversacion;
import org.example.mensaje.Mensaje;

public class UsuarioDAO {

    private Usuario usuario;
    public UsuarioDAO(Usuario usuario){
        this.usuario = usuario;
    }

    public String getNombre() {
        return usuario.getNombre();
    }

    public String getIp() {
        return usuario.getIp();
    }

    public int getPuerto() {
        return usuario.getPuerto();
    }

    public void addContacto(UsuarioDTO contacto) {
        usuario.addContacto(contacto);
    }

    public void addMensaje(UsuarioDTO emisor, Mensaje mensaje) {
        //Me fijo si la conversacion ya existe y si no, la creo (La linea me la recomendo IntelliJ jajaja)
        usuario.getConversaciones().computeIfAbsent(mensaje.getEmisor(), k -> new Conversacion());
        usuario.addMensaje(emisor, mensaje);
    }






}
