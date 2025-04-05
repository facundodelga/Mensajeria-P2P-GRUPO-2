package org.example.conexion;

import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.UsuarioDTO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Clase que representa una conexión de servidor.
 * Implementa la interfaz IConexion.
 */
public class Conexion implements IConexion {
    private ServerSocket socketServer;
    private Socket socket;

    /**
     * Constructor de la clase Conexion.
     */
    public Conexion() {
    }

    /**
     * Inicia el servidor en el puerto especificado.
     * @param puerto El puerto en el que se configurará el servidor.
     */
    @Override
    public void iniciarServidor(int puerto) {
        try {
            this.socketServer = new ServerSocket(puerto);
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
            while(true){
                System.out.println("Esperando conexiones...");
                this.socket = this.socketServer.accept();
                new Thread(new ManejadorEntradas(this.socket)).start();
            }
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
    public void enviarMensaje(UsuarioDTO usuarioDTO, Mensaje mensaje) throws EnviarMensajeException {
        System.out.println("Intentando enviar mensaje a " + usuarioDTO);
        try {
            Socket socketSalida = new Socket(usuarioDTO.getIp(), usuarioDTO.getPuerto());
            ObjectOutputStream salida = new ObjectOutputStream(socketSalida.getOutputStream());
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
            this.socket.close();
            this.socketServer.close();
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
     * Obtiene el ServerSocket del servidor.
     * @return El ServerSocket del servidor.
     */
    public ServerSocket getSocketServer() {
        return socketServer;
    }

    /**
     * Obtiene el Socket de la conexión actual.
     * @return El Socket de la conexión actual.
     */
    public Socket getSocket() {
        return socket;
    }
}