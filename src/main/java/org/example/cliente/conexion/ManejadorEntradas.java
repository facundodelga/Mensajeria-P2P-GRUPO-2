package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO; // Make sure this is imported

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList; // Not strictly needed here
import java.util.Observable; // Keep, as ManejadorEntradas extends Observable

/**
 * Clase que maneja las entradas de mensajes desde un socket.
 * Extiende Observable para notificar a los observadores cuando se recibe un mensaje.
 */
public class ManejadorEntradas extends Observable implements Runnable {
    private Socket socket;
    private ObjectInputStream entrada;
    private Controlador controlador; // Hold a reference to the Controlador

    /**
     * Constructor de la clase ManejadorEntradas.
     * @param socket El socket desde el cual se recibirán los mensajes.
     * @param entrada El ObjectInputStream para leer objetos del socket.
     * @param controlador La instancia del Controlador para la comunicación directa de mensajes.
     */
    public ManejadorEntradas(Socket socket, ObjectInputStream entrada, Controlador controlador) {
        this.socket = socket;
        this.entrada = entrada;
        this.controlador = controlador; // Assign the passed controller instance

        // Only add Controlador as an observer for non-message related updates (like DirectorioDTO or errors)
        // Messages (Mensaje) will be handled via direct method call to controlador.recibirMensaje()
        addObserver(this.controlador); // Controlador will receive DirectoryDTO and error updates
        // Remove: addObserver(conexion); // Not needed as Conexion's update is empty
    }

    /**
     * Lee mensajes desde el socket y notifica a los observadores.
     */
    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("Esperando objeto en el cliente...");
                Object msg = entrada.readObject();
                System.out.println("Objeto recibido de tipo: " + msg.getClass().getName());

                if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenidoCifrado()); // Log encrypted content
                    // Direct call to controller for message processing
                    // This avoids the Observer pattern for messages, making flow clearer
                    controlador.recibirMensaje(mensaje);
                } else if (msg instanceof DirectorioDTO) {
                    DirectorioDTO contactos = (DirectorioDTO) msg;
                    System.out.println("DirectorioDTO recibido: " + contactos.getContactos().size() + " contactos.");
                    // Notify observers for DirectoryDTO, as Controlador's update handles this
                    setChanged();
                    notifyObservers(contactos);
                } else if (msg instanceof String) {
                    String serverMessage = (String) msg;
                    System.out.println("Mensaje de texto del servidor recibido: " + serverMessage);
                    // Decide if this string needs to be passed to controller.update()
                    // or if it's an internal handshake. For now, pass it if it's not a handshake.
                    // If it's a "CONNECT_ACK" or "NICKNAME_IN_USE" this should be handled by Conexion.conectar()
                    // If it's a general server notification, pass to controller.
                    // Assuming this is a general notification for now.
                    setChanged();
                    notifyObservers(serverMessage);
                } else {
                    System.out.println("Objeto desconocido recibido: " + msg);
                    // Optionally notify observers of unknown objects/errors
                    setChanged();
                    notifyObservers(new Exception("Objeto desconocido recibido: " + msg.getClass().getName()));
                }
            }
        } catch (SocketException e) {
            System.out.println("El socket del ManejadorEntradas se ha cerrado o reseteado: " + e.getMessage());
            // Notify the controller that connection might be lost
            setChanged();
            notifyObservers(new PerdioConexionException("Conexión perdida con el servidor: " + e.getMessage()));
        } catch (EOFException e) {
            System.out.println("Fin de flujo de entrada alcanzado (Server closed connection).");
            setChanged();
            notifyObservers(new PerdioConexionException("El servidor cerró la conexión inesperadamente."));
        } catch (IOException e) {
            System.err.println("Error de E/S en ManejadorEntradas: " + e.getMessage());
            e.printStackTrace();
            setChanged();
            notifyObservers(new IOException("Error de E/S en el manejo de entradas: " + e.getMessage(), e)); // Notify with original exception
        } catch (ClassNotFoundException e) {
            System.err.println("Clase no encontrada al deserializar objeto en ManejadorEntradas: " + e.getMessage());
            e.printStackTrace();
            setChanged();
            notifyObservers(new ClassNotFoundException("Error de deserialización: " + e.getMessage(), e));
        } finally {
            // Important: Do NOT close `socket` here, `Conexion` owns the socket lifecycle.
            // Only close the `entrada` stream if it's the specific responsibility of this thread.
            // If `Conexion` manages the ObjectInputStream, this might also be problematic.
            // Typically, the entity that CREATES the stream should CLOSE it.
            // In your case, Conexion creates it, so Conexion should close it in cerrarConexiones().
            try {
                if (entrada != null) {
                    // entrada.close(); // Consider commenting this out if Conexion manages it fully
                    // and only closing it in Conexion.cerrarConexiones()
                }
                // socket.close(); // Definitely DO NOT close the socket here. Conexion manages it.
            } catch (Exception e) { // Catch generic Exception to log any issues with cleanup
                System.err.println("Error al intentar cerrar recursos en finally de ManejadorEntradas: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}