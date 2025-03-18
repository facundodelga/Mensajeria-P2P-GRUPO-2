package org.example.sistema;

import org.example.mensaje.Mensaje;
import org.example.usuario.Usuario;
import org.example.usuario.UsuarioDTO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Sistema implements Runnable{
    private static Sistema instancia;
    private Usuario usuario;
    private ServerSocket socketServer;
    private Socket socket;

    private ConcurrentHashMap<UsuarioDTO, ObjectOutputStream> conexionesDeSalida = new ConcurrentHashMap<>();

    private Sistema() {
    }

    public static Sistema getInstancia() {
        if (instancia == null) {
            instancia = new Sistema();
        }
        return instancia;
    }

    public void configurarServidor(Usuario usuario) {
        try {
            this.socketServer = new ServerSocket(usuario.getPuerto());
            System.out.println("Servidor configurado en el puerto: " + usuario.getPuerto());
            this.usuario = usuario;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void iniciarServidor() {
        try {
            while(true){
                this.socket = this.socketServer.accept();
                new Thread(new ManejadorCliente(this.socket)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        cerrarConexiones();
    }

    public void enviarMensaje(Mensaje mensaje) {
        ObjectOutputStream salida = conexionesDeSalida.get(mensaje.getUsuario());
        if (salida != null) {
            try {
                System.out.println("Enviando mensaje a " + mensaje.getUsuario().getNombre() + ": " + mensaje.getContenido());
                salida.writeObject(mensaje);
                salida.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("El usuario " + mensaje.getUsuario().getNombre() + " no está conectado.");
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

    public Usuario getUsuario() {
        return usuario;
    }
}
