package org.example.cliente.conexion;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.IOException;
import java.util.ArrayList;

public interface IConexion extends Runnable {
    void conectarServidor(Contacto usuario,int puerto) throws PuertoEnUsoException;

    void esperarMensajes();

    void enviarMensaje(Contacto usuarioDTO, Mensaje mensaje) throws IOException, EnviarMensajeException;

    void cerrarConexiones();
    void obtenerMensajesPendientes();

    ArrayList<Contacto> obtenerContactos();
}
