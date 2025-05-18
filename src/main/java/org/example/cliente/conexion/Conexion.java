package org.example.cliente.conexion;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * Clase que representa una conexión de servidor.
 * Implementa la interfaz IConexion.
 */
public class Conexion implements IConexion, Observer {

    private Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private PrintWriter registroOut;
    private String ip;
    private Contacto usuario;
    private ArrayList<Map.Entry<String, Integer>> servers;
    private int serverActivo;
    private int puertoRespaldo;
    private int puerto;


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
     *
     */
    @Override
    public void conectarServidor(Contacto usuario) throws PuertoEnUsoException, IOException, PerdioConexionException {
        this.usuario = usuario;
//        if (elPuertoEstaEnUso(puerto)) {
//            throw new PuertoEnUsoException("El puerto " + puerto + " ya está en uso.");
//        }
        try {
            this.socket = new Socket();

            try (BufferedReader reader = new BufferedReader(new FileReader("clienteConfig.txt"))) {
                ip = reader.readLine().trim();
                puerto = Integer.parseInt(reader.readLine().trim());
                puertoRespaldo = Integer.parseInt(reader.readLine().trim());

            } catch (NumberFormatException e) {
                throw new RuntimeException("Error al leer el puerto desde el archivo de configuracion");
            } catch (IOException e) {
                throw new RuntimeException("Error al abrir el archivo de configuracion");
            }

            this.servers = new ArrayList<>();
            this.servers.add(new AbstractMap.SimpleEntry<>(this.ip, this.puerto));
            this.servers.add(new AbstractMap.SimpleEntry<>(this.ip, this.puertoRespaldo));

            // Conexión a servidor
            this.serverActivo = 0;

            conectar(this.servers.get(this.serverActivo));

        } catch (UnknownHostException e) {
            System.err.println("Error: Host desconocido.");
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

    public ArrayList<Contacto> obtenerContactos() throws PerdioConexionException {
        try {

            Contacto c = new Contacto("Contactos", "111", 0);
            this.salida.writeObject(c);
            this.salida.flush();

            return null;
        }catch (SocketException e){
            throw new PerdioConexionException("Error: No se pudo conectar al servidor en el puerto " + puerto + ". Asegúrese de que el servidor esté en ejecución.");
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

        new Thread(new ManejadorEntradas(socket, entrada,this)).start();

    }

    /**
     * Envía un mensaje a un usuario específico.
     * @param usuarioDTO El usuario al que se enviará el mensaje.
     * @param mensaje El mensaje que se enviará.
     * @throws EnviarMensajeException Si ocurre un error al enviar el mensaje.
     */
    @Override
    public void enviarMensaje(Contacto usuarioDTO, Mensaje mensaje) throws EnviarMensajeException, IOException, PerdioConexionException {
        if (salida == null) {
            throw new IOException("El canal de salida no está inicializado.");
        } else {
            System.out.println("Intentando enviar mensaje a " + usuarioDTO);
            try {
                System.out.println("Enviando mensaje a " + usuarioDTO + ": " + mensaje.getContenido());
                salida.writeObject(mensaje);
                salida.flush();

            }catch (SocketException e){
                throw new PerdioConexionException("Error: No se pudo conectar al servidor en el puerto " + puerto + ". Asegúrese de que el servidor esté en ejecución.");
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


    public void conectar(Map.Entry<String, Integer> entry) throws IOException, PuertoEnUsoException {
        try {

            this.socket = new Socket();
            System.out.println("Intentando conectar al servidor " + ip + ":" + entry.getValue() + ".");
            this.socket.connect(new InetSocketAddress(ip, entry.getValue()));
            System.out.println("Estoy aca?");
            this.socket.setReuseAddress(true);

            // Enviar intento de conexion
            this.registroOut = new PrintWriter(socket.getOutputStream(), true);
            this.registroOut.println("CLIENTE");

            sleep(50);

            System.out.println("Conexión autorizada.");
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());

            sleep(50);

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

        } catch (UnknownHostException e) {
            System.err.println("Error: Host desconocido.");
        }
        catch (InterruptedException e) {
            System.err.println("Error: Hilo interrumpido.");
        }
        catch (ClassNotFoundException e) {
            System.err.println("Error: Clase no encontrada.");
        }

    }

    public void reconectar() throws IOException {
        this.abrirMensajeConectando();
        System.out.println("Intentando reconectar al servidor " + ip + ":" + puerto + ".");
        boolean conectado= false;
        for(int i= 5; i>0 && !conectado; i--){

            try{
                this.conectar(servers.get(this.serverActivo));
                conectado=true;

            }catch (IOException e) {
                try {
                    sleep(3000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                if(serverActivo==1){
                    this.serverActivo=0;
                }else {
                    System.out.println("Intentando reconectar al servidor " + ip + ":" + puertoRespaldo + ".");
                    this.serverActivo=1;
                }
            }catch(PuertoEnUsoException e){
                System.out.println("reconectado");
            }

        }

        this.cerrarMensajeConectando();

        if (!conectado) {

            throw new IOException("No se pudo conectar a ninguno de los servidores disponibles.");
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
        esperarMensajes();
    }

    /**
     * Obtiene el Socket de la conexión actual.
     * @return El Socket de la conexión actual.
     */
    public Socket getSocket() {
        return socket;
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}