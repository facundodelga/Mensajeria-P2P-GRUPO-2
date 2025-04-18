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
    private ServidorDirectorio servidorDirectorio;

    /**
     * Constructor para ManejadorRegistro.
     * Inicializa el socket y el mapa de usuarios para manejar el registro de usuarios.
     *
     * @param socket   El socket para la comunicación con el cliente.
     * @param servidorDirectorio El servidor de directorio que gestiona los usuarios registrados.
     */
    public ManejadorRegistro(Socket socket, ServidorDirectorio servidorDirectorio) {
        this.socket = socket;
        this.servidorDirectorio = servidorDirectorio;
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
            if (this.servidorDirectorio.getUsuarios().containsKey(usuarioDTO.getNombre())) {
                salida.writeObject("El nickname ya está en uso.");
            } else {
                // Registrar el nuevo usuario en el mapa
                this.servidorDirectorio.getUsuarios().put(usuarioDTO.getNombre(), usuarioDTO);

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