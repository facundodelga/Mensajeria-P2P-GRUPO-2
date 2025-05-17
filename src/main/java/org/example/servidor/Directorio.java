package org.example.servidor;

import org.example.cliente.modelo.usuario.Contacto;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Directorio implements IDirectorio {
    private  Map<String, Contacto> usuarios;
    private  transient Map<Contacto, Socket> sockets;

    public Directorio() {
        usuarios = new HashMap<>();
        sockets = new HashMap<>();
    }

    @Override
    public Map<Contacto, Socket> getSockets() {
        return sockets;
    }

    @Override
    public void addSocket(Contacto usuario, Socket socket) {
        sockets.put(usuario, socket);
    }

    @Override
    public Map<String, Contacto> getUsuarios() {
        return usuarios;
    }

    @Override
    public void addUsuario(String nombre, Contacto usuario) {
        usuarios.put(nombre, usuario);
    }

    @Override
    public void removeUsuario(String nombre, Contacto usuario) {
        usuarios.remove(nombre, usuario);
    }

    @Override
    public Socket getSocket(Contacto receptor) {
        return sockets.get(receptor);
    }

    @Override
    public Directorio clonar() {
        Directorio copia = Clonador.deepClone(this);

        // Restaurar los sockets manualmente
        for (Map.Entry<Contacto, Socket> entry : this.sockets.entrySet()) {
            copia.sockets.put(new Contacto(entry.getKey()), entry.getValue()); // Clonar Contacto, compartir Socket
        }

        return copia;
    }

}