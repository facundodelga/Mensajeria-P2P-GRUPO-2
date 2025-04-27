package org.example.cliente.conexion;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Conexion implements IConexion {
    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public Conexion() {
    }

    @Override
    public void conectarServidor(Contacto usuario, int puerto) throws PuertoEnUsoException {
        try {
            this.socket = new Socket("127.0.0.1", puerto);
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());

            Thread.sleep(50);

            this.salida.writeObject(usuario);
            this.salida.flush();

            String estaOcupado = (String) this.entrada.readObject();

            if ("El nickname ya está en uso.".equals(estaOcupado)) {
                throw new PuertoEnUsoException("El nickname ya está en uso.");
            } else {
                System.out.println("Conexión establecida con el servidor en el puerto: " + puerto);
            }
        } catch (ConnectException e) {
            System.err.println("Error: No se pudo conectar al servidor en el puerto " + puerto + ". Asegúrese de que el servidor esté en ejecución.");
        } catch (UnknownHostException e) {
            System.err.println("Error: Host desconocido.");
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void obtenerMensajesPendientes() {
        try {
            this.salida.writeObject("MensajesPendientes");
            this.salida.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Contacto> obtenerContactos() {
        try {
            this.salida.writeObject("Contactos");
            this.salida.flush();

            ArrayList<Contacto> contactos = (ArrayList<Contacto>) this.entrada.readObject();
            return contactos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void esperarMensajes() throws IOException {
        new Thread(new ManejadorEntradas(socket, salida)).start();
    }

    @Override
    public void enviarMensaje(Contacto usuarioDTO, Mensaje mensaje) throws EnviarMensajeException, IOException {
        if (salida == null) {
            throw new IOException("El canal de salida no está inicializado.");
        } else {
            try {
                salida.writeObject(mensaje);
                salida.flush();
            } catch (IOException e) {
                throw new EnviarMensajeException("Error al enviar el mensaje a " + usuarioDTO, e);
            }
        }
    }

    @Override
    public void cerrarConexiones() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            esperarMensajes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }
}