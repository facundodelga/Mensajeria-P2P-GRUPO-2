// src/main/java/org/example/cliente/conexion/Conexion.java
package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.util.Cifrador; // Importar Cifrador
import org.example.util.cifrado.ClaveUtil; // Importar ClaveUtil

import javax.crypto.SecretKey;
import java.io.*;
import java.net.*;
import java.security.KeyPair; // Importar KeyPair
import java.security.PublicKey;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * Clase que representa una conexión de servidor.
 * Implementa la interfaz IConexion.
 */
public class Conexion extends Observable implements IConexion, Observer, Runnable  {

    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private PrintWriter registroOut; // Para enviar comandos de texto como "CLIENTE"
    private String ip;
    private Contacto usuario; // El usuario local que usa esta conexión
    private ArrayList<Map.Entry<String, Integer>> servers;
    private int serverActivo;
    private int puertoRespaldo;
    private int puerto;

    // Referencia al controlador para pedir la conversación y almacenar la clave AES
    private Controlador controlador;

    // Campo para almacenar el par de claves DH del cliente
    private KeyPair miParClavesDH;

    /**
     * Constructor de la clase Conexion.
     */
    public Conexion() {
        this.controlador = Controlador.getInstancia(); // Asumiendo que Controlador es un Singleton
    }

    /**
     * Establece el par de claves Diffie-Hellman para esta conexión.
     * @param miParClavesDH El par de claves DH generado por el controlador.
     */
    public void setMiParClavesDH(KeyPair miParClavesDH) {
        this.miParClavesDH = miParClavesDH;
    }

