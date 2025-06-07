// src/main/java/org/example/cliente/conexion/IConexion.java
package org.example.cliente.conexion;

import org.example.cliente.modelo.usuario.Contacto;
import org.example.cliente.modelo.mensaje.Mensaje;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observer;
import java.security.KeyPair;
import java.lang.Runnable; // Importar Runnable

// CAMBIO AQUÍ: IConexion también debe extender Runnable
public interface IConexion extends Runnable { //
    // ... otros métodos existentes en IConexion ...

    void esperarMensajes();
    ArrayList<Contacto> obtenerContactos() throws PerdioConexionException, IOException, ClassNotFoundException;
    void enviarMensaje(Contacto contactoRemoto, Mensaje mensaje) throws EnviarMensajeException, IOException, PerdioConexionException;
    void reconectar() throws IOException;
    void conectarServidor(Contacto usuario) throws PuertoEnUsoException, IOException, PerdioConexionException;
    void obtenerMensajesPendientes();
    void conectar(Map.Entry<String, Integer> entry) throws IOException, PuertoEnUsoException;
    void cerrarConexiones();
    void iniciarIntercambioDeClaves(Contacto contactoRemoto) throws Exception;

    // Métodos de Observable/Getter que añadimos previamente (asegúrate de que estén aquí)
    void addObserver(Observer o);
    void setMiParClavesDH(KeyPair miParClavesDH);
    PublicKey getMiClavePublicaDH();
}