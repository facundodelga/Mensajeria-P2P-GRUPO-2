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
    private Map<Contacto, ManejadorRegistro> manejadores;
    private IDirectorio directorio;
    private IColaMensajes colaMensajes;
    private boolean cambios;

    public ServidorPrincipal(Servidor servidor) throws IOException {
        this.servidor = servidor;
        int puerto = servidor.getPuerto();
        this.serverSocket = new ServerSocket(puerto);
        serverSocket.setReuseAddress(true);
        this.directorio = new Directorio();
        this.colaMensajes = new ColaMensajes();
        this.manejadores = new HashMap<>();
        this.cambios = false;
    }

    public ServidorPrincipal(Servidor servidor, IDirectorio directorio, IColaMensajes colaMensajes, boolean cambios) throws IOException {
        this(servidor);
        this.manejadores = new HashMap<>();
        this.directorio = directorio;
        this.colaMensajes = colaMensajes;
        this.cambios = cambios;
    }

    public void setCambios(boolean cambios) {
        // Implementación del método para manejar cambios
        this.cambios = cambios;
    }
    public boolean hayCambios () {
        return this.cambios;
    }

    @Override
    public void esperarConexiones() {
        try {
            Socket socket = this.serverSocket.accept();
            new Thread(() -> { // En otro thread para no interferir con la conexión de nuevas terminales
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String msg = in.readLine(); // Recibe identificación
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

    public IColaMensajes getColaMensajes() {
        return colaMensajes;
    }

    public Map<Contacto, ManejadorRegistro> getManejadores() {
        return manejadores;
    }


    public void addManejador(Contacto usuario, ManejadorRegistro manejador) {
        manejadores.put(usuario, manejador);
        System.out.println(manejadores);
    }
}
