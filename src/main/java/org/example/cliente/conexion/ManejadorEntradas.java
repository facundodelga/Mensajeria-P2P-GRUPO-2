// src/main/java/org/example/cliente/conexion/ManejadorEntradas.java
package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;

/**
 * Clase que maneja las entradas de mensajes desde un socket.
 * Extiende Observable para notificar a los observadores cuando se recibe un mensaje.
 */
public class ManejadorEntradas extends Observable implements Runnable {
    private Socket socket;
    private ObjectInputStream entrada;
    // La referencia a 'conexion' ya no es necesaria aquí para la lógica de errores
    // private Conexion conexion;

    /**
     * Constructor de la clase ManejadorEntradas.
     * @param socket El socket desde el cual se recibirán los mensajes.
     * @param entrada El ObjectInputStream para leer objetos.
     */
    public ManejadorEntradas(Socket socket, ObjectInputStream entrada /*, Conexion conexion */) {
        this.socket = socket;
        this.entrada = entrada;
        // this.conexion = conexion; // Ya no necesitamos esta referencia aquí
        addObserver(Controlador.getInstancia()); // El Controlador es el principal observador
    }

    /**
     * Lee objetos desde el socket y notifica a los observadores (principalmente al Controlador).
     */
    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("ManejadorEntradas: Esperando objeto...");
                Object msg = entrada.readObject();
                System.out.println("ManejadorEntradas: Objeto recibido de tipo: " + msg.getClass().getName());

                // Notificar al Controlador para que maneje el objeto recibido
                setChanged();
                notifyObservers(msg);
            }
        } catch (SocketException e) {
            System.err.println("ManejadorEntradas: El socket se ha cerrado inesperadamente para " + socket.getInetAddress() + ":" + socket.getPort() + ". Notificando al Controlador.");
            // Notificar al Controlador que la conexión se perdió
            setChanged();
            notifyObservers("CONNECTION_LOST"); // Enviar una señal específica
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ManejadorEntradas: Error al leer objeto del stream: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Asegurarse de que los streams y el socket se cierren cuando este hilo termine.
            // Es crucial para liberar recursos.
            try {
                if (entrada != null) {
                    entrada.close();
                    entrada = null;
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    socket = null;
                }
                System.out.println("ManejadorEntradas: Streams y socket cerrados. Hilo terminado.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}