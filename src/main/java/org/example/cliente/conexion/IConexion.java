package org.example.cliente.conexion;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.IOException;

public interface IConexion extends Runnable {
    void conectarServidor(int puerto) throws PuertoEnUsoException;

    void esperarMensajes();

    void enviarMensaje(Contacto usuarioDTO, Mensaje mensaje) throws IOException, EnviarMensajeException;

    void cerrarConexiones();
}
