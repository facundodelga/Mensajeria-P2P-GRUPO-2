package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Observable;

/**
 * Clase que maneja las entradas de mensajes desde un socket.
 * Extiende Observable para notificar a los observadores cuando se recibe un mensaje.
 */
public class ManejadorEntradas extends Observable implements Runnable {
    private Socket socket;

    /**
     * Constructor de la clase ManejadorEntradas.
     * @param socket El socket desde el cual se recibirán los mensajes.
     */
    public ManejadorEntradas(Socket socket) {
        this.socket = socket;
        addObserver(Controlador.getInstancia());
    }

    /**
     * Método que se ejecuta cuando el hilo se inicia.
     * Lee mensajes desde el socket y notifica a los observadores.
     */
    @Override
    public void run() {
        try {
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            Mensaje mensaje = (Mensaje) entrada.readObject();

            System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());

            setChanged();
            notifyObservers(mensaje);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}