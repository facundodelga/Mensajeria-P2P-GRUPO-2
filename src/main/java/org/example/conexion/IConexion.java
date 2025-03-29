package org.example.conexion;

import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.UsuarioDTO;

import java.io.IOException;
import java.net.Socket;

public interface IConexion {
    void configurarServidor(int puerto);

    void iniciarServidor();

    void enviarMensaje(UsuarioDTO usuarioDTO, Mensaje mensaje) throws IOException;

    void agregarConexionDeSalida(String nombre, Socket socket);

    void cerrarConexiones();
}
