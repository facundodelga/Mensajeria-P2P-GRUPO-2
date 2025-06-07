package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO; // Importar DirectorioDTO para la recepción de contactos

import java.io.*;
import java.net.*;
import java.util.*;
// No se usa Thread.sleep estáticamente, se llama como Thread.sleep()

/**
 * Clase que representa una conexión de servidor.
 * Implementa la interfaz IConexion.
 */
public class Conexion implements IConexion { // Removed 'implements Observer'

    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private PrintWriter registroOut; // Para enviar comandos de texto iniciales (CLIENTE/SERVIDOR)
    private String serverIpPrincipal; // La IP principal del servidor
    private Contacto usuarioActual; // El usuario que se conecta
    private ArrayList<Map.Entry<String, Integer>> servers; // Lista de servidores (principal y respaldo)
    private int serverActivoIndex; // Índice del servidor actualmente conectado en la lista `servers`
    private Controlador controlador; // Referencia al controlador

    /**
     * Constructor de la clase Conexion.
     */
    public Conexion() {
        this.servers = new ArrayList<>();
        this.serverActivoIndex = 0; // Por defecto, empezar con el primer servidor
    }

    /**
     * Establece la referencia al Controlador.
     * Implementa el método de la interfaz IConexion.
     * @param controlador La instancia del Controlador.
     */
    @Override
    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    /*
     * Eliminado: elPuertoEstaEnUso() ya que no se utiliza y no es relevante para la lógica de conexión remota del cliente.
     */

    /**
     * Intenta conectar al servidor. Lee la configuración desde "clienteConfig.txt".
     *
     * @param usuario El contacto del usuario actual.
     * @throws PuertoEnUsoException Si el nickname ya está en uso en el servidor.
     * @throws IOException Si ocurre un error de E/S.
     * @throws PerdioConexionException Si no se puede establecer la conexión inicial.
     */
    @Override
    public void conectarServidor(Contacto usuario) throws PuertoEnUsoException, IOException, PerdioConexionException {
        this.usuarioActual = usuario;

        try (BufferedReader reader = new BufferedReader(new FileReader("clienteConfig.txt"))) {
            serverIpPrincipal = reader.readLine().trim();
            int puertoPrincipal = Integer.parseInt(reader.readLine().trim());
            int puertoRespaldo = Integer.parseInt(reader.readLine().trim());

            this.servers.clear(); // Limpiar por si acaso
            this.servers.add(new AbstractMap.SimpleEntry<>(serverIpPrincipal, puertoPrincipal));
            this.servers.add(new AbstractMap.SimpleEntry<>(serverIpPrincipal, puertoRespaldo));

        } catch (NumberFormatException e) {
            throw new RuntimeException("Error al leer los puertos desde el archivo de configuración: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error al abrir el archivo de configuración 'clienteConfig.txt': " + e.getMessage(), e);
        }

        // Intenta conectar al servidor principal primero
        this.serverActivoIndex = 0; // Asegura que siempre se intenta el principal primero
        try {
            conectar(this.servers.get(this.serverActivoIndex));
        } catch (PuertoEnUsoException e) {
            // Este caso es para el nickname ya en uso, debe propagarse al controlador
            throw e;
        } catch (IOException e) {
            // Si el servidor principal falla, intenta con el de respaldo
            System.out.println("No se pudo conectar al servidor principal (" + servers.get(0).getKey() + ":" + servers.get(0).getValue() + "). Intentando con el de respaldo...");
            this.serverActivoIndex = 1; // Cambiar al servidor de respaldo
            try {
                conectar(this.servers.get(this.serverActivoIndex));
            } catch (PuertoEnUsoException ex) {
                throw ex; // Propagar si el nickname ya está en uso en el respaldo
            } catch (IOException ex) {
                // Si ambos fallan, entonces es una pérdida de conexión inicial
                throw new PerdioConexionException("No se pudo conectar a ningún servidor disponible. " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Establece una conexión con el servidor especificado (IP y Puerto).
     * @param serverEntry El par IP-Puerto del servidor al que conectar.
     * @throws IOException Si ocurre un error de E/S al conectar o inicializar streams.
     * @throws PuertoEnUsoException Si el nickname ya está en uso en el servidor.
     */
    @Override
    public void conectar(Map.Entry<String, Integer> serverEntry) throws IOException, PuertoEnUsoException {
        // Cierra conexiones previas si existen antes de intentar una nueva
        cerrarConexiones(); // Asegúrate de que los recursos anteriores se liberen

        this.socket = new Socket();
        String currentIp = serverEntry.getKey();
        int currentPort = serverEntry.getValue();

        System.out.println("Intentando conectar al servidor " + currentIp + ":" + currentPort + "...");
        this.socket.connect(new InetSocketAddress(currentIp, currentPort));
        this.socket.setReuseAddress(true); // Permite reusar la dirección local inmediatamente

        // Obtener el OutputStream e InputStream primero para evitar deadlocks con ObjectOutputStream/ObjectInputStream
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();

        // ObjectOutputStream debe crearse antes que ObjectInputStream en el lado del cliente si el servidor
        // también los crea en orden inverso, para evitar bloqueos.
        this.salida = new ObjectOutputStream(os);
        this.entrada = new ObjectInputStream(is);

        // Enviar intento de conexion (tipo de cliente)
        // Usamos un PrintWriter para enviar la cadena inicial "CLIENTE"
        this.registroOut = new PrintWriter(os, true); // `true` para auto-flush
        this.registroOut.println("CLIENTE");
        // No es necesario Thread.sleep() aquí; el auto-flush debería ser suficiente.

        // Enviar el objeto UsuarioDTO al servidor
        this.salida.writeObject(usuarioActual);
        this.salida.flush();

        try {
            Object response = this.entrada.readObject();
            if (response instanceof String) {
                String serverResponse = (String) response;
                if ("El nickname ya está en uso.".equals(serverResponse)) {
                    throw new PuertoEnUsoException("El nickname ya está en uso.");
                } else if ("OK".equals(serverResponse)) { // Asumiendo que el servidor envía "OK" para éxito
                    System.out.println("Conexión establecida con el servidor en el puerto: " + currentPort);
                } else {
                    System.err.println("Respuesta inesperada del servidor al conectar: " + serverResponse);
                    throw new IOException("Respuesta inesperada del servidor: " + serverResponse);
                }
            } else {
                System.err.println("Respuesta inesperada del servidor (no es String): " + response);
                throw new IOException("Respuesta inesperada del servidor: " + response.getClass().getName());
            }

        } catch (ClassNotFoundException e) {
            throw new IOException("Error al leer la respuesta del servidor: clase no encontrada.", e);
        }
    }


    public void obtenerMensajesPendientes(){
        try {
            if (salida != null && socket.isConnected()) {
                // Asegúrate de que el servidor espera un String "MensajesPendientes"
                this.salida.writeObject("MensajesPendientes");
                this.salida.flush();
                System.out.println("Solicitud de mensajes pendientes enviada.");
            } else {
                System.err.println("No se pudo solicitar mensajes pendientes: salida no inicializada o socket no conectado.");
            }
        } catch (IOException e) {
            System.err.println("Error al solicitar mensajes pendientes: " + e.getMessage());
            // Notificar al controlador sobre la posible pérdida de conexión
            if (controlador != null) {
                controlador.update(null, new PerdioConexionException("Error al solicitar mensajes pendientes."));
            }
        }
    }

    @Override
    public ArrayList<Contacto> obtenerContactos() throws PerdioConexionException {
        try {
            if (salida != null && socket.isConnected()) {
                // Enviar un comando o un objeto de petición para obtener contactos
                // Asumo que el servidor espera un String específico o un objeto que represente esta petición
                this.salida.writeObject("GET_CONTACTS"); // Un comando simple como String
                this.salida.flush();
                System.out.println("Solicitud de contactos enviada.");
            } else {
                System.err.println("No se pudo solicitar contactos: salida no inicializada o socket no conectado.");
            }
            // El DirectorioDTO se recibe y procesa en el ManejadorEntradas, no aquí.
            // Este método solo inicia la petición.
            return null; // O una lista vacía, ya que el Controlador se actualizará vía `update`
        } catch (SocketException e){
            throw new PerdioConexionException("Error: La conexión al servidor se perdió al intentar obtener contactos. " + e.getMessage());
        } catch (IOException e){
            System.err.println("Error al solicitar contactos: " + e.getMessage());
            // También podemos notificar al controlador directamente para que reaccione a esta excepción
            if (controlador != null) {
                controlador.update(null, new Exception("Error al solicitar contactos: " + e.getMessage()));
            }
            return null; // O una lista vacía
        }
    }

    /**
     * Espera conexiones entrantes y maneja los mensajes recibidos.
     * Inicia un hilo `ManejadorEntradas` que escuchará en el `ObjectInputStream`.
     */
    @Override
    public void esperarMensajes() {
        if (entrada == null || socket == null || !socket.isConnected()) {
            System.err.println("No se puede esperar mensajes: Entrada o socket no están listos o conectados.");
            return;
        }
        // El ManejadorEntradas necesita una referencia al Controlador para notificar los eventos
        // como la recepción de Mensajes o DirectorioDTO.
        new Thread(new ManejadorEntradas(socket, entrada, this.controlador)).start();
        System.out.println("ManejadorEntradas iniciado para esperar mensajes.");
    }

    /**
     * Envía un mensaje a un usuario específico.
     * @param receptor El contacto al que se enviará el mensaje.
     * @param mensaje El mensaje que se enviará.
     * @throws EnviarMensajeException Si ocurre un error al enviar el mensaje.
     * @throws IOException Si ocurre un error de E/S.
     * @throws PerdioConexionException Si la conexión al servidor se pierde.
     */
    @Override
    public void enviarMensaje(Contacto receptor, Mensaje mensaje) throws EnviarMensajeException, IOException, PerdioConexionException {
        if (salida == null || !socket.isConnected() || socket.isClosed()) {
            throw new PerdioConexionException("La conexión al servidor no está activa. Reintentando...");
        } else {
            System.out.println("Intentando enviar mensaje a " + receptor.getNombre());
            try {
                // Enviar el Mensaje (que ya está cifrado si aplica)
                salida.writeObject(mensaje);
                salida.flush();
                System.out.println("Mensaje enviado a " + receptor.getNombre() + ": " + mensaje.getContenidoCifrado());

            } catch (SocketException e){
                throw new PerdioConexionException("Error: La conexión al servidor se perdió al enviar mensaje. " + e.getMessage());
            } catch (IOException e) {
                throw new EnviarMensajeException("Error al enviar el mensaje a " + receptor.getNombre(), e);
            }
        }
    }

    /**
     * Cierra las conexiones del servidor y del socket.
     */
    @Override
    public void cerrarConexiones() {
        try {
            if (salida != null && socket != null && socket.isConnected()) {
                // Enviar un comando de desconexión al servidor si el usuario actual está inicializado
                if (usuarioActual != null && usuarioActual.getNombre() != null) {
                    try {
                        salida.writeObject("DISCONNECT:" + usuarioActual.getNombre());
                        salida.flush();
                        System.out.println("Comando de desconexión enviado al servidor para " + usuarioActual.getNombre());
                    } catch (IOException e) {
                        System.err.println("Error al enviar comando de desconexión: " + e.getMessage());
                    }
                }
            }
            if (entrada != null) {
                entrada.close();
                entrada = null;
            }
            if (salida != null) {
                salida.close();
                salida = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
            System.out.println("Conexiones cerradas.");
        } catch (IOException e) {
            System.err.println("Error al cerrar conexiones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Intenta reconectar al servidor activo o al de respaldo.
     * @throws IOException Si no se puede reconectar a ningún servidor después de varios intentos.
     */
    @Override
    public void reconectar() throws IOException {
        this.abrirMensajeConectando();
        System.out.println("Iniciando proceso de reconexión...");
        boolean conectado = false;
        int intentosMaximos = 5; // Número de intentos para cada servidor
        int serverInicialIndex = this.serverActivoIndex; // Recuerda el servidor actual

        for (int i = 0; i < intentosMaximos * 2; i++) { // Intentos para ambos servidores (intentosMaximos por servidor)
            try {
                // Alternar entre los servidores
                this.serverActivoIndex = (serverInicialIndex + (i / intentosMaximos)) % servers.size();
                Map.Entry<String, Integer> currentServer = servers.get(this.serverActivoIndex);
                System.out.println("Intentando reconectar al servidor " + currentServer.getKey() + ":" + currentServer.getValue() + " (Intento " + (i + 1) + ")");

                conectar(currentServer);
                conectado = true;
                System.out.println("¡Reconexión exitosa!");
                break; // Conectado, salir del bucle
            } catch (PuertoEnUsoException e) {
                // Si el nickname ya está en uso, significa que sí hay conexión pero no podemos usarlo.
                // Esto es un error irrecuperable en este contexto de reconexión.
                System.err.println("Error de reconexión: " + e.getMessage());
                this.cerrarMensajeConectando();
                throw new IOException("Fallo la reconexión: " + e.getMessage(), e);
            } catch (IOException e) {
                System.err.println("Fallo la conexión al servidor " + servers.get(this.serverActivoIndex).getValue() + ": " + e.getMessage());
                try {
                    Thread.sleep(3000); // Esperar antes de reintentar
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                    throw new IOException("Hilo de reconexión interrumpido.", ex);
                }
            }
        }

        this.cerrarMensajeConectando();

        if (!conectado) {
            throw new IOException("No se pudo conectar a ninguno de los servidores disponibles después de varios intentos.");
        }
    }

    private void cerrarMensajeConectando() {
        if (controlador != null) {
            controlador.cerrarMensajeConectando();
        }
    }

    private void abrirMensajeConectando() {
        if (controlador != null) {
            controlador.abrirMensajeConectando();
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

    /*
     * Eliminado: update(Observable o, Object arg) ya que la clase Conexion ya no implementa Observer.
     * La lógica de notificación al controlador se maneja a través de setControlador y llamadas directas.
     */
}