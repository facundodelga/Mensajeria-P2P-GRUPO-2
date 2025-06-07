// src/main/java/org/example/cliente/conexion/IConexion.java
package org.example.cliente.conexion;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.IOException;
import java.security.PublicKey; // Importar PublicKey
import java.util.ArrayList;
import java.util.Map;

public interface IConexion extends Runnable {
    void conectarServidor(Contacto usuario) throws PuertoEnUsoException, IOException, PerdioConexionException;

    void esperarMensajes();

    // El mensaje que se envía es con contenido en texto plano. La conexión se encarga de cifrarlo.
    void enviarMensaje(Contacto usuarioDTO, Mensaje mensaje) throws IOException, EnviarMensajeException, PerdioConexionException;

    void cerrarConexiones();
    void obtenerMensajesPendientes();
    void reconectar() throws IOException;
    void conectar(Map.Entry<String, Integer> entry) throws IOException, PuertoEnUsoException;
    ArrayList<Contacto> obtenerContactos() throws PerdioConexionException;

    // --- Nuevos métodos para el intercambio de claves ---

    /**
     * Inicia el proceso de intercambio de claves con un contacto.
     * Esto implica enviar nuestra clave pública DH y esperar la del otro.
     * @param contactoRemoto El contacto con el que se desea establecer la clave.
     * @throws Exception Si ocurre un error durante el intercambio de claves (generación/envío).
     */
    void iniciarIntercambioDeClaves(Contacto contactoRemoto) throws Exception;

    /**
     * Permite a la Conexión acceder a la clave pública DH del usuario local,
     * la cual está gestionada por el Controlador.
     * @return La clave pública DH del usuario local.
     */
    PublicKey getMiClavePublicaDH();
}