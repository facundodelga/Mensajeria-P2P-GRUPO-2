package org.example.cliente.controlador;

import org.example.cliente.conexion.*;
import org.example.cliente.modelo.*;
import org.example.cliente.vista.*;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Usuario;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.util.cifrado.Cifrador; // Keep this import
import org.example.servidor.DirectorioDTO;

import javax.crypto.SecretKey; // Keep this import
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.NoSuchAlgorithmException; // Keep this import
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap; // Keep this import
import java.util.Map; // Keep this import

import java.util.Observable; // Keep this import if Observer is still intended, but we discussed removing it
import java.util.Observer; // Keep this import if Observer is still intended, but we discussed removing it

/**
 * Clase Controlador que implementa ActionListener y Observer.
 * Maneja la lógica de la aplicación y la interacción entre la vista y el modelo.
 */
public class Controlador implements ActionListener, Observer {
    private static Controlador instancia = null;
    private IVistaPrincipal vista;
    private IVistaInicioSesion vistaInicioSesion;

    private IAgenda agendaServicio;
    private IConversacion conversacionServicio;
    private Conexion conexion;
    private Contacto usuarioDTO;
    private DirectorioDTO directorioDTO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private UsuarioServicio usuarioServicio;

    // Mapa para almacenar las claves secretas AES para cada conversación/contacto
    private Map<Contacto, SecretKey> clavesConversacion = new HashMap<>();

    /**
     * Constructor privado para el patrón Singleton.
     */
    private Controlador() {
        directorioDTO = new DirectorioDTO();
    }

    /**
     * Obtiene la instancia única de la clase Controlador.
     * @return instancia de Controlador
     */
    public static Controlador getInstancia() {
        if (instancia == null) {
            instancia = new Controlador();
        }
        return instancia;
    }

    /**
     * Maneja los eventos de acción de la interfaz de usuario.
     * @param e el evento de acción
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Iniciar":
                iniciarServidor();
                break;
            case "botonAgregarContacto":
                agregarNuevoContacto();
                break;
            case "Enviar":
                enviarMensaje();
                break;
            case "IniciarChat":
                iniciarChat();
                break;
            case "CerrarSesion":
                cerrarSesion();
                break;
            case "ObtenerContactos":
                obtenerContactos();
                break;
        }
    }

    private void cerrarSesion() {
        if (conexion != null) {
            conexion.cerrarConexiones();
        }
        vista.ocultar();
        vista.limpiarCampos();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        vistaInicioSesion.mostrar();
        conexion = null;
        clavesConversacion.clear();
    }

    /**
     * Inicia un nuevo chat con el contacto seleccionado.
     */
    private void iniciarChat() {
        System.out.println("Inicio de chat");
        Contacto selectedValue = vista.getListaContactos().getSelectedValue();

        if (selectedValue != null) {
            ChatPantalla newChat = new ChatPantalla(selectedValue);
            if (!vista.getModeloChats().contains(newChat)) { // Check if chat already exists using a ChatPantalla object
                System.out.println("inicio de chat " + selectedValue);
                this.conversacionServicio.agregarConversacion(selectedValue);

                // Generar/Obtener una clave para esta nueva conversación
                try {
                    SecretKey nuevaClave = Cifrador.generarClaveAES();
                    clavesConversacion.put(selectedValue, nuevaClave);
                    System.out.println("DEBUG: Clave AES generada para " + selectedValue.getNombre() + ": " + Cifrador.claveATexto(nuevaClave));
                } catch (NoSuchAlgorithmException e) {
                    mostrarMensajeFlotante("Error al generar clave de cifrado: " + e.getMessage(), Color.RED);
                    e.printStackTrace();
                    return;
                }

                vista.getModeloChats().addElement(newChat); // Add the new ChatPantalla object
                vista.getListaChats().setSelectedValue(newChat, true); // Select the new ChatPantalla object
                vista.getPanelMensajes().revalidate();
                vista.getPanelMensajes().repaint();
            } else {
                mostrarMensajeFlotante("El contacto " + selectedValue.getNombre() + " ya tiene un chat iniciado", Color.RED);
            }
        } else {
            mostrarMensajeFlotante("Seleccione un contacto", Color.RED);
        }
    }

