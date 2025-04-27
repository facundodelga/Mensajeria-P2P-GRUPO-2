package org.example.servidor;

import org.example.cliente.modelo.usuario.Contacto;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que representa un servidor de directorio para gestionar el registro de usuarios.
 * Escucha conexiones entrantes y delega el manejo de cada conexión a un hilo separado.
 */
public class Servidor {
    private ServerSocket serverSocket;
    private IDirectorio directorio;
    private IColaMensajes colaMensajes;
    private Map<Contacto, ManejadorRegistro> manejadores;

    /**
     * Constructor para ServidorDirectorio.
     * Inicializa el ServerSocket en el puerto especificado y crea instancias de Directorio y ColaMensajes.
     * @throws IOException Si hay un error al abrir el puerto.
     */
    public Servidor() throws IOException {
        int puerto;
        try (BufferedReader reader = new BufferedReader(new FileReader("serverConfig.txt"))) {
            puerto = Integer.parseInt(reader.readLine().trim());
            this.serverSocket = new ServerSocket(puerto);
            serverSocket.setReuseAddress(true);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error al leer el puerto desde el archivo de configuración");
        } catch (IOException e) {
            throw new RuntimeException("Error al abrir el archivo de configuración");
        }

        this.directorio = new Directorio();
        this.colaMensajes = new ColaMensajes();
        this.manejadores = new HashMap<>();
        System.out.println("Servidor iniciado en el puerto: " + puerto);
    }

    /**
     * Inicia el servidor y comienza a aceptar conexiones de clientes.
     * Por cada conexión entrante, se crea un nuevo hilo para manejar el registro del usuario.
     */
    public void iniciar() {
        while (true) {
            try {
                System.out.println("SERVIDOR: Esperando conexiones...");
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + socket.getInetAddress() + ":" + socket.getPort());
                ManejadorRegistro manejador = new ManejadorRegistro(socket, this.directorio, this.colaMensajes, this.manejadores);
                new Thread(manejador).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cierra el ServerSocket y libera los recursos asociados.
     * @throws IOException Si hay un error al cerrar el socket del servidor.
     */
    public void cerrar() throws IOException {
        serverSocket.close();
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
        System.out.println("Manejadores: " + manejadores);
    }
}