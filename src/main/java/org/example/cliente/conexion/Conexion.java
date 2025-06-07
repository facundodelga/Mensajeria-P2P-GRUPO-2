package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.lang.Thread.sleep;

public class Conexion implements IConexion, Observer {

    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private String ip;
    private Contacto usuario;
    private ArrayList<Map.Entry<String, Integer>> servers;
    private int serverActivo;
    private int puertoRespaldo;
    private int puerto;
    private Controlador controlador;

    public Conexion() {
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    private boolean elPuertoEstaEnUso(int puerto) {
        try (ServerSocket ignored = new ServerSocket(puerto)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public void conectarServidor(Contacto usuario) throws PuertoEnUsoException, IOException, PerdioConexionException {
        this.usuario = usuario;

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader("clienteConfig.txt"))) {
                ip = reader.readLine().trim();
                puerto = Integer.parseInt(reader.readLine().trim());
                puertoRespaldo = Integer.parseInt(reader.readLine().trim());

            } catch (NumberFormatException e) {
                throw new RuntimeException("Error al leer el puerto desde el archivo de configuracion: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException("Error al abrir el archivo de configuracion: " + e.getMessage());
            }

            this.servers = new ArrayList<>();
            this.servers.add(new AbstractMap.SimpleEntry<>(this.ip, this.puerto));
            this.servers.add(new AbstractMap.SimpleEntry<>(this.ip, this.puertoRespaldo));

            this.serverActivo = 0;

            conectar(this.servers.get(this.serverActivo));

        } catch (UnknownHostException e) {
            System.err.println("Error: Host desconocido: " + e.getMessage());
            throw e;
        }
    }

    public void obtenerMensajesPendientes(){
        try {
            if (salida != null) {
                this.salida.writeObject("MensajesPendientes");
                this.salida.flush();
            } else {
                System.err.println("Error: Salida de objetos no inicializada al intentar obtener mensajes pendientes.");
            }
        } catch (IOException e) {
            System.err.println("Error al obtener mensajes pendientes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ArrayList<Contacto> obtenerContactos() throws PerdioConexionException {
        try {
            if (salida == null) {
                throw new PerdioConexionException("Error: La conexión no está activa. No se puede obtener contactos.");
            }
            Contacto c = new Contacto("Contactos", "111", 0);
            this.salida.writeObject(c);
            this.salida.flush();

            return null;
        } catch (SocketException e){
            throw new PerdioConexionException("Error de socket al obtener contactos: " + e.getMessage() + ". Asegúrese de que el servidor esté en ejecución.");
        } catch (IOException e) {
            System.err.println("Error de E/S al obtener contactos: " + e.getMessage());
            e.printStackTrace();
            throw new PerdioConexionException("Error de E/S al obtener contactos: " + e.getMessage());
        } catch (Exception e){
            System.err.println("Error inesperado al obtener contactos: " + e.getMessage());
            e.printStackTrace();
            throw new PerdioConexionException("Error inesperado al obtener contactos: " + e.getMessage());
        }
    }

    @Override
    public void esperarMensajes() {
        new Thread(new ManejadorEntradas(socket, entrada, this.controlador)).start();
    }

    @Override
    public void enviarMensaje(Contacto usuarioDTO, Mensaje mensaje) throws EnviarMensajeException, IOException, PerdioConexionException {
        if (salida == null || socket == null || !socket.isConnected()) {
            throw new PerdioConexionException("Error: La conexión no está activa o el canal de salida no está inicializado.");
        } else {
            System.out.println("Intentando enviar mensaje a " + usuarioDTO.getNombre());
            try {
                System.out.println("Enviando mensaje a " + usuarioDTO.getNombre() + ": " + mensaje.getContenidoCifrado());
                salida.writeObject(mensaje);
                salida.flush();
            } catch (SocketException e){
                throw new PerdioConexionException("Error de socket al enviar mensaje: " + e.getMessage() + ". Posiblemente se perdió la conexión.");
            } catch (IOException e) {
                throw new EnviarMensajeException("Error al enviar el mensaje a " + usuarioDTO.getNombre(), e);
            }
        }
    }

    @Override
    public void cerrarConexiones() {
        try {
            System.out.println("Cerrando conexiones...");
            if (salida != null) {
                salida.flush();
                salida.close();
                salida = null;
            }
            if (entrada != null) {
                entrada.close();
                entrada = null;
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

    public void conectar(Map.Entry<String, Integer> entry) throws IOException, PuertoEnUsoException {
        try {
            this.ip = entry.getKey();
            this.puerto = entry.getValue();

            System.out.println("Intentando conectar al servidor " + ip + ":" + puerto + ".");
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(ip, puerto), 5000);

            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());

            this.salida.writeObject("CLIENTE_HANDSHAKE");
            this.salida.flush();

            System.out.println("Enviando UsuarioDTO: " + usuario.toString());
            this.salida.writeObject(usuario);
            this.salida.flush();

            Object response = this.entrada.readObject();
            if (response instanceof String) {
                String serverResponse = (String) response;
                if ("El nickname ya está en uso.".equals(serverResponse)) {
                    throw new PuertoEnUsoException("El nickname ya está en uso.");
                } else if ("CONEXION_ESTABLECIDA".equals(serverResponse)) {
                    System.out.println("Conexión establecida con el servidor en el puerto: " + puerto);
                } else {
                    System.out.println("Respuesta inesperada del servidor: " + serverResponse);
                }
            } else {
                System.out.println("Respuesta de objeto inesperada del servidor: " + response.getClass().getName());
            }

        } catch (SocketTimeoutException e) {
            throw new IOException("Tiempo de espera agotado al conectar al servidor " + ip + ":" + puerto + ". " + e.getMessage());
        } catch (ConnectException e) {
            throw new IOException("Conexión rechazada por el servidor " + ip + ":" + puerto + ". " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("Error: Host desconocido: " + e.getMessage());
            throw e;
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Clase no encontrada al leer objeto del servidor: " + e.getMessage());
            throw new IOException("Error de serialización/deserialización con el servidor: " + e.getMessage());
        }
    }

    public void reconectar() throws IOException {
        this.abrirMensajeConectando();
        System.out.println("Intentando reconectar al servidor.");
        boolean conectado = false;
        int attempts = 5;

        for (int i = 0; i < attempts && !conectado; i++) {
            try {
                cerrarConexiones();

                System.out.println("Intento " + (i + 1) + ": Conectando a " + this.servers.get(this.serverActivo).getKey() + ":" + this.servers.get(this.serverActivo).getValue());
                this.conectar(servers.get(this.serverActivo));
                conectado = true;
                System.out.println("Reconexión exitosa al servidor " + servers.get(this.serverActivo).getValue());

            } catch (IOException e) {
                System.err.println("Fallo el intento " + (i + 1) + " de reconexión: " + e.getMessage());
                if (i < attempts - 1) {
                    try {
                        sleep(3000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    if(serverActivo==1){
                        this.serverActivo=0;
                    }else {
                        System.out.println("Intentando cambiar al servidor de respaldo " + ip + ":" + puertoRespaldo + ".");
                        this.serverActivo=1;
                    }
                }
            } catch(PuertoEnUsoException e){
                System.out.println("Reconectado (nickname en uso, verificar lógica): " + e.getMessage()); // This case indicates server accepted, but nickname issue
                conectado = true; // Still counts as connected for reconn. loop
            }
        }

        this.cerrarMensajeConectando();

        if (!conectado) {
            throw new IOException("No se pudo conectar a ninguno de los servidores disponibles después de varios reintentos.");
        }
    }

    private void cerrarMensajeConectando() {
        Controlador.getInstancia().cerrarMensajeConectando();
    }

    private void abrirMensajeConectando() {
        Controlador.getInstancia().abrirMensajeConectando();
    }

    @Override
    public void run() {
        esperarMensajes();
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void update(Observable o, Object arg) {
    }
}