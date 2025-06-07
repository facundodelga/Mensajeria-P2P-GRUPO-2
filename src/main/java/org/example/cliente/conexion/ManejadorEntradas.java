package org.example.cliente.conexion;

import org.example.cliente.modelo.mensaje.Mensaje; // Asegúrate de importar las clases necesarias
import org.example.servidor.DirectorioDTO; // Asegúrate de importar las clases necesarias

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Clase responsable de escuchar y leer objetos entrantes del servidor
 * a través de un ObjectInputStream en un hilo separado.
 * Notifica a la instancia de Conexion padre sobre los objetos recibidos.
 */
public class ManejadorEntradas implements Runnable {
    private final Socket socket;
    private final ObjectInputStream entrada;
    private final Conexion conexionPadre; // Referencia a la instancia de Conexion
    private volatile boolean ejecutando = true; // Flag para controlar el bucle de escucha

    /**
     * Constructor de ManejadorEntradas.
     * @param socket El socket de la conexión.
     * @param entrada El ObjectInputStream para leer objetos.
     * @param conexionPadre La instancia de Conexion que creó este manejador.
     */
    public ManejadorEntradas(Socket socket, ObjectInputStream entrada, Conexion conexionPadre) {
        this.socket = socket;
        this.entrada = entrada;
        this.conexionPadre = conexionPadre;
    }

    /**
     * Método principal del hilo. Contiene el bucle de escucha de objetos.
     */
    @Override
    public void run() {
        System.out.println("DEBUG: ManejadorEntradas - Hilo de escucha iniciado.");
        while (ejecutando) {
            try {
                // Bloquea aquí hasta que un objeto es recibido o el stream se cierra
                Object objetoRecibido = entrada.readObject();
                if (objetoRecibido != null) {
                    System.out.println("DEBUG: ManejadorEntradas - Objeto recibido: " + objetoRecibido.getClass().getSimpleName());
                    // Notifica a la instancia de Conexion padre
                    conexionPadre.notificarControlador(objetoRecibido);
                }
            } catch (EOFException e) {
                // Fin del stream, el servidor cerró la conexión limpiamente
                System.out.println("DEBUG: ManejadorEntradas - Fin de stream (EOFException). Servidor desconectado.");
                ejecutando = false;
                // Notifica al controlador a través de la conexión padre que la conexión se perdió
                conexionPadre.notificarConexionPerdida("El servidor cerró la conexión.");
            } catch (SocketException e) {
                // Error de socket (ej. conexión reseteada, socket cerrado inesperadamente)
                System.err.println("ERROR: ManejadorEntradas - SocketException: " + e.getMessage());
                ejecutando = false;
                conexionPadre.notificarConexionPerdida("Conexión perdida inesperadamente: " + e.getMessage(), e);
            } catch (IOException e) {
                // Otro error de E/S al leer
                System.err.println("ERROR: ManejadorEntradas - IOException al leer objeto: " + e.getMessage());
                e.printStackTrace();
                ejecutando = false;
                conexionPadre.notificarErrorLectura("Error de lectura en la conexión: " + e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                System.err.println("ERROR: ManejadorEntradas - Clase no encontrada al leer objeto: " + e.getMessage());
                e.printStackTrace();
                // No detiene la ejecución, solo notifica el error
            } catch (Exception e) { // Captura cualquier otra excepción no esperada
                System.err.println("ERROR: ManejadorEntradas - Excepción inesperada: " + e.getMessage());
                e.printStackTrace();
                // Puedes decidir si detener la ejecución o solo notificar
            }
        }
        System.out.println("DEBUG: ManejadorEntradas - Hilo de escucha detenido.");
        // Al finalizar el bucle, asegurar que las conexiones de la Conexion padre se cierren.
        // Esto es importante si el ManejadorEntradas es el que detecta la pérdida de conexión.
        conexionPadre.cerrarConexiones();
    }

    /**
     * Detiene el bucle de escucha, cerrando los streams para forzar la salida del readObject().
     */
    public void detener() {
        ejecutando = false;
        try {
            if (entrada != null) {
                entrada.close(); // Cierra el stream para desbloquear readObject
            }
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Cierra el socket para forzar la salida
            }
        } catch (IOException e) {
            System.err.println("Error al detener ManejadorEntradas: " + e.getMessage());
        }
    }
}