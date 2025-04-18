package org.example.servidor;

import org.example.modelo.usuario.Contacto;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que representa un servidor de directorio para gestionar el registro de usuarios.
 * Escucha conexiones entrantes y delega el manejo de cada conexión a un hilo separado.
 */

public class ServidorDirectorio {
    private ServerSocket serverSocket;
    private Map<String, Contacto> usuarios; // Almacena los usuarios registrados

    /**
     * Constructor para ServidorDirectorio.
     * Inicializa el ServerSocket en el puerto especificado y crea un mapa para almacenar usuarios.
     *
     * @param puerto El puerto en el que el servidor escuchará las conexiones entrantes.
     * @throws IOException Si hay un error al abrir el puerto.
     */
    public ServidorDirectorio(int puerto) throws IOException {
        this.serverSocket = new ServerSocket(puerto);
        this.usuarios = new HashMap<>();
        System.out.println("Servidor de directorio iniciado en el puerto: " + puerto);
    }

    /**
     * Inicia el servidor y comienza a aceptar conexiones de clientes.
     * Por cada conexión entrante, se crea un nuevo hilo para manejar el registro del usuario.
     */
    public void iniciar() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                // Iniciar un nuevo hilo para manejar el registro del usuario
                new Thread(new ManejadorRegistro(socket, usuarios)).start();
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
}