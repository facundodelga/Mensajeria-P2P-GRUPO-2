package org.example.conexion;

import org.example.controlador.Controlador;
import org.example.modelo.mensaje.Mensaje;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;

public class ManejadorEntradas extends Observable implements Runnable {
    private Socket socket;

    public ManejadorEntradas(Socket socket) {
        this.socket = socket;
        addObserver(Controlador.getInstancia());
    }

    @Override
    public void run() {
        try {
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            Mensaje mensaje = (Mensaje) entrada.readObject();

            System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());
            //Si no existe la conexion de salida, la agrego
            if (!Conexion.getInstancia().getConexionesDeSalida().containsKey(mensaje.getEmisor())) {
                ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                Conexion.getInstancia().getConexionesDeSalida().put(mensaje.getEmisor(), salida);
            }

            System.out.println("Mensaje recibido de " + mensaje.getEmisor() + ": " + mensaje.getContenido());


            setChanged();
            notifyObservers(mensaje);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