    /**
     * Verifica si un puerto está en uso. (Este método no es usado en el flujo actual de conexión cliente)
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
     * Conecta al servidor con el usuario especificado.
     */
    @Override
    public void conectarServidor(Contacto usuario) throws PuertoEnUsoException, IOException, PerdioConexionException {
        this.usuario = usuario;
        try {
            // Leer configuración de IP y puertos desde archivo
            try (BufferedReader reader = new BufferedReader(new FileReader("clienteConfig.txt"))) {
                ip = reader.readLine().trim();
                puerto = Integer.parseInt(reader.readLine().trim());
                puertoRespaldo = Integer.parseInt(reader.readLine().trim());

            } catch (NumberFormatException e) {
                throw new RuntimeException("Error al leer el puerto desde el archivo de configuracion: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new RuntimeException("Error al abrir el archivo de configuracion: " + e.getMessage(), e);
            }

            this.servers = new ArrayList<>();
            this.servers.add(new AbstractMap.SimpleEntry<>(this.ip, this.puerto)); // Servidor principal
            this.servers.add(new AbstractMap.SimpleEntry<>(this.ip, this.puertoRespaldo)); // Servidor de respaldo

            this.serverActivo = 0; // Intentar conectar al servidor principal primero

            conectar(this.servers.get(this.serverActivo));

        } catch (UnknownHostException e) {
            System.err.println("Error: Host desconocido: " + e.getMessage());
            throw e; // Relanzar la excepción para que el controlador la maneje
        }
    }

    public void obtenerMensajesPendientes(){
        try {
            if (salida != null) {
                this.salida.writeObject("MensajesPendientes");
                this.salida.flush();
            } else {
                System.err.println("Conexion: Salida no inicializada al intentar obtener mensajes pendientes.");
            }
        } catch (IOException e) {
            System.err.println("Conexion: Error al enviar solicitud de mensajes pendientes: " + e.getMessage());
            // No lanzar PerdioConexionException aquí; el ManejadorEntradas o el propio bucle de envío lo detectará
        }
    }

    @Override
    public ArrayList<Contacto> obtenerContactos() throws PerdioConexionException {
        // Este método sólo envía la solicitud.
        // La respuesta (ArrayList<Contacto> encapsulada en DirectorioDTO)
        // será recibida por el ManejadorEntradas y notificada al Controlador.
        try {
            if (salida != null) {
                // Envía una solicitud para el directorio de contactos.
                // Podría ser un String "ObtenerDirectorio" o un objeto de control específico.
                // Usaremos un Contacto ficticio como marcador para la solicitud,
                // pero un String o un enum sería más claro en un sistema real.
                Contacto solicitudDirectorio = new Contacto("Contactos", "DIRECTORIO", -1);
                this.salida.writeObject(solicitudDirectorio);
                this.salida.flush();
                System.out.println("Conexion: Solicitud de directorio de contactos enviada.");
            } else {
                throw new IOException("El canal de salida no está inicializado.");
            }
        } catch(SocketException e){
            System.err.println("Conexion: SocketException al solicitar contactos: " + e.getMessage());
            throw new PerdioConexionException("Error de conexión al intentar obtener contactos. Intente reconectar.", e);
        } catch(IOException e){
            System.err.println("Conexion: IOException al solicitar contactos: " + e.getMessage());
        }
        return null; // La lista real se recibe y procesa de forma asíncrona en el Controlador.update()
    }

    /**
     * Espera conexiones entrantes y maneja los mensajes recibidos.
     * Pasa la referencia de 'this' para que ManejadorEntradas pueda notificar al Controlador.
     */
    @Override
    public void esperarMensajes() {
        // ManejadorEntradas necesita una referencia para notificar al Controlador
        // o directamente observar a Conexion para que esta última notifique al Controlador.
        // En tu diseño actual, ManejadorEntradas ya tiene un observador (Controlador, al que le pasas 'this').
        // Asegúrate de que ManejadorEntradas notifica a 'this' (Conexion), y que Conexion luego notifica a sus propios observers (Controlador).
        ManejadorEntradas manejador = new ManejadorEntradas(socket, entrada);
        manejador.addObserver(this.controlador); // ManejadorEntradas notifica directamente al Controlador
        new Thread(manejador).start(); // Inicia el hilo de ManejadorEntradas
    }

    /**
     * Envía un mensaje a un usuario específico.
     * Antes de enviar, el contenido del mensaje se cifrará con la clave AES de la conversación.
     * @param contactoRemoto El contacto al que se enviará el mensaje.
     * @param mensaje El mensaje que se enviará (contenido en texto plano aquí).
     * @throws EnviarMensajeException Si ocurre un error al enviar el mensaje.
     * @throws PerdioConexionException Si se pierde la conexión.
     * @throws IOException Si ocurre un error de I/O.
     */
    @Override
    public void enviarMensaje(Contacto contactoRemoto, Mensaje mensaje) throws EnviarMensajeException, IOException, PerdioConexionException {
        if (salida == null) {
            throw new IOException("El canal de salida no está inicializado.");
        } else {
            System.out.println("Conexion: Intentando enviar mensaje a " + contactoRemoto.getNombre());
            try {
                // 1. Obtener la conversación para este contacto desde el Controlador
                Conversacion conversacion = controlador.getConversacion(contactoRemoto);
                if (conversacion == null || conversacion.getClaveSecretaAes() == null) {
                    throw new EnviarMensajeException("No se ha establecido una clave secreta con " + contactoRemoto.getNombre() + ". Inicie un intercambio de claves.");
                }

                // 2. Cifrar el contenido del mensaje
                String contenidoCifrado = Cifrador.cifrar(mensaje.getContenido(), conversacion.getClaveSecretaAes());
                Mensaje mensajeCifrado = new Mensaje(contenidoCifrado, mensaje.getEmisor(), mensaje.getReceptor());

                System.out.println("Conexion: Enviando mensaje cifrado a " + contactoRemoto.getNombre() + ": " + contenidoCifrado);
                salida.writeObject(mensajeCifrado); // Enviar el mensaje con el contenido cifrado
                salida.flush();

            }catch (SocketException e){
                System.err.println("Conexion: SocketException al enviar mensaje: " + e.getMessage());
                throw new PerdioConexionException("Error de conexión al enviar mensaje. Intente reconectar.", e);
            } catch (Exception e) { // Capturar Exception para errores de cifrado/derivación de clave
                System.err.println("Conexion: Error al enviar o cifrar el mensaje a " + contactoRemoto.getNombre() + ": " + e.getMessage());
                throw new EnviarMensajeException("Error al enviar o cifrar el mensaje a " + contactoRemoto.getNombre(), e);
            }
        }
    }

    /**
     * Cierra las conexiones del servidor y del socket.
     */
    @Override
    public void cerrarConexiones() {
        System.out.println("Conexion: Cerrando conexiones...");
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
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
            System.out.println("Conexion: Conexiones cerradas.");
        } catch (IOException e) {
            System.err.println("Conexion: Error al cerrar conexiones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void conectar(Map.Entry<String, Integer> entry) throws IOException, PuertoEnUsoException {
        try {
            // Asegurarse de que el socket esté cerrado antes de intentar una nueva conexión
            if (this.socket != null && !this.socket.isClosed()) {
                this.socket.close();
            }
            this.socket = new Socket();
            System.out.println("Conexion: Intentando conectar al servidor " + entry.getKey() + ":" + entry.getValue() + "...");
            this.socket.connect(new InetSocketAddress(entry.getKey(), entry.getValue()));
            System.out.println("Conexion: Conexión socket establecida con " + entry.getKey() + ":" + entry.getValue());
            this.socket.setReuseAddress(true);

            // Enviar intento de conexion (primer mensaje de texto al servidor)
            // Asegúrate de que el servidor esté esperando este "CLIENTE"
            this.registroOut = new PrintWriter(socket.getOutputStream(), true);
            this.registroOut.println("CLIENTE");

            sleep(50); // Pequeña pausa para asegurar que el PrintWritter envíe el mensaje

            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());

            sleep(50); // Pequeña pausa

            System.out.println("Conexion: Enviando información de usuario al servidor: " + usuario.toString());
            // Enviar el objeto Contacto (usuario local) al servidor para registro/identificación
            this.salida.writeObject(usuario);
            this.salida.flush();

            String estaOcupado = (String) this.entrada.readObject();

            if ("El nickname ya está en uso.".equals(estaOcupado)) {
                throw new PuertoEnUsoException("El nickname ya está en uso.");
            } else {
                System.out.println("Conexion: Conexión establecida y usuario registrado con el servidor en el puerto: " + entry.getValue());
            }

        } catch (UnknownHostException e) {
            System.err.println("Conexion: Error: Host desconocido: " + e.getMessage());
            throw e;
        }
        catch (InterruptedException e) {
            System.err.println("Conexion: Error: Hilo interrumpido durante la conexión: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restablecer el estado de interrupción
            throw new IOException("Conexión interrumpida.", e);
        }
        catch (ClassNotFoundException e) {
            System.err.println("Conexion: Error: Clase no encontrada al recibir respuesta del servidor: " + e.getMessage());
            throw new IOException("Error de protocolo con el servidor.", e);
        } catch (IOException e) {
            System.err.println("Conexion: Fallo en la conexión inicial al servidor " + entry.getKey() + ":" + entry.getValue() + ": " + e.getMessage());
            throw e; // Relanzar la excepción para el manejo de reconexión
        }
    }

    public void reconectar() throws IOException {
        this.abrirMensajeConectando();
        System.out.println("Conexion: Intentando reconectar al servidor.");
        boolean conectado = false;
        for(int i = 0; i < servers.size() && !conectado; i++){ // Iterar sobre los servidores disponibles
            Map.Entry<String, Integer> currentServer = servers.get(i);
            System.out.println("Conexion: Intentando conectar al servidor " + currentServer.getKey() + ":" + currentServer.getValue() + " (Intento " + (i + 1) + "/" + servers.size() + ")");
            try {
                this.conectar(currentServer);
                this.serverActivo = i; // Actualizar el servidor activo
                conectado = true;
            } catch (IOException | PuertoEnUsoException e) {
                System.err.println("Conexion: Fallo de conexión o puerto en uso con " + currentServer.getKey() + ":" + currentServer.getValue() + ": " + e.getMessage());
                try {
                    sleep(3000); // Esperar antes de intentar con el siguiente servidor
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); // Restablecer el estado de interrupción
                    throw new IOException("Reconexión interrumpida.", ex);
                }
            }
        }

        this.cerrarMensajeConectando();

        if (!conectado) {
            throw new IOException("No se pudo conectar a ninguno de los servidores disponibles después de varios intentos.");
        }
    }

    private void cerrarMensajeConectando() {
        Controlador.getInstancia().cerrarMensajeConectando();
    }

    private void abrirMensajeConectando() {
        Controlador.getInstancia().abrirMensajeConectando();
    }

    /**
     * Método que ejecuta el hilo del servidor para esperar mensajes.
     */
    @Override
    public void run() {
        esperarMensajes(); // Esta es la lógica principal del hilo de la conexión
    }

    /**
     * Obtiene el Socket de la conexión actual.
     * @return El Socket de la conexión actual.
     */
    public Socket getSocket() {
        return socket;
    }

    // *****************************************************************
    // *** NUEVO GETTER: Obtiene el Contacto asociado a esta conexión ***
    // *****************************************************************
    /**
     * Obtiene el Contacto del usuario asociado a esta conexión.
     * @return El Contacto del usuario.
     */
    public Contacto getUsuario() {
        return usuario;
    }

    // *****************************************************************
    // *** NUEVO GETTER: Obtiene la clave pública DH de esta conexión ***
    // *****************************************************************
    /**
     * Obtiene la clave pública Diffie-Hellman de este cliente.
     * @return La clave pública DH, o null si no ha sido establecida.
     */
    @Override // Asegúrate de que esta anotación sea válida si IConexion también lo tiene
    public PublicKey getMiClavePublicaDH() {
        if (miParClavesDH != null) {
            return miParClavesDH.getPublic();
        }
        return null;
    }


    @Override
    public void update(Observable o, Object arg) {
        // En esta configuración, la lógica de procesamiento de mensajes y de objetos de control
        // se ha movido casi en su totalidad al Controlador.
        // ManejadorEntradas ahora notifica directamente al Controlador.
        // Si Conexion no necesita procesar ninguna notificación de ManejadorEntradas aquí,
        // puedes dejar este método vacío o eliminarlo si Conexion no es Observer de nada.
        // (Pero la interfaz IConexion lo extiende de Observer, por lo que debe implementarlo).
    }

    // --- Implementación de los métodos de Intercambio de Claves (parte de IConexion) ---

    /**
     * Inicia el proceso de intercambio de claves Diffie-Hellman con un contacto remoto.
     * Envía la clave pública DH de este cliente al contacto remoto a través del servidor.
     * @param contactoRemoto El contacto con el que se quiere iniciar el intercambio de claves.
     * @throws Exception Si ocurre un error al generar o enviar la clave pública.
     */
    @Override
    public void iniciarIntercambioDeClaves(Contacto contactoRemoto) throws Exception {
        if (salida == null) {
            throw new IOException("El canal de salida no está inicializado para iniciar el intercambio de claves.");
        }
        // Obtener la clave pública DH del usuario local
        if (miParClavesDH == null || miParClavesDH.getPublic() == null) {
            throw new IllegalStateException("La clave pública Diffie-Hellman del usuario local no ha sido establecida en Conexion.");
        }
        PublicKey miClavePublicaDH = miParClavesDH.getPublic();

        // Crear un "mensaje" especial para enviar la clave pública
        Map<String, Serializable> keyExchangeMessage = new HashMap<>();
        keyExchangeMessage.put("tipo", "CLAVE_PUBLICA_DH");
        keyExchangeMessage.put("clavePublica", ClaveUtil.publicKeyAString(miClavePublicaDH)); // Enviar como String Base64
        keyExchangeMessage.put("emisor", usuario); // El usuario local es el emisor de su clave pública
        keyExchangeMessage.put("receptor", contactoRemoto); // A quién se destina esta clave pública

        System.out.println("Conexion: Enviando clave pública DH a " + contactoRemoto.getNombre() + " a través del servidor.");
        salida.writeObject(keyExchangeMessage);
        salida.flush();
    }


}