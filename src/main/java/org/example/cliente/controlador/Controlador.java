package org.example.cliente.controlador;

import org.example.cliente.conexion.*;
import org.example.cliente.modelo.*;
import org.example.cliente.vista.*;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Usuario;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.util.cifrado.Cifrador; // ¡IMPORTANTE! Nueva importación para el cifrador
import org.example.servidor.DirectorioDTO;

import javax.crypto.SecretKey; // ¡IMPORTANTE! Nueva importación
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.NoSuchAlgorithmException; // ¡IMPORTANTE! Nueva importación
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap; // ¡IMPORTANTE! Nueva importación
import java.util.Map;     // ¡IMPORTANTE! Nueva importación
import java.util.Observable;
import java.util.Observer;

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
    private IConexion conexion;
    private Contacto usuarioDTO;
    private DirectorioDTO directorioDTO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private UsuarioServicio usuarioServicio;

    // --- NUEVO ---
    // Mapa para almacenar las claves secretas AES para cada conversación/contacto
    // En un sistema real, estas claves se establecerían de forma segura (ej. Diffie-Hellman)
    private Map<Contacto, SecretKey> clavesConversacion = new HashMap<>();
    // -------------

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
        // Cerrar la conexión y limpiar la vista
        if (conexion != null) {
            conexion.cerrarConexiones();
        }
        vista.ocultar();
        vista.limpiarCampos();
        // Esperar un tiempo para que el sistema libere el puerto
        try {
            Thread.sleep(1000); // Esperar 1 segundo
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        vistaInicioSesion.mostrar();
        conexion = null;
        // --- NUEVO ---
        clavesConversacion.clear(); // Limpiar claves al cerrar sesión
        // -------------
    }

    /**
     * Inicia un nuevo chat con el contacto seleccionado.
     */
    private void iniciarChat() {
        System.out.println("Inicio de chat");
        Contacto selectedValue = vista.getListaContactos().getSelectedValue();

        if (selectedValue != null) {
            ChatPantalla chatPantallaExistente = new ChatPantalla(selectedValue);
            if (!vista.getModeloChats().contains(chatPantallaExistente)) {
                System.out.println("inicio de chat " + selectedValue);
                this.conversacionServicio.agregarConversacion(selectedValue);

                // --- NUEVO ---
                // Generar/Obtener una clave para esta nueva conversación
                // En un sistema real, esta clave se establecería a través de un intercambio seguro.
                // Aquí, simplemente la generamos y la almacenamos.
                try {
                    SecretKey nuevaClave = Cifrador.generarClaveAES();
                    clavesConversacion.put(selectedValue, nuevaClave);
                    System.out.println("DEBUG: Clave AES generada para " + selectedValue.getNombre() + ": " + Cifrador.claveATexto(nuevaClave));
                } catch (NoSuchAlgorithmException e) {
                    mostrarMensajeFlotante("Error al generar clave de cifrado: " + e.getMessage(), Color.RED);
                    e.printStackTrace();
                    return; // No iniciar chat si no se puede generar clave
                }
                // -------------

                vista.getModeloChats().addElement(new ChatPantalla(selectedValue));
                vista.getListaChats().setSelectedValue(selectedValue, true);
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
        Contacto receptor = vista.getListaChats().getSelectedValue().getContacto();
        String contenidoTextoPlano = vista.getCampoMensaje().getText(); // Contenido en texto plano

        if (contenidoTextoPlano.trim().isEmpty()) {
            mostrarMensajeFlotante("El mensaje no puede estar vacío.", Color.ORANGE);
            return;
        }

        // --- NUEVO ---
        SecretKey claveConversacion = clavesConversacion.get(receptor);
        if (claveConversacion == null) {
            mostrarMensajeFlotante("ERROR: No hay clave de cifrado para la conversación con " + receptor.getNombre(), Color.RED);
            System.err.println("No se encontró clave para " + receptor.getNombre() + ". Mensaje no enviado.");
            return;
        }

        String contenidoCifrado = null;
        try {
            contenidoCifrado = Cifrador.cifrar(contenidoTextoPlano, claveConversacion);
            System.out.println("DEBUG: Mensaje cifrado: " + contenidoCifrado); // Para depuración
        } catch (Exception e) {
            mostrarMensajeFlotante("Error al cifrar el mensaje: " + e.getMessage(), Color.RED);
            e.printStackTrace();
            return;
        }
        // -------------

        // Usamos el constructor de Mensaje que acepta emisor, receptor y contenidoCifrado
        // Ajustamos el constructor de Mensaje si fuera necesario
        Mensaje mensaje = new Mensaje(this.usuarioDTO.getNombre(), receptor.getNombre(), contenidoCifrado);


        try {
            conexion.enviarMensaje(receptor, mensaje); // Envía el objeto Mensaje CON EL CONTENIDO CIFRADO
            // También almacenamos el mensaje cifrado en el servicio de conversación
            this.conversacionServicio.addMensajeSaliente(receptor, mensaje);
            vista.getCampoMensaje().setText("");

            // Agregar el mensaje descifrado (para el emisor, que lo ve en su propia pantalla)
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    // Para mostrar el mensaje enviado por el propio usuario, lo desciframos localmente
                    new Mensaje(this.usuarioDTO.getNombre(), receptor.getNombre(), contenidoTextoPlano), // Creamos un Mensaje con texto plano para mostrarlo localmente
                    true,
                    sdf.format(mensaje.getTimestamp()))); // Usamos el timestamp original del mensaje cifrado
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

            Usuario usuario = new Usuario(nombre, "127.0.0.1", puerto); // Ajusta constructor de Usuario si es necesario
            this.usuarioServicio = new UsuarioServicio(usuario);
            this.agendaServicio = new AgendaServicio(usuario);
            this.conversacionServicio = new ConversacionServicio(usuario);
            this.conexion = new Conexion();

            this.usuarioDTO = new Contacto(usuario);

            conexion.conectarServidor(usuarioDTO);
            new Thread(conexion).start();
            vista.mostrar();
            vista.titulo("Usuario: " + nombre + " | Ip: "+ "127.0.0.1" + " | Puerto: " + puerto);
            vista.informacionDelUsuario(usuarioDTO);

            // --- NUEVO ---
            // Para el propio usuario, inicializa su clave de conversación consigo mismo (aunque no se use directamente para P2P)
            // Esto es más bien una "clave maestra" o una simulación para la persistencia local de sus propios mensajes
            try {
                SecretKey selfKey = Cifrador.generarClaveAES();
                clavesConversacion.put(usuarioDTO, selfKey); // Clave para el propio usuario
                System.out.println("DEBUG: Clave AES generada para propio usuario: " + Cifrador.claveATexto(selfKey));
            } catch (NoSuchAlgorithmException e) {
                mostrarMensajeFlotante("Error al generar clave para el propio usuario: " + e.getMessage(), Color.RED);
                e.printStackTrace();
            }
            // -------------

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

                // --- NUEVO ---
                // Si se agrega un nuevo contacto, se debería establecer una clave para la conversación con él.
                // En un sistema real, esto iniciaría un protocolo de intercambio de claves.
                // Aquí, simplemente generamos una clave nueva.
                try {
                    SecretKey nuevaClave = Cifrador.generarClaveAES();
                    clavesConversacion.put(nuevoContacto, nuevaClave);
                    System.out.println("DEBUG: Clave AES generada para nuevo contacto " + nuevoContacto.getNombre() + ": " + Cifrador.claveATexto(nuevaClave));
                } catch (NoSuchAlgorithmException e) {
                    mostrarMensajeFlotante("Error al generar clave para nuevo contacto: " + e.getMessage(), Color.RED);
                    e.printStackTrace();
                }
                // -------------

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
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // padding interno
        mensaje.getContentPane().add(label);

        mensaje.pack(); // ajusta automaticamente al contenido
        mensaje.setLocationRelativeTo((Component) vista);
        mensaje.setAlwaysOnTop(true);
        mensaje.setVisible(true);

        new Timer(2000, e -> mensaje.dispose()).start();
    }

    /**
     * Recibe un mensaje y lo agrega a la conversación correspondiente.
     * @param mensajeRecibido el mensaje recibido (el Mensaje que viene cifrado de la red)
     */
    public void recibirMensaje(Mensaje mensajeRecibido){
        // --- NUEVO ---
        // Necesitamos la clave para descifrar el mensaje
        Contacto emisorContacto = null;
        // Primero, busca el contacto en la agenda del usuario logueado usando el nombre del emisor
        // Esto es necesario para obtener la instancia correcta de Contacto que usas como clave en 'clavesConversacion'

        for (Contacto c : agendaServicio.getUsuario().getContactos()) { // asumiendo que AgendaServicio.getUsuario() te da el Usuario actual
            if (c.getNombre().equals(mensajeRecibido.getEmisor())) {
                emisorContacto = c;
                break;
            }
        }
        // Si no lo encuentra en la agenda, puede que sea un contacto nuevo o un mensaje de un desconocido
        // Para este ejemplo, si no lo encuentra, lo crea como Contacto simple para intentar obtener la clave
        if (emisorContacto == null) {
            // Esto es un parche. En un sistema real, deberías manejar mejor contactos no registrados.
            emisorContacto = new Contacto(mensajeRecibido.getEmisor(), null, 0); // Solo el nombre para la clave
        }


        SecretKey claveConversacion = clavesConversacion.get(emisorContacto);
        if (claveConversacion == null) {
          System.err.println("ERROR: No hay clave de cifrado para la conversación con " + mensajeRecibido.getEmisor() + ". Mensaje no descifrado.");
            mostrarMensajeFlotante("Mensaje cifrado de " + mensajeRecibido.getEmisor() + " no pudo ser descifrado (clave no disponible).", Color.RED);
            // Mostrar el mensaje cifrado o un placeholder
            String contenidoVisible = "MENSAJE CIFRADO (sin clave)";
            this.conversacionServicio.addMensajeEntrante(new Mensaje(mensajeRecibido.getEmisor(), mensajeRecibido.getReceptor(), contenidoVisible));
            // Actualizar vista con el placeholder
            // ... (lógica para agregar el placeholder a la vista)
            return;
        }

        String contenidoDescifrado = null;
        try {
            contenidoDescifrado = Cifrador.descifrar(mensajeRecibido.getContenidoCifrado(), claveConversacion);
            System.out.println("DEBUG: Mensaje descifrado: " + contenidoDescifrado); // Para depuración
        } catch (Exception e) {
            System.err.println("Error al descifrar el mensaje: " + e.getMessage());
            e.printStackTrace();
            contenidoDescifrado = "ERROR AL DESCIFRAR"; // O manejar de otra forma
        }

        // Crear un objeto Mensaje "lógico" para el servicio de conversación y la vista
        // con el contenido descifrado.

        Mensaje mensajeParaProcesar = new Mensaje(
                mensajeRecibido.getEmisor(),
                mensajeRecibido.getReceptor(),
                contenidoDescifrado // Contenido ya descifrado
        );

        this.conversacionServicio.addMensajeEntrante(mensajeParaProcesar);
        String fechaFormateada = sdf.format(java.sql.Timestamp.valueOf(mensajeRecibido.getTimestamp())); // Convertir LocalDateTime a Date para SimpleDateFormat

        //Creo un chat para el mensaje si no existe opero
        ChatPantalla chatPantalla = new ChatPantalla(emisorContacto); // Usar el Contacto encontrado/creado

        if(!vista.getModeloChats().contains(chatPantalla)){
            if(!vista.getModeloContactos().contains(chatPantalla.getContacto())){
                vista.getModeloContactos().addElement(emisorContacto); // Agregar el Contacto real, no el nombre
            }else {
                vista.getModeloChats().addElement(chatPantalla);
                vista.getListaChats().setSelectedValue(chatPantalla, true);
            }
        }

        this.conversacionServicio.addMensajeEntrante(mensajeParaProcesar);

        if(vista.getListaChats().getSelectedValue() != null
                && emisorContacto.equals(vista.getListaChats().getSelectedValue().getContacto())) { // Comparar con Contacto, no solo nombre
            //agregar el mensaje a la vista
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensajeParaProcesar, // Usar el mensaje con el contenido descifrado
                    false,
                    fechaFormateada));

        }else{
            // Notificar visualmente que hay un nuevo mensaje
            int index = vista.getModeloChats().indexOf(chatPantalla);
            if (index >= 0) {
                ChatPantalla chatConNotificacion = vista.getModeloChats().get(index);
                chatConNotificacion.setPendiente();
                vista.getModeloChats().set(index, chatConNotificacion);
            }
        }
    }

    /**
     * Actualiza la vista con el nuevo mensaje recibido.
     * @param o el objeto observable
     * @param arg el argumento pasado al método notifyObservers
     */
    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof Mensaje) {
            Mensaje mensaje = (Mensaje) arg;
            System.out.println("Mensaje recibido en update: " + mensaje); // Muestra el mensaje cifrado
            recibirMensaje(mensaje);
            // Aquí puedes manejar el mensaje de texto recibido
        } else if (arg instanceof Contacto) {
            Contacto contacto = (Contacto) arg;
            System.out.println("Contacto recibido en update: " + contacto);
            // Aquí puedes manejar el contacto recibido
        } else if (arg instanceof DirectorioDTO) {
            DirectorioDTO contactos = (DirectorioDTO) arg;
            System.out.println("Contactos recibidos en update: " + contactos);
            contactos.getContactos().removeIf(c -> c.getNombre().equals(usuarioDTO.getNombre()));
            this.directorioDTO = contactos;

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

            // --- NUEVO ---
            // Recuperar la clave para este contacto
            SecretKey claveConversacion = clavesConversacion.get(chatConNotificacion.getContacto());
            if (claveConversacion == null) {
                mostrarMensajeFlotante("ERROR: No hay clave para cargar conversación con " + chatConNotificacion.getContacto().getNombre(), Color.RED);
                System.err.println("No se puede cargar conversación: clave no disponible para " + chatConNotificacion.getContacto().getNombre());
                return; // No se pueden cargar mensajes si no hay clave
            }
            // -------------

            // Limpiar el panel de mensajes
            vista.getPanelMensajes().removeAll();

            for(Mensaje mensajeCifrado : mensajesCifrados) {
                String contenidoDescifrado = null;
                boolean esMensajePropio = false;

                // --- NUEVO ---
                try {
                    // Decide si el mensaje fue enviado por el propio usuario o recibido
                    // Esto es crucial para saber qué instancia de Contacto usar para buscar la clave
                    // y también para el 'true/false' en MensajePantalla.
                    if (mensajeCifrado.getEmisor().equals(usuarioDTO.getNombre())) { // Si el emisor es el propio usuario logueado
                        contenidoDescifrado = Cifrador.descifrar(mensajeCifrado.getContenidoCifrado(), claveConversacion); // Usar clave del receptor
                        esMensajePropio = true;
                    } else { // Si el emisor es el otro contacto
                        // Asegúrate de que el Contacto del emisor en el mapa sea la misma instancia que la del mensaje
                        // o al menos que su equals/hashCode funcione correctamente.
                        // Para simplificar, si ya estamos en una conversación cargada, asumimos la clave asociada a 'selectedValue'
                        contenidoDescifrado = Cifrador.descifrar(mensajeCifrado.getContenidoCifrado(), claveConversacion);
                        esMensajePropio = false;
                    }
                } catch (Exception e) {
                    System.err.println("Error al descifrar mensaje al cargar conversación: " + e.getMessage());
                    e.printStackTrace();
                    contenidoDescifrado = "ERROR AL DESCIFRAR"; // O manejar de otra forma
                }
                // -------------

                String fechaFormateada = sdf.format(java.sql.Timestamp.valueOf(mensajeCifrado.getTimestamp())); // Convertir LocalDateTime a Date

                // Agregar el mensaje a la vista con el contenido descifrado
                vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                        new Mensaje(mensajeCifrado.getEmisor(), mensajeCifrado.getReceptor(), contenidoDescifrado),
                        esMensajePropio, // mensaje.getEmisor().equals(usuarioDTO), // Ahora es 'esMensajePropio'
                        fechaFormateada));
            }

            // Actualizar la vista
            vista.getPanelMensajes().revalidate();
            vista.getPanelMensajes().repaint();
        }
    }


    public ArrayList<Contacto> obtenerContactos() {
        ArrayList<Contacto> contactos = new ArrayList<>();
        try {
            contactos = this.conexion.obtenerContactos();
        } catch (PerdioConexionException e) {
            //intentar reconectar
            reconectar();
        }
        return contactos;
    }

    void reconectar(){
        try {
            this.conexion.reconectar();
            new Thread(conexion).start();
        } catch (IOException e) {
            if(this.vista.mostrarDialogoReintentarConexion()){
                try {
                    this.conexion.reconectar();
                    new Thread(conexion).start();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
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