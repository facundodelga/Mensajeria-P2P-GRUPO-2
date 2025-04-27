package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Observable;

/**
 * Clase que maneja las entradas de mensajes desde un socket.
 * Extiende Observable para notificar a los observadores cuando se recibe un mensaje.
 */
public class ManejadorEntradas extends Observable implements Runnable {
    private Socket socket;
    private ObjectInputStream entrada;


    /**
     * Constructor de la clase ManejadorEntradas.
     * @param socket El socket desde el cual se recibirán los mensajes.
     */
    public ManejadorEntradas(Socket socket, ObjectInputStream entrada) {
        this.socket = socket;
        this.entrada = entrada;
        addObserver(Controlador.getInstancia());
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

                if (msg instanceof String) {
                    String mensaje = (String) msg;
                    System.out.println("Mensaje de texto recibido: " + mensaje);
                    setChanged();
                    notifyObservers(mensaje);
                } else if (msg instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) msg;
                    System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());
                    setChanged();
                    notifyObservers(mensaje);
                } else if (msg instanceof DirectorioDTO) {
                    DirectorioDTO contactos = (DirectorioDTO) msg;
                    System.out.println("Contacto recibido: " + contactos);
                    setChanged();
                    notifyObservers(contactos);
                } else {
                    System.out.println("Objeto desconocido recibido: " + msg);
                }
            }
        } catch (SocketException e) {
            System.out.println("El socket se ha cerrado.");
        } catch (IOException | ClassNotFoundException e) {

            e.printStackTrace();
        } finally {
            try {
                if (entrada != null) {
                    entrada.close();

                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}