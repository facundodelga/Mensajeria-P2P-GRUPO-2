package org.example.cliente.conexion;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
        if (elPuertoEstaEnUso(puerto)) {
            throw new PuertoEnUsoException("El puerto " + puerto + " ya está en uso.");
        }
        try{
            this.socket = new Socket("localhost", puerto);
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Contacto usuario = new Contacto("Usuario", "localhost", puerto);

            this.salida.writeObject(usuario);
            this.salida.flush();

            String estaOcupado = (String) this.entrada.readObject(); // Espera la confirmación del servidor

            if(estaOcupado.equals("El nickname ya está en uso.")){
                //queda provisorio esto
                throw new PuertoEnUsoException("El nickname ya está en uso.");
            } else {
                System.out.println(estaOcupado);
                System.out.println("Conexión establecida con el servidor en el puerto: " + puerto);
            }

            System.out.println("Servidor configurado en el puerto: " + puerto);
            System.out.println("Servidor iniciado...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Espera conexiones entrantes y maneja los mensajes recibidos.
     */
    @Override
    public void esperarMensajes() {
        try {
                //this.socket = this.socketServer.accept();
                new Thread(new ManejadorEntradas(this.socket)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        cerrarConexiones();
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