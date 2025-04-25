package org.example.cliente.conexion;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Clase que representa una conexión de servidor.
 * Implementa la interfaz IConexion.
 */
public class Conexion implements IConexion {

    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    /**
     * Constructor de la clase Conexion.
     */
    public Conexion() {
    }

    /**
     * Verifica si un puerto está en uso.
     * @param puerto El puerto a verificar.
     * @return true si el puerto está en uso, false en caso contrario.
     */
    private boolean elPuertoEstaEnUso(int puerto) {
        try (ServerSocket ignored = new ServerSocket(puerto)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * Inicia el servidor en el puerto especificado.
     * @param puerto El puerto en el que se configurará el servidor.
     */
    @Override
    public void conectarServidor(int puerto) throws PuertoEnUsoException {
//        if (elPuertoEstaEnUso(puerto)) {
//            throw new PuertoEnUsoException("El puerto " + puerto + " ya está en uso.");
//        }
        try {
            this.socket = new Socket("127.0.0.1", puerto);
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());

            Thread.sleep(50);

            Contacto usuario = new Contacto("Usuario", "localhost", puerto);
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

    public void obtenerMensajesPendientes(){
        try {
            this.salida.writeObject("MensajesPendientes");
            this.salida.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Contacto> obtenerContactos(){
        try{
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.salida.writeObject("Contactos");
            this.salida.flush();

            this.entrada = new ObjectInputStream(socket.getInputStream());

            ArrayList<Contacto> contactos;
            contactos = (ArrayList<Contacto>) this.entrada.readObject();
            return contactos;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Espera conexiones entrantes y maneja los mensajes recibidos.
     */
    @Override
    public void esperarMensajes() {

        new Thread(new ManejadorEntradas(socket, entrada)).start();

    }

    /**
     * Envía un mensaje a un usuario específico.
     * @param usuarioDTO El usuario al que se enviará el mensaje.
     * @param mensaje El mensaje que se enviará.
     * @throws EnviarMensajeException Si ocurre un error al enviar el mensaje.
     */
    @Override
    public void enviarMensaje(Contacto usuarioDTO, Mensaje mensaje) throws EnviarMensajeException {
        System.out.println("Intentando enviar mensaje a " + usuarioDTO);
        try {
            System.out.println("Enviando mensaje a " + usuarioDTO + ": " + mensaje.getContenido());
            salida.writeObject(mensaje);
            salida.flush();

        } catch (IOException e) {
            throw new EnviarMensajeException("Error al enviar el mensaje a " + usuarioDTO, e);
        }
    }

    /**
     * Cierra las conexiones del servidor y del socket.
     */
    @Override
    public void cerrarConexiones(){
        try {
            if(socket != null) {
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que ejecuta el hilo del servidor para esperar mensajes.
     */
    @Override
    public void run() {
        esperarMensajes();
    }

    /**
     * Obtiene el Socket de la conexión actual.
     * @return El Socket de la conexión actual.
     */
    public Socket getSocket() {
        return socket;
    }
}