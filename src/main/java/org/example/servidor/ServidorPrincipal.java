package org.example.servidor;

import org.example.cliente.modelo.usuario.Contacto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServidorPrincipal implements ServidorState{
    private ServerSocket serverSocket;
    private Servidor servidor;
    private Map<String, ManejadorRegistro> manejadores; // Changed from Contacto to String
    private IDirectorio directorio;
    private IColaMensajes colaMensajes;
    private IDirectorio conectados;
    private boolean cambios;

    public ServidorPrincipal(Servidor servidor) throws IOException {
        this.servidor = servidor;
        int puerto = servidor.getPuerto();
        this.serverSocket = new ServerSocket(puerto);
        serverSocket.setReuseAddress(true);
        this.directorio = new Directorio();
        this.conectados = new Directorio();
        this.colaMensajes = new ColaMensajes();
        this.manejadores = new HashMap<>(); // Now HashMap<String, ManejadorRegistro>
        this.cambios = false;
    }

    public ServidorPrincipal(Servidor servidor, IDirectorio directorio, IColaMensajes colaMensajes, boolean cambios) throws IOException {
        this(servidor);
        this.manejadores = new HashMap<>(); // Now HashMap<String, ManejadorRegistro>
        this.directorio = directorio;
        this.conectados = new Directorio();
        this.colaMensajes = colaMensajes;
        this.cambios = cambios;
    }

    public void setCambios(boolean cambios) {
        this.cambios = cambios;
    }
    public boolean hayCambios () {
        return this.cambios;
    }

    @Override
    public void esperarConexiones() {
        try {
            Socket socket = this.serverSocket.accept();
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String msg = in.readLine();
                    System.out.println("INTENTO DE CONEXION DE: " + msg);
                    if ("CLIENTE".equals(msg)) {
                        new Thread(new ManejadorRegistro(socket, this)).start();
                    } else if("SERVIDOR".equals(msg)) {
                        new ManejadorRedundancia(socket,this).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void cambiarEstado() {

    }

    public IDirectorio getDirectorio() {
        return directorio;
    }
    public IDirectorio getConectados() {
        return conectados;
    }

    public IColaMensajes getColaMensajes() {
        return colaMensajes;
    }

    public Map<String, ManejadorRegistro> getManejadores() { // Changed return type key to String
        return manejadores;
    }

    public void addManejador(String nombreUsuario, ManejadorRegistro manejador) { // Changed parameter type to String
        manejadores.put(nombreUsuario, manejador);
        System.out.println(manejadores);
    }

    public void removeManejador(String nombreUsuario) { // Changed parameter type to String
        manejadores.remove(nombreUsuario);
        System.out.println("Manejador para " + nombreUsuario + " eliminado.");
    }
}