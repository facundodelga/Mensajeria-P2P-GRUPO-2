package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que representa un servidor de directorio para gestionar el registro de usuarios.
 * Escucha conexiones entrantes y delega el manejo de cada conexión a un hilo separado.
 */

public class Servidor {
    private ServerSocket serverSocket;
    private ArrayList<Mensaje> mensajesRecibidos; // Almacena los mensajes recibidos
    private Map<String, Contacto> usuarios; // Almacena los usuarios registrados
    private Map<Contacto, Socket> sockets; // Almacena los sockets de los usuarios registrados
    private Map<Contacto, ManejadorRegistro> manejadores;

    /**
     * Constructor para ServidorDirectorio.
     * Inicializa el ServerSocket en el puerto especificado y crea un mapa para almacenar usuarios.
     * @throws IOException Si hay un error al abrir el puerto.
     */
    public Servidor() throws IOException {
        int puerto;
        try (BufferedReader reader = new BufferedReader(new FileReader("serverConfig.txt"))) {
            puerto = Integer.parseInt(reader.readLine().trim());
            this.serverSocket = new ServerSocket(puerto);
            serverSocket.setReuseAddress(true);
            //serverSocket.bind(new InetSocketAddress(puerto));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error al leer el puerto desde el archivo de configuración");
        } catch (IOException e) {
            throw new RuntimeException("Error al abrir el archivo de configuración");
        }

        this.usuarios = new HashMap<>();
        this.mensajesRecibidos = new ArrayList<>();
        this.sockets = new HashMap<>();
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
                new Thread(new ManejadorRegistro(socket, this)).start();
                
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    /**
     * Cierra el ServerSocket y libera los recursos asociados.
     *
     * @throws IOException Si hay un error al cerrar el socket del servidor.
     */
    public void cerrar() throws IOException {
        serverSocket.close();
    }

    public ArrayList<Mensaje> getMensajesRecibidos() {
        return mensajesRecibidos;
    }

    public Map<String, Contacto> getUsuarios() {
        return usuarios;
    }

    public Map<Contacto, Socket> getSockets() {
        return sockets;
    }

    public Map<Contacto, ManejadorRegistro> getManejadores() {
        return manejadores;
    }

    public void addUsuario(String nombre, Contacto usuario) {
        usuarios.put(nombre, usuario);
    }

    public void addSocket(Contacto usuario, Socket socket) {
        sockets.put(usuario, socket);
    }

    public void addManejador(Contacto usuario, ManejadorRegistro manejador) {
        manejadores.put(usuario, manejador);
        System.out.println(manejadores);
    }
}