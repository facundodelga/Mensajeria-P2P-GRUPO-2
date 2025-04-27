package org.example.servidor;

import org.example.cliente.modelo.usuario.Contacto;

import java.net.Socket;
import java.util.Map;

public interface IDirectorio {
    void addUsuario(String nombre, Contacto usuario);
    void removeUsuario(String nombre, Contacto usuario);
    Map<Contacto, Socket> getSockets();
    void addSocket(Contacto usuario, Socket socket);

    Map<String, Contacto> getUsuarios();
}
