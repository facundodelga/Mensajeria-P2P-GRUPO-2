package org.example.sistema;

import org.example.conversacion.Conversacion;
import org.example.mensaje.Mensaje;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ManejadorCliente implements Runnable {
    private Socket socket;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            Mensaje mensaje = (Mensaje) entrada.readObject();

            System.out.println("Mensaje recibido de " + mensaje.getUsuario() + ": " + mensaje.getContenido());
            //Si no existe la conexion de salida, la agrego
            if (!Sistema.getInstancia().getConexionesDeSalida().containsKey(mensaje.getUsuario())) {
                ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                Sistema.getInstancia().getConexionesDeSalida().put(mensaje.getUsuario(), salida);
            }

            System.out.println("Mensaje recibido de " + mensaje.getUsuario() + ": " + mensaje.getContenido());

            //Me fijo si la conversacion ya existe y si no, la creo (La linea me la recomendo IntelliJ jajaja)
            Sistema.getInstancia().getUsuario().getConversaciones().computeIfAbsent(mensaje.getUsuario(), k -> new Conversacion());
            Sistema.getInstancia().getUsuario().getConversaciones().get(mensaje.getUsuario()).getMensajes().add(mensaje);


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

