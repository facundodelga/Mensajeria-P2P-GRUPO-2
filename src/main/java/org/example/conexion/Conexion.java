package org.example.conexion;

import org.example.modelo.mensaje.Mensaje;
import org.example.modelo.usuario.UsuarioDTO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Conexion implements IConexion {
    private ServerSocket socketServer;
    private Socket socket;
    public Conexion() {
    }
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

    @Override
    public void enviarMensaje(UsuarioDTO usuarioDTO, Mensaje mensaje) {
        System.out.println("Intentando enviar mensaje a " + usuarioDTO);


        try {
            Socket socketSalida = new Socket(usuarioDTO.getIp(),usuarioDTO.getPuerto());
            ObjectOutputStream salida = new ObjectOutputStream(socketSalida.getOutputStream());
            System.out.println("Enviando mensaje a " + usuarioDTO + ": " + mensaje.getContenido());
            salida.writeObject(mensaje);
            salida.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void agregarConexionDeSalida(String nombre, Socket socket) {
        try {
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Conexión de salida agregada para " + nombre);
            UsuarioDTO usuarioDTO = new UsuarioDTO(nombre, socket.getInetAddress().getHostAddress(), socket.getPort());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cerrarConexiones(){
        try {

            this.socket.close();
            this.socketServer.close();

        } catch (IOException e) {
            e.printStackTrace();
            }
    }

    @Override
    public void run() {
        esperarMensajes();
    }

    public ServerSocket getSocketServer() {
        return socketServer;
    }

    public Socket getSocket() {
        return socket;
    }


}