    /**
     * Envía un mensaje al contacto seleccionado.
     */
    private void enviarMensaje()  {
        ChatPantalla selectedChat = vista.getListaChats().getSelectedValue();
        if (selectedChat == null) {
            mostrarMensajeFlotante("Seleccione un chat para enviar un mensaje.", Color.ORANGE);
            return;
        }
        Contacto receptor = selectedChat.getContacto();
        String contenidoTextoPlano = vista.getCampoMensaje().getText();

        if (contenidoTextoPlano.trim().isEmpty()) {
            mostrarMensajeFlotante("El mensaje no puede estar vacío.", Color.ORANGE);
            return;
        }

        SecretKey claveConversacion = clavesConversacion.get(receptor);
        if (claveConversacion == null) {
            mostrarMensajeFlotante("ERROR: No hay clave de cifrado para la conversación con " + receptor.getNombre() + ". Mensaje no enviado.", Color.RED);
            System.err.println("No se encontró clave para " + receptor.getNombre() + ". Mensaje no enviado.");
            return;
        }

        String contenidoCifrado = null;
        try {
            contenidoCifrado = Cifrador.cifrar(contenidoTextoPlano, claveConversacion);
            System.out.println("DEBUG: Mensaje cifrado: " + contenidoCifrado);
        } catch (Exception e) {
            mostrarMensajeFlotante("Error al cifrar el mensaje: " + e.getMessage(), Color.RED);
            e.printStackTrace();
            return;
        }

        Mensaje mensaje = new Mensaje(this.usuarioDTO.getNombre(), receptor.getNombre(), contenidoCifrado);

        try {
            conexion.enviarMensaje(receptor, mensaje);
            this.conversacionServicio.addMensajeSaliente(receptor, mensaje);
            vista.getCampoMensaje().setText("");

            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    new Mensaje(this.usuarioDTO.getNombre(), receptor.getNombre(), contenidoTextoPlano),
                    true,
                    sdf.format(mensaje.getTimestamp())));
        } catch (PerdioConexionException e){
            reconectar();

        } catch (EnviarMensajeException | IOException e) {
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
        }
    }

    /**
     * Inicia el servidor con los datos proporcionados por la vista de inicio de sesión.
     */
    public void iniciarServidor() {
        vistaInicioSesion.mostrar();
        String nombre = vistaInicioSesion.getNombre();
        if(nombre.isEmpty()){
            mostrarMensajeFlotante("El nombre no puede estar vacío", Color.RED);
            return;
        }
        try {
            int puerto = Integer.parseInt(vistaInicioSesion.getPuerto());
            vistaInicioSesion.ocultar();

            Usuario usuario = new Usuario(nombre, "127.0.0.1", puerto);
            this.usuarioServicio = new UsuarioServicio(usuario);
            this.agendaServicio = new AgendaServicio(usuario);
            this.conversacionServicio = new ConversacionServicio(usuario);
            this.conexion = new Conexion(); // Instanciar la clase concreta Conexion

            this.usuarioDTO = new Contacto(usuario);

            conexion.conectarServidor(usuarioDTO);
            conexion.setControlador(this); // Pasar la instancia de Controlador a Conexion
            new Thread(conexion).start(); // Inicia el hilo de Conexion (que a su vez iniciará ManejadorEntradas)

            vista.mostrar();
            vista.titulo("Usuario: " + nombre + " | Ip: "+ "127.0.0.1" + " | Puerto: " + puerto);
            vista.informacionDelUsuario(usuarioDTO);

            // Para el propio usuario, inicializa su clave de conversación consigo mismo
            try {
                SecretKey selfKey = Cifrador.generarClaveAES();
                clavesConversacion.put(usuarioDTO, selfKey);
                System.out.println("DEBUG: Clave AES generada para propio usuario: " + Cifrador.claveATexto(selfKey));
            } catch (NoSuchAlgorithmException e) {
                mostrarMensajeFlotante("Error al generar clave para el propio usuario: " + e.getMessage(), Color.RED);
                e.printStackTrace();
            }

        }catch (NumberFormatException e) {
            mostrarMensajeFlotante("El puerto debe ser un número entre 0 y 65535", Color.RED);

        }catch (PuertoEnUsoException e){
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
            vistaInicioSesion.mostrar();
        } catch (IOException | PerdioConexionException e) {
            reconectar();
            vista.mostrar();
            vista.titulo("Usuario: " + nombre + " | Ip: "+ "127.0.0.1" + " | Puerto: " + usuarioDTO.getPuerto());
            vista.informacionDelUsuario(usuarioDTO);
        }
    }

    /**
     * Agrega un nuevo contacto a la agenda y a la vista.
     */
    public void agregarNuevoContacto() {
        Contacto nuevoContacto = null;

        nuevoContacto = vista.mostrarAgregarContacto();

        if(nuevoContacto != null) {
            try {
                agendaServicio.addContacto(nuevoContacto);
                vista.getModeloContactos().addElement(nuevoContacto);
                mostrarMensajeFlotante("Contacto agregado: " + nuevoContacto.getNombre(), Color.GREEN);

                // Si se agrega un nuevo contacto, establecer una clave para la conversación con él.
                try {
                    SecretKey nuevaClave = Cifrador.generarClaveAES();
                    clavesConversacion.put(nuevoContacto, nuevaClave);
                    System.out.println("DEBUG: Clave AES generada para nuevo contacto " + nuevoContacto.getNombre() + ": " + Cifrador.claveATexto(nuevaClave));
                } catch (NoSuchAlgorithmException e) {
                    mostrarMensajeFlotante("Error al generar clave para nuevo contacto: " + e.getMessage(), Color.RED);
                    e.printStackTrace();
                }

            } catch (ContactoRepetidoException e) {
                mostrarMensajeFlotante(e.getMessage(), Color.RED);
            }
        }
    }

    /**
     * Muestra un mensaje flotante en la interfaz de usuario.
     * @param texto el texto del mensaje
     * @param fondo el color de fondo del mensaje
     */
    private void mostrarMensajeFlotante(String texto, Color fondo) {
        JDialog mensaje = new JDialog((Frame) vista, false);
        mensaje.setUndecorated(true);
        mensaje.getContentPane().setBackground(fondo);

        JLabel label = new JLabel("<html><div style='text-align: center;'>" + texto + "</div></html>", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        mensaje.getContentPane().add(label);

        mensaje.pack();
        mensaje.setLocationRelativeTo((Component) vista);
        mensaje.setAlwaysOnTop(true);
        mensaje.setVisible(true);

        new Timer(2000, e -> mensaje.dispose()).start();
    }

    /**
     * Recibe un mensaje y lo agrega a la conversación correspondiente.
     * Este método es llamado directamente por la clase `Conexion`.
     * @param mensajeRecibido el mensaje recibido (el Mensaje que viene cifrado de la red)
     */
    public void recibirMensaje(Mensaje mensajeRecibido){
        Contacto emisorContacto = null;

        // Primero, intenta encontrar el contacto en la agenda existente
        // Es crucial que la instancia de Contacto sea la misma si ya existe.
        // Asumo que Contacto tiene un buen método equals/hashCode basado en el nombre.
        for (Contacto c : agendaServicio.getContactos()) {
            if (c.getNombre().equals(mensajeRecibido.getEmisor())) {
                emisorContacto = c;
                break;
            }
        }
        // Si no se encuentra en la agenda, crea un nuevo Contacto "temporal"
        // Y LO AÑADE A LAS CLAVES DE CONVERSACION CON UNA CLAVE GENERADA.
        if (emisorContacto == null) {
            emisorContacto = new Contacto(mensajeRecibido.getEmisor(), null, 0); // Solo el nombre
            // Add the new contact to the view's model if it's not already there
            if (!vista.getModeloContactos().contains(emisorContacto)) {
                vista.getModeloContactos().addElement(emisorContacto);
            }
            try {
                SecretKey nuevaClave = Cifrador.generarClaveAES();
                clavesConversacion.put(emisorContacto, nuevaClave); // NUEVO: Generar clave para nuevo contacto
                System.out.println("DEBUG: Clave AES generada para contacto recibido (nuevo): " + Cifrador.claveATexto(nuevaClave));
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Error al generar clave para contacto recibido: " + e.getMessage());
                // Podrías mostrar un mensaje de error al usuario o loguear
            }
        }

        SecretKey claveConversacion = clavesConversacion.get(emisorContacto);
        if (claveConversacion == null) {
            System.err.println("ERROR: No hay clave de cifrado para la conversación con " + mensajeRecibido.getEmisor() + ". Mensaje no descifrado.");
            mostrarMensajeFlotante("Mensaje cifrado de " + mensajeRecibido.getEmisor() + " no pudo ser descifrado (clave no disponible).", Color.RED);
            String contenidoVisible = "MENSAJE CIFRADO (sin clave)";
            // Create a new Mensaje object with the visible content for storage and display
            Mensaje messageForDisplay = new Mensaje(mensajeRecibido.getEmisor(), mensajeRecibido.getReceptor(), contenidoVisible, mensajeRecibido.getTimestamp());
            this.conversacionServicio.addMensajeEntrante(messageForDisplay);
            // También agregarlo a la vista para que el usuario vea el mensaje ilegible
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    messageForDisplay,
                    false,
                    sdf.format(java.sql.Timestamp.valueOf(mensajeRecibido.getTimestamp()))));
            return;
        }

        String contenidoDescifrado = null;
        try {
            contenidoDescifrado = Cifrador.descifrar(mensajeRecibido.getContenidoCifrado(), claveConversacion);
            System.out.println("DEBUG: Mensaje descifrado: " + contenidoDescifrado);
        } catch (Exception e) {
            System.err.println("Error al descifrar el mensaje: " + e.getMessage());
            e.printStackTrace();
            contenidoDescifrado = "ERROR AL DESCIFRAR";
        }

        Mensaje mensajeParaProcesar = new Mensaje(
                mensajeRecibido.getEmisor(),
                mensajeRecibido.getReceptor(),
                contenidoDescifrado,
                mensajeRecibido.getTimestamp() // Keep the original timestamp
        );

        String fechaFormateada = sdf.format(java.sql.Timestamp.valueOf(mensajeRecibido.getTimestamp()));

        ChatPantalla chatPantalla = new ChatPantalla(emisorContacto);

        int chatIndex = -1;
        for (int i = 0; i < vista.getModeloChats().size(); i++) {
            if (vista.getModeloChats().getElementAt(i).getContacto().equals(emisorContacto)) {
                chatPantalla = vista.getModeloChats().getElementAt(i);
                chatIndex = i;
                break;
            }
        }

        if (chatIndex == -1) { // If chat doesn't exist, add it
            this.conversacionServicio.agregarConversacion(emisorContacto); // Ensure conversation is added to model
            vista.getModeloChats().addElement(chatPantalla);
            // After adding, find its new index to select it
            chatIndex = vista.getModeloChats().size() - 1;
        }

        this.conversacionServicio.addMensajeEntrante(mensajeParaProcesar);

        // Update view if current chat is the one that received the message
        ChatPantalla currentSelectedChat = vista.getListaChats().getSelectedValue();
        if(currentSelectedChat != null && emisorContacto.equals(currentSelectedChat.getContacto())) {
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensajeParaProcesar,
                    false, // It's an incoming message
                    fechaFormateada));
        } else {
            // Visually notify that there's a new message in an unselected chat
            if (chatIndex >= 0) {
                chatPantalla.setPendiente(); // Mark as pending
                vista.getModeloChats().set(chatIndex, chatPantalla); // Update the element in the model
                vista.getListaChats().repaint(); // Force repaint to show notification
            }
        }
    }

    /**
     * Actualiza la vista con la información recibida (DirectorioDTO o excepciones).
     * Este método solo debería ser llamado por `Conexion` para información NO-MENSAJE.
     * Los mensajes son manejados por `recibirMensaje(Mensaje)`.
     * @param o el objeto observable (será null si es llamado por Conexion.update)
     * @param arg el argumento pasado al método notifyObservers
     */
    @Override
    public void update(Observable o, Object arg) {
        // The part 'arg instanceof Mensaje' is removed or commented here,
        // since `recibirMensaje(Mensaje)` is called directly by `Conexion`.
        if (arg instanceof DirectorioDTO) {
            DirectorioDTO contactos = (DirectorioDTO) arg;
            System.out.println("Contactos recibidos: " + contactos);
            contactos.getContactos().removeIf(c -> c.getNombre().equals(usuarioDTO.getNombre()));
            this.directorioDTO = contactos;
            vista.actualizarListaContactos(contactos.getContactos()); // Update contact view
        } else if (arg instanceof PerdioConexionException) {
            PerdioConexionException e = (PerdioConexionException) arg;
            System.err.println("Error de conexión notificado al Controlador: " + e.getMessage());
            mostrarMensajeFlotante("Error de conexión: " + e.getMessage(), Color.RED);
            // reconectar(); // You could trigger reconnection here if you want it to be automatic
        } else if (arg instanceof Exception) { // Capture other general exceptions
            Exception e = (Exception) arg;
            System.err.println("Excepción general notificada al Controlador: " + e.getMessage());
            mostrarMensajeFlotante("Error inesperado: " + e.getMessage(), Color.RED);
        }
    }

    public DirectorioDTO getDirectorioDTO() {
        return directorioDTO;
    }

    /**
     * Establece la vista de inicio de sesión.
     * @param vistaInicioSesion la vista de inicio de sesión
     */
    public void setVistaInicioSesion(IVistaInicioSesion vistaInicioSesion) {
        this.vistaInicioSesion = vistaInicioSesion;
    }

    /**
     * Establece la vista principal.
     * @param vista la vista principal
     */
    public void setVistaPrincipal(IVistaPrincipal vista) {
        this.vista = vista;
    }

    /**
     * Carga la conversación seleccionada en la vista.
     * @param selectedValue el chat seleccionado
     */
    public void cargarConversacion(ChatPantalla selectedValue) {
        int index = vista.getModeloChats().indexOf(selectedValue);

        if (index >= 0) {
            ChatPantalla chatConNotificacion = vista.getModeloChats().get(index);
            chatConNotificacion.setLeido();
            vista.getModeloChats().set(index, chatConNotificacion);
            ArrayList<Mensaje> mensajesCifrados = (ArrayList<Mensaje>) this.conversacionServicio.getMensajes(chatConNotificacion.getContacto());

            SecretKey claveConversacion = clavesConversacion.get(chatConNotificacion.getContacto());
            if (claveConversacion == null) {
                mostrarMensajeFlotante("ERROR: No hay clave para cargar conversación con " + chatConNotificacion.getContacto().getNombre(), Color.RED);
                System.err.println("No se puede cargar conversación: clave no disponible para " + chatConNotificacion.getContacto().getNombre());
                return;
            }

            vista.getPanelMensajes().removeAll(); // Clear message panel

            for(Mensaje mensajeCifrado : mensajesCifrados) {
                String contenidoDescifrado = null;
                boolean esMensajePropio = false;

                try {
                    // The sender of the encrypted message determines if it's ours or someone else's.
                    // The key was already obtained from the contact associated with the chat.
                    contenidoDescifrado = Cifrador.descifrar(mensajeCifrado.getContenidoCifrado(), claveConversacion);
                    esMensajePropio = mensajeCifrado.getEmisor().equals(usuarioDTO.getNombre());
                } catch (Exception e) {
                    System.err.println("Error al descifrar mensaje al cargar conversación: " + e.getMessage());
                    e.printStackTrace();
                    contenidoDescifrado = "ERROR AL DESCIFRAR";
                }

                String fechaFormateada = sdf.format(java.sql.Timestamp.valueOf(mensajeCifrado.getTimestamp()));

                vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                        new Mensaje(mensajeCifrado.getEmisor(), mensajeCifrado.getReceptor(), contenidoDescifrado),
                        esMensajePropio,
                        fechaFormateada));
            }

            vista.getPanelMensajes().revalidate();
            vista.getPanelMensajes().repaint();
        }
    }

    public ArrayList<Contacto> obtenerContactos() {
        ArrayList<Contacto> contactos = new ArrayList<>();
        try {
            contactos = this.conexion.obtenerContactos();
        } catch (PerdioConexionException e) {
            reconectar();
        }
        return contactos;
    }

    void reconectar(){
        try {
            this.conexion.reconectar();
        } catch (IOException e) {
            if(this.vista.mostrarDialogoReintentarConexion()){
                try {
                    this.conexion.reconectar();
                } catch (IOException ex) {
                    throw new RuntimeException(ex); // Re-throw exception in case of repeated failure
                }
            }else{
                this.vista.cerrarDialogoReconexion();
                System.exit(0);
            }
        }
    }

    public void cerrarMensajeConectando() {
        this.vista.cerrarDialogoReconexion();
    }

    public void abrirMensajeConectando() {
        this.vista.mostrarDialogoReconexion();
    }
}