package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;

public class ManejadorEntradas extends Observable implements Runnable {
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ManejadorEntradas(Socket socket, ObjectOutputStream salida) throws IOException {
        this.socket = socket;
        this.salida = salida;
        this.entrada = new ObjectInputStream(socket.getInputStream());
        addObserver(Controlador.getInstancia());
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    Object msg = entrada.readObject();
                    if (msg instanceof Mensaje) {
                        Mensaje mensaje = (Mensaje) msg;
                        System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());
                        setChanged();
                        notifyObservers(mensaje);
                    }
                } catch (IOException e) {
                    System.err.println("Error de entrada/salida: " + e.getMessage());
                    break; // Salir del bucle en caso de error
                } catch (ClassNotFoundException e) {
                    System.err.println("Clase no encontrada: " + e.getMessage());
                }
            }
        } finally {
            try {
                entrada.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}