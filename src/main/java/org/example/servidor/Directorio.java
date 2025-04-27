package org.example.servidor;

import org.example.cliente.modelo.usuario.Contacto;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Directorio implements IDirectorio{
    private Map<String, Contacto> usuarios;
    private Map<Contacto, Socket> sockets;

    public Directorio(){
        usuarios=new HashMap<>();
        sockets=new HashMap<>();
    }

    public Map<Contacto, Socket> getSockets() {
        return sockets;
    }

    public void addSocket(Contacto usuario, Socket socket) {
        sockets.put(usuario, socket);
    }

    public void addUsuario(String nombre, Contacto usuario) {
        usuarios.put(nombre, usuario);
    }

    public void removeUsuario(String nombre, Contacto usuario) {
        usuarios.remove(nombre, usuario);
    }

    public Map<String, Contacto> getUsuarios() {
        return usuarios;
    }


}
