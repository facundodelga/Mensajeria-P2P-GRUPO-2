package org.example.conexion;

import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.UsuarioDTO;

import java.io.IOException;
import java.net.Socket;

public interface IConexion extends Runnable {
    void iniciarServidor(int puerto) throws PuertoEnUsoException;

    void esperarMensajes();

    void enviarMensaje(UsuarioDTO usuarioDTO, Mensaje mensaje) throws IOException, EnviarMensajeException;

    void cerrarConexiones();
}
