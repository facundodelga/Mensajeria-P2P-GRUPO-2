package org.example.conexion;

import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.UsuarioDTO;

import java.net.Socket;

public interface IConexion {
    void configurarServidor(int puerto);

    void iniciarServidor();

    void enviarMensaje(UsuarioDTO usuarioDTO, Mensaje mensaje);

    void agregarConexionDeSalida(String nombre, Socket socket);

    void cerrarConexiones();
}
