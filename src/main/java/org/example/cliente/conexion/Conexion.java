package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Observable;

/**
 * Clase encargada de gestionar la conexión de red del cliente con el servidor.
 * Extiende Observable para notificar al Controlador sobre eventos (ej. mensajes recibidos, DirectorioDTO).
 */
public class Conexion extends Observable implements Runnable {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ManejadorEntradas manejadorEntradas; // Ahora es una clase externa
    private Controlador controlador; // Referencia al controlador para notificarle eventos

    private String serverIp = "127.0.0.1";
    private int serverPort = 8080;
    private Contacto usuarioDTOOriginal;

    private volatile boolean conectado = false; // Estado de la conexión

    /**
     * Constructor de la clase Conexion.
     */
    public Conexion() {
        // La conexión se realiza en conectarServidor()
    }

    /**
     * Establece la referencia al controlador. Es crucial para que Conexion pueda notificarle.
     * @param controlador La instancia del Controlador.
     */
    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
        this.addObserver(controlador); // Registrar el controlador como observador
    }

    /**
     * Intenta establecer la conexión con el servidor.
     * @param usuarioDTO El objeto Contacto del usuario que intenta conectarse.
     * @throws IOException Si ocurre un error de entrada/salida durante la conexión.
     * @throws PuertoEnUsoException Si el puerto del cliente ya está en uso.
     */
    public void conectarServidor(Contacto usuarioDTO) throws IOException, PuertoEnUsoException {
        this.usuarioDTOOriginal = usuarioDTO;
        try {
            socket = new Socket(serverIp, serverPort);
            System.out.println("DEBUG: Socket conectado a " + serverIp + ":" + serverPort);
            conectado = true;

            // 1. Crear el ObjectOutputStream PRIMERO en el cliente
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush(); // IMPORTANTE: Envía la cabecera de serialización
            System.out.println("DEBUG: ObjectOutputStream inicializado y flusheado.");

            // 2. Crear el ObjectInputStream DESPUÉS en el cliente
            inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("DEBUG: ObjectInputStream inicializado.");

            // 3. Enviar el objeto de usuario inicial
            outputStream.writeObject(usuarioDTO);
            outputStream.flush();
            System.out.println("DEBUG: Usuario " + usuarioDTO.getNombre() + " enviado al servidor.");

            // 4. Inicializar el ManejadorEntradas (la clase externa)
            this.manejadorEntradas = new ManejadorEntradas(socket, inputStream, this); // Pasa 'this' como conexionPadre
            System.out.println("DEBUG: ManejadorEntradas inicializado en Conexion.");

        } catch (ConnectException e) {
            conectado = false;
            throw new IOException("No se pudo conectar al servidor en " + serverIp + ":" + serverPort + ". " + e.getMessage(), e);
        } catch (IOException e) {
            conectado = false;
            if (e.getMessage() != null && e.getMessage().contains("Address already in use")) {
                throw new PuertoEnUsoException("El puerto del cliente ya está en uso o no se pudo asignar. Intente de nuevo.", e);
            }
            throw new IOException("Error de E/S al conectar: " + e.getMessage(), e);
        }
    }

    /**
     * Intenta reconectar al servidor con el usuario original.
     * @throws IOException Si ocurre un error de entrada/salida durante la reconexión.
     */
    public void reconectar() throws IOException {
        System.out.println("DEBUG: Intentando reconectar...");
        cerrarConexiones(); // Asegurarse de cerrar todo antes de intentar reconectar

        try {
            conectarServidor(usuarioDTOOriginal);
            System.out.println("DEBUG: Reconexión exitosa.");
        } catch (PuertoEnUsoException e) {
            System.err.println("Error al reconectar (puerto en uso): " + e.getMessage());
            throw new IOException("Fallo al reconectar: " + e.getMessage(), e);
        } catch (IOException e) {
            System.err.println("Fallo grave al reconectar: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Método run del hilo de Conexion. Es el punto de inicio para el ManejadorEntradas.
     * Se ejecuta en un hilo separado por el Controlador.
     */
    @Override
    public void run() {
        if (manejadorEntradas != null) {
            new Thread(manejadorEntradas, "ManejadorEntradas-Cliente").start(); // Asignar un nombre al hilo
            System.out.println("DEBUG: Hilo de ManejadorEntradas iniciado.");
        } else {
            System.err.println("ERROR: ManejadorEntradas es null. No se puede iniciar el hilo de escucha.");
            setChanged();
            notifyObservers(new IOException("Error interno: Fallo al inicializar el manejador de entradas."));
        }
    }

    /**
     * Envía un mensaje al servidor.
     * @param receptor El contacto receptor del mensaje.
     * @param mensaje El objeto Mensaje a enviar.
     * @throws IOException Si ocurre un error de E/S.
     * @throws PerdioConexionException Si la conexión no está activa.
     * @throws EnviarMensajeException Si hay un problema al escribir el objeto.
     */
    public void enviarMensaje(Contacto receptor, Mensaje mensaje) throws IOException, PerdioConexionException, EnviarMensajeException {
        if (!conectado || socket == null || socket.isClosed() || outputStream == null) {
            throw new PerdioConexionException("Conexión no activa. No se puede enviar mensaje.");
        }
        try {
            outputStream.writeObject(mensaje);
            outputStream.flush();
            // ¡¡¡CORRECCIÓN AQUÍ!!! Usamos getEmisor() y getReceptor() directamente (son Strings)
            System.out.println("DEBUG: Mensaje enviado: " + mensaje.getEmisor() + " -> " + mensaje.getReceptor());
        } catch (IOException e) {
            conectado = false;
            // Notificar al controlador que la conexión se perdió
            notificarConexionPerdida("Error de I/O al enviar mensaje: " + e.getMessage(), e);
            throw new EnviarMensajeException("Error al escribir mensaje en el stream: " + e.getMessage(), e);
        }
    }

    /**
     * Solicita la lista de contactos actualizada al servidor.
     * @throws IOException Si ocurre un error de E/S.
     * @throws PerdioConexionException Si la conexión no está activa.
     */
    public void obtenerContactos() throws IOException, PerdioConexionException {
        if (!conectado || socket == null || socket.isClosed() || outputStream == null) {
            throw new PerdioConexionException("Conexión no activa. No se puede obtener el directorio.");
        }
        try {
            outputStream.writeObject("SOLICITAR_DIRECTORIO"); // Puedes usar un enum o una clase específica para esto
            outputStream.flush();
            System.out.println("DEBUG: Solicitud de directorio enviada.");
        } catch (IOException e) {
            conectado = false;
            notificarConexionPerdida("Error de I/O al solicitar directorio: " + e.getMessage(), e);
            throw new IOException("Error al enviar solicitud de directorio: " + e.getMessage(), e);
        }
    }

    /**
     * Método llamado por ManejadorEntradas para procesar objetos recibidos.
     * Esta clase (Conexion) luego notifica a sus observadores (el Controlador).
     * @param objetoRecibido El objeto recibido del servidor (Mensaje, DirectorioDTO, etc.).
     */
    public void procesarObjetoRecibido(Object objetoRecibido) {
        setChanged(); // Marca que el estado de este Observable ha cambiado
        notifyObservers(objetoRecibido); // Notifica a todos los Observadores (el Controlador)
    }

    /**
     * Notifica al observador que la conexión se perdió.
     * @param mensaje El mensaje de la excepción.
     */
    public void notificarConexionPerdida(String mensaje) {
        conectado = false;
        setChanged();
        notifyObservers(new PerdioConexionException(mensaje));
    }

    /**
     * Notifica al observador que la conexión se perdió.
     * @param mensaje El mensaje de la excepción.
     * @param causa La causa raíz de la excepción.
     */
    public void notificarConexionPerdida(String mensaje, Throwable causa) {
        conectado = false;
        setChanged();
        notifyObservers(new PerdioConexionException(mensaje, causa));
    }

    /**
     * Notifica al observador de un error general de lectura.
     * @param mensaje El mensaje de la excepción.
     * @param causa La causa raíz de la excepción.
     */
    public void notificarErrorLectura(String mensaje, Throwable causa) {
        conectado = false;
        setChanged();
        notifyObservers(new IOException(mensaje, causa));
    }

    /**
     * Cierra todos los streams y el socket.
     */
    public void cerrarConexiones() {
        conectado = false;
        if (manejadorEntradas != null) {
            manejadorEntradas.detener(); // Detener el hilo de escucha del manejador
        }
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("DEBUG: Conexiones cerradas.");
        } catch (IOException e) {
            System.err.println("Error al cerrar conexiones: " + e.getMessage());
            e.printStackTrace();
        } finally {
            outputStream = null;
            inputStream = null;
            socket = null;
            manejadorEntradas = null;
        }
    }
}