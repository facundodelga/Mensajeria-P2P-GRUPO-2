package org.example.cliente.conexion;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public interface IConexion extends Runnable {
    void conectarServidor(Contacto usuario) throws PuertoEnUsoException, IOException, PerdioConexionException;

    void esperarMensajes();

    void enviarMensaje(Contacto usuarioDTO, Mensaje mensaje) throws IOException, EnviarMensajeException, PerdioConexionException;

    void cerrarConexiones();
    void obtenerMensajesPendientes();
    void reconectar() throws IOException;
    void conectar(Map.Entry<String, Integer> entry) throws IOException, PuertoEnUsoException;

    ArrayList<Contacto> obtenerContactos() throws PerdioConexionException;
}
