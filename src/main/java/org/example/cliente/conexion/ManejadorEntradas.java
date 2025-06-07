package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto; // Needed if Contacto objects are also received directly
import org.example.servidor.DirectorioDTO;

import java.io.IOException;
import java.io.InputStream; // Not directly used with ObjectInputStream, but kept if needed for other streams
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream; // Not directly used in ManejadorEntradas, but might be needed if sending a response
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList; // Not directly used in ManejadorEntradas, but might be needed for other types
import java.util.Observable; // No longer needed
import java.util.Observer; // No longer needed

/**
 * Clase que maneja las entradas de objetos desde un socket.
 * Notifica directamente al Controlador sobre los objetos recibidos.
 */
public class ManejadorEntradas implements Runnable { // Removed 'extends Observable'
    private Socket socket;
    private ObjectInputStream entrada;
    private Controlador controlador; // Direct reference to the controller

    /**
     * Constructor de la clase ManejadorEntradas.
     * @param socket El socket desde el cual se recibirán los mensajes.
     * @param entrada El ObjectInputStream ya inicializado desde el socket.
     * @param controlador La instancia del Controlador para notificar los eventos.
     */
    public ManejadorEntradas(Socket socket, ObjectInputStream entrada, Controlador controlador) {
        this.socket = socket;
        this.entrada = entrada;
        this.controlador = controlador; // Store the controller instance
        // No more addObserver calls as we're not using Observable/Observer pattern here
    }

    /**
     * Lee objetos desde el socket y notifica al controlador.
     */
    @Override
    public void run() {
        try {
            while (!socket.isClosed()) { // Loop while socket is open
                System.out.println("Esperando objeto en el cliente (ManejadorEntradas)...");
                Object msg = entrada.readObject();
                System.out.println("Objeto recibido de tipo: " + msg.getClass().getName());

                if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenidoCifrado());
                    if (controlador != null) {
                        controlador.recibirMensaje(mensaje); // Call the specific method for messages
                    }
                } else if (msg instanceof DirectorioDTO) {
                    DirectorioDTO contactos = (DirectorioDTO) msg;
                    System.out.println("DirectorioDTO recibido: " + contactos);
                    if (controlador != null) {
                        controlador.update(null, contactos); // Call update for other data types
                    }
                } else if (msg instanceof String) {
                    String stringMessage = (String) msg;
                    System.out.println("String recibido del servidor: " + stringMessage);
                    // Handle specific string commands from the server here if any (e.g., "SERVER_SHUTDOWN")
                    // Or pass it to the controller's update if it's a general notification.
                    if (controlador != null) {
                        // This might be for simple server notifications or errors.
                        // Decide how the controller should react to a String message.
                        // For now, it's passed as an update.
                        controlador.update(null, stringMessage);
                    }
                } else {
                    System.out.println("Objeto desconocido recibido: " + msg);
                    if (controlador != null) {
                        controlador.update(null, new Exception("Tipo de objeto desconocido recibido del servidor."));
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("El socket del cliente se ha cerrado o la conexión se ha reseteado: " + e.getMessage());
            if (controlador != null) {
                // Notify the controller about the lost connection
                controlador.update(null, new PerdioConexionException("Conexión con el servidor perdida: " + e.getMessage()));
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error de lectura en ManejadorEntradas: " + e.getMessage());
            e.printStackTrace();
            if (controlador != null) {
                controlador.update(null, new Exception("Error al leer datos del servidor: " + e.getMessage(), e));
            }
        } finally {
            try {
                if (entrada != null) {
                    entrada.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                System.out.println("ManejadorEntradas finalizado y recursos cerrados.");
            } catch (IOException e) {
                System.err.println("Error al cerrar recursos en ManejadorEntradas: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}