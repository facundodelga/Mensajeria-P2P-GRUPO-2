package org.example.servidor;

import org.example.modelo.usuario.Contacto;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Clase que maneja el registro de usuarios en el servidor.
 * Implementa la interfaz Runnable para permitir la ejecución en un hilo separado.
 */

public class ManejadorRegistro implements Runnable {
    private Socket socket;
    private Map<String, Contacto> usuarios;

    /**
     * Constructor para ManejadorRegistro.
     * Inicializa el socket y el mapa de usuarios para manejar el registro de usuarios.
     *
     * @param socket   El socket para la comunicación con el cliente.
     * @param usuarios Un mapa que contiene los usuarios registrados, indexados por su nickname.
     */
    public ManejadorRegistro(Socket socket, Map<String, Contacto> usuarios) {
        this.socket = socket;
        this.usuarios = usuarios;
    }


    /**
     * Ejecuta el proceso de registro en un hilo separado.
     * Lee un objeto UsuarioDTO del flujo de entrada, verifica si el nickname ya está en uso,
     * y envía una respuesta al cliente indicando el éxito o fracaso del registro.
     */
    @Override
    public void run() {
        try (ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream())) {

            Contacto usuarioDTO = (Contacto) entrada.readObject();

            // Verificar si el nickname ya está en uso
            if (usuarios.containsKey(usuarioDTO.getNombre())) {
                salida.writeObject("El nickname ya está en uso.");
            } else {
                // Registrar el nuevo usuario en el mapa
                usuarios.put(usuarioDTO.getNombre(), usuarioDTO);
                salida.writeObject("Registro exitoso.");
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}