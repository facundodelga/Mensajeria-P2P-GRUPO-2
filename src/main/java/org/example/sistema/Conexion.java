package org.example.sistema;

import org.example.mensaje.Mensaje;
import org.example.usuario.Usuario;
import org.example.usuario.UsuarioDTO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Conexion implements Runnable{
    private static Conexion instancia;
    private ServerSocket socketServer;
    private Socket socket;
    private ConcurrentHashMap<UsuarioDTO, ObjectOutputStream> conexionesDeSalida = new ConcurrentHashMap<>();

    private Conexion() {
    }

    public static Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    public void configurarServidor(int puerto) {
        try {
            this.socketServer = new ServerSocket(puerto);
            System.out.println("Servidor configurado en el puerto: " + puerto);
            System.out.println("Servidor iniciado...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void iniciarServidor() {
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

    public void enviarMensaje(UsuarioDTO usuarioDTO, Mensaje mensaje) {
        System.out.println("Intentando enviar mensaje a " + usuarioDTO);
        ObjectOutputStream salida = conexionesDeSalida.get(usuarioDTO);
        if (salida != null) {
            try {
                System.out.println("Enviando mensaje a " + usuarioDTO + ": " + mensaje.getContenido());
                salida.writeObject(mensaje);
                salida.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("El usuario " + usuarioDTO.getPuerto() + " no está conectado.");
            System.out.println("Conexiones actuales: " + conexionesDeSalida.keySet());
        }
    }


    public void agregarConexionDeSalida(String nombre, Socket socket) {
        try {
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Conexión de salida agregada para " + nombre);
            UsuarioDTO usuarioDTO = new UsuarioDTO(nombre, socket.getInetAddress().getHostAddress(), socket.getPort());
            conexionesDeSalida.put(usuarioDTO, salida);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        iniciarServidor();
    }

    public ServerSocket getSocketServer() {
        return socketServer;
    }

    public Socket getSocket() {
        return socket;
    }

    public ConcurrentHashMap<UsuarioDTO, ObjectOutputStream> getConexionesDeSalida() {
        return conexionesDeSalida;
    }

}
