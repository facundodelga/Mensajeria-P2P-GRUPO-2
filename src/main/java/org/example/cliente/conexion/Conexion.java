package org.example.cliente.conexion;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Clase que representa una conexión de servidor.
 * Implementa la interfaz IConexion.
 */
public class Conexion implements IConexion {

    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    /**
     * Constructor de la clase Conexion.
     */
    public Conexion() {
    }

    /**
     * Verifica si un puerto está en uso.
     * @param puerto El puerto a verificar.
     * @return true si el puerto está en uso, false en caso contrario.
     */
    private boolean elPuertoEstaEnUso(int puerto) {
        try (ServerSocket ignored = new ServerSocket(puerto)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * Inicia el servidor en el puerto especificado.
     * @param puerto El puerto en el que se configurará el servidor.
     */
    @Override
    public void conectarServidor(Contacto usuario, int puerto) throws PuertoEnUsoException {
//        if (elPuertoEstaEnUso(puerto)) {
//            throw new PuertoEnUsoException("El puerto " + puerto + " ya está en uso.");
//        }
        try {
            this.socket = new Socket();

            //this.socket.bind(new InetSocketAddress(usuario.getPuerto())); // El puerto que eligió el usuario
            this.socket.connect(new InetSocketAddress("127.0.0.1", 8080));

            this.socket.setReuseAddress(true);

            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());

            Thread.sleep(50);
            System.out.println(usuario.toString());
            // Enviar el objeto UsuarioDTO al servidor
            this.salida.writeObject(usuario);
            this.salida.flush();

            String estaOcupado = (String) this.entrada.readObject();

            if ("El nickname ya está en uso.".equals(estaOcupado)) {
                throw new PuertoEnUsoException("El nickname ya está en uso.");
            } else {
                System.out.println("Conexión establecida con el servidor en el puerto: " + puerto);
            }
        } catch (ConnectException e) {
            System.err.println("Error: No se pudo conectar al servidor en el puerto " + puerto + ". Asegúrese de que el servidor esté en ejecución.");
        } catch (UnknownHostException e) {
            System.err.println("Error: Host desconocido.");
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
        }
        catch (InterruptedException e) {
            System.err.println("Error: Hilo interrumpido.");
        }
        catch (ClassNotFoundException e) {
            System.err.println("Error: Clase no encontrada.");
        }
    }

    public void obtenerMensajesPendientes(){
        try {
            this.salida.writeObject("MensajesPendientes");
            this.salida.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Contacto> obtenerContactos(){
        try{

            Contacto c = new Contacto("Contactos","111", 0);
            this.salida.writeObject(c);
            this.salida.flush();

            return null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Espera conexiones entrantes y maneja los mensajes recibidos.
     */
    @Override
    public void esperarMensajes() {

        new Thread(new ManejadorEntradas(socket, entrada)).start();

    }

    /**
     * Envía un mensaje a un usuario específico.
     * @param usuarioDTO El usuario al que se enviará el mensaje.
     * @param mensaje El mensaje que se enviará.
     * @throws EnviarMensajeException Si ocurre un error al enviar el mensaje.
     */
    @Override
    public void enviarMensaje(Contacto usuarioDTO, Mensaje mensaje) throws EnviarMensajeException, IOException {
        if (salida == null) {
            throw new IOException("El canal de salida no está inicializado.");
        } else {
            System.out.println("Intentando enviar mensaje a " + usuarioDTO);
            try {
                System.out.println("Enviando mensaje a " + usuarioDTO + ": " + mensaje.getContenido());
                salida.writeObject(mensaje);
                salida.flush();

            } catch (IOException e) {
                throw new EnviarMensajeException("Error al enviar el mensaje a " + usuarioDTO, e);
            }
        }
    }

    /**
     * Cierra las conexiones del servidor y del socket.
     */
    @Override
    public void cerrarConexiones() {
        try {

            if (entrada != null) {
                entrada.close();
                entrada = null;
            }
            if (salida != null) {
                salida.flush(); // Asegurar que todos los datos sean enviados
                salida.close();
                salida = null;
            }
            if (socket != null) {

                socket.close();
                socket = null;
            }
            System.gc(); // Sugerencia al recolector de basura (no garantiza nada)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método que ejecuta el hilo del servidor para esperar mensajes.
     */
    @Override
    public void run() {
        esperarMensajes();
    }

    /**
     * Obtiene el Socket de la conexión actual.
     * @return El Socket de la conexión actual.
     */
    public Socket getSocket() {
        return socket;
    }
}