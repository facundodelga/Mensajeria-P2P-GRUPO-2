package org.example.cliente.controlador;

import org.example.cliente.conexion.*;
import org.example.cliente.modelo.*;
import org.example.cliente.vista.*;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Usuario;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.util.cifrado.Cifrador; // Asumo que esta clase y sus métodos existen
import org.example.servidor.DirectorioDTO; // Asumo que esta clase existe

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime; // Clase para manejar fecha y hora moderna
import java.time.format.DateTimeFormatter; // Clase para formatear LocalDateTime
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; // Usamos List en lugar de ArrayList para mayor flexibilidad
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional; // Para manejo seguro de búsqueda de contactos

/**
 * Clase Controlador que implementa ActionListener y Observer.
 * Maneja la lógica de la aplicación y la interacción entre la vista y el modelo.
 */
public class Controlador implements ActionListener, Observer {
    private static Controlador instancia = null;
    private IVistaPrincipal vista;
    private IVistaInicioSesion vistaInicioSesion;

    // Servicios del modelo
    private IAgenda agendaServicio; // Representa el servicio para gestionar contactos (AgendaServicio)
    private IConversacion conversacionServicio; // Representa el servicio para gestionar conversaciones (ConversacionServicio)
    private UsuarioServicio usuarioServicio; // Servicio para gestionar al Usuario (incluye getUsuario())

    private Conexion conexion; // Objeto para manejar la conexión de red
    private Contacto usuarioDTO; // Representa el propio usuario como un Contacto
    private DirectorioDTO directorioDTO; // El directorio de contactos recibido del servidor

    // Formateador para las marcas de tiempo de los mensajes (para la UI)
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Mapa para almacenar las claves secretas AES para cada conversación/contacto
    private Map<Contacto, SecretKey> clavesConversacion = new HashMap<>();

    /**
     * Constructor privado para el patrón Singleton.
     */
    private Controlador() {
        directorioDTO = new DirectorioDTO(); // Inicializa el directorio (vacío al principio)
    }

    /**
     * Obtiene la instancia única de la clase Controlador (patrón Singleton).
     * @return La única instancia de Controlador.
     */
    public static Controlador getInstancia() {
        if (instancia == null) {
            instancia = new Controlador();
        }
        return instancia;
    }

    /**
     * Maneja los eventos de acción de la interfaz de usuario.
     * @param e El evento de acción disparado.
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
                obtenerContactos(); // Obtener y actualizar la lista de contactos del servidor
                break;
            default:
                System.out.println("DEBUG: Acción no reconocida: " + e.getActionCommand());
                break;
        }
    }

    /**
     * Cierra la sesión del usuario, cierra conexiones y resetea el estado.
     */
    private void cerrarSesion() {
        if (conexion != null) {
            conexion.cerrarConexiones();
        }
        vista.ocultar();
        vista.limpiarCampos(); // Limpiar campos de la vista principal
        try {
            // Dar un pequeño tiempo para que el cierre de conexiones se propague.
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            System.err.println("Cierre de sesión interrumpido: " + e.getMessage());
        }

        // Resetear todos los atributos a su estado inicial
        vistaInicioSesion.mostrar(); // Vuelve a mostrar la vista de inicio de sesión
        conexion = null;
        clavesConversacion.clear();
        usuarioServicio = null;
        agendaServicio = null;
        conversacionServicio = null;
        usuarioDTO = null;
        directorioDTO = new DirectorioDTO();
        System.out.println("Sesión cerrada y estado reseteado.");
    }

    /**
     * Inicia un nuevo chat con el contacto seleccionado en la lista de contactos.
     * Genera o recupera una clave AES para la conversación.
     */
    private void iniciarChat() {
        System.out.println("DEBUG: Intentando iniciar chat...");
        Contacto selectedValue = vista.getListaContactos().getSelectedValue();

        if (selectedValue == null) {
            mostrarMensajeFlotante("Seleccione un contacto para iniciar chat.", Color.ORANGE);
            return;
        }

        // IMPORTANTE: Asegurarse de usar la misma instancia de Contacto en todo el controlador
        // para las claves y conversaciones, especialmente si Contacto.equals/hashCode se basan en referencias.
        // Aquí buscamos el Contacto en la agenda real del usuario para obtener la instancia gestionada.
        Optional<Contacto> contactoEnAgendaOptional = usuarioServicio.getUsuario().getContactos().stream()
                .filter(c -> c.equals(selectedValue))
                .findFirst();
        // Si por alguna razón el contacto seleccionado no está en la agenda (lo cual no debería pasar si fue agregado),
        // usamos el valor seleccionado.
        Contacto contactoParaChat = contactoEnAgendaOptional.orElse(selectedValue);


        ChatPantalla chatPantallaExistente = new ChatPantalla(contactoParaChat);
        // Verificar si el chat ya está abierto en la vista
        if (vista.getModeloChats().contains(chatPantallaExistente)) {
            mostrarMensajeFlotante("El chat con " + contactoParaChat.getNombre() + " ya está iniciado.", Color.ORANGE);
            vista.getListaChats().setSelectedValue(chatPantallaExistente, true); // Lo selecciona si ya está abierto
            cargarConversacion(chatPantallaExistente); // Cargar mensajes existentes para este chat
            return;
        }

        System.out.println("DEBUG: Iniciando chat con " + contactoParaChat.getNombre());
        // Agrega la conversación al modelo (si no existe, ConversacionServicio la creará)
        this.conversacionServicio.agregarConversacion(contactoParaChat);

        // Generar o recuperar una clave AES para esta conversación si no existe
        if (!clavesConversacion.containsKey(contactoParaChat)) {
            try {
                SecretKey nuevaClave = Cifrador.generarClaveAES();
                clavesConversacion.put(contactoParaChat, nuevaClave); // Asocia la clave con la instancia del Contacto
                System.out.println("DEBUG: Clave AES generada para " + contactoParaChat.getNombre() + ": " + Cifrador.claveATexto(nuevaClave));
            } catch (NoSuchAlgorithmException e) {
                mostrarMensajeFlotante("Error al generar clave de cifrado: " + e.getMessage(), Color.RED);
                e.printStackTrace();
                return; // No continuar si la clave no se pudo generar
            }
        }

        // Agrega el chat a la lista visible en la interfaz
        vista.getModeloChats().addElement(chatPantallaExistente);
        vista.getListaChats().setSelectedValue(chatPantallaExistente, true); // Selecciona el chat recién creado
        // Asegurarse de que el panel de mensajes se actualice
        vista.getPanelMensajes().revalidate();
        vista.getPanelMensajes().repaint();
        cargarConversacion(chatPantallaExistente); // Cargar mensajes existentes para este chat
    }

    /**
     * Envía un mensaje al contacto seleccionado en el chat activo.
     * Cifra el mensaje antes de enviarlo por la red.
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
            System.err.println("ERROR: No se encontró clave para " + receptor.getNombre() + ". Mensaje no enviado.");
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

        // 1. Crear el Mensaje con el contenido CIFRADO para enviar por la red
        Mensaje mensajeParaEnviar = new Mensaje(this.usuarioDTO.getNombre(), receptor.getNombre(), contenidoCifrado, LocalDateTime.now());

        try {
            // 2. Enviar el mensaje cifrado a través de la conexión
            conexion.enviarMensaje(receptor, mensajeParaEnviar);
            // 3. Guardar el mensaje (cifrado) en el historial de conversaciones del usuario local
            this.conversacionServicio.addMensajeSaliente(receptor, mensajeParaEnviar);

            vista.getCampoMensaje().setText(""); // Limpiar el campo de texto después de enviar

            // 4. Crear un Mensaje nuevo (o una copia) con el contenido en TEXTO PLANO
            //    para mostrarlo en la burbuja de chat de la propia UI.
            Mensaje mensajeParaVistaPropio = new Mensaje(this.usuarioDTO.getNombre(), receptor.getNombre(), contenidoTextoPlano, mensajeParaEnviar.getTimestamp());
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensajeParaVistaPropio, // Usa el mensaje con el texto plano
                    true, // Es un mensaje propio
                    mensajeParaVistaPropio.getTimestamp().format(dtf))); // Formatea la fecha con DateTimeFormatter
        } catch (PerdioConexionException e){
            mostrarMensajeFlotante("Conexión perdida. Intentando reconectar...", Color.RED);
            reconectar();
        } catch (EnviarMensajeException | IOException e) {
            mostrarMensajeFlotante("Error al enviar mensaje: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Inicia el servidor de conexión con los datos proporcionados por la vista de inicio de sesión.
     */
    public void iniciarServidor() {
        vistaInicioSesion.mostrar(); // Asegurarse de que la vista de inicio de sesión esté visible al inicio
        String nombre = vistaInicioSesion.getNombre();
        if(nombre.trim().isEmpty()){
            mostrarMensajeFlotante("El nombre de usuario no puede estar vacío.", Color.RED);
            return;
        }
        try {
            int puerto = Integer.parseInt(vistaInicioSesion.getPuerto());
            if (puerto < 0 || puerto > 65535) {
                throw new NumberFormatException(); // Lanzar para que el catch lo capture
            }

            vistaInicioSesion.ocultar(); // Ocultar la vista de inicio de sesión

            // 1. Crear el objeto Usuario para el usuario actual
            Usuario usuarioActual = new Usuario(nombre, "127.0.0.1", puerto);

            // 2. Inicializar los servicios con la instancia de Usuario
            this.usuarioServicio = new UsuarioServicio(usuarioActual); // Servicio que gestiona al Usuario
            this.agendaServicio = new AgendaServicio(usuarioActual); // Servicio para la agenda del usuario
            this.conversacionServicio = new ConversacionServicio(usuarioActual); // Servicio para las conversaciones del usuario

            this.conexion = new Conexion(); // Instanciar la clase concreta Conexion
            this.usuarioDTO = new Contacto(usuarioActual.getNombre(),usuarioActual.getIp(),usuarioActual.getPuerto()); // El propio usuario como un Contacto

            // 3. Conectar al servidor
            conexion.conectarServidor(usuarioDTO);
            conexion.setControlador(this); // Pasar la instancia de Controlador a Conexion
            new Thread(conexion).start(); // Inicia el hilo de Conexion (que a su vez iniciará ManejadorEntradas)

            // 4. Actualizar la vista principal
            vista.mostrar();
            vista.titulo("Usuario: " + nombre + " | Ip: " + "127.0.0.1" + " | Puerto: " + puerto);
            vista.informacionDelUsuario(usuarioDTO);

            // 5. Generar una clave AES para la "conversación" del propio usuario consigo mismo
            // Esto es útil si tienes alguna funcionalidad de auto-mensajes o para asegurar
            // que siempre hay una clave base asociada a la identidad del usuario.
            try {
                SecretKey selfKey = Cifrador.generarClaveAES();
                clavesConversacion.put(usuarioDTO, selfKey);
                System.out.println("DEBUG: Clave AES generada para propio usuario: " + Cifrador.claveATexto(selfKey));
            } catch (NoSuchAlgorithmException e) {
                mostrarMensajeFlotante("Error al generar clave para el propio usuario: " + e.getMessage(), Color.RED);
                e.printStackTrace();
            }

        }catch (NumberFormatException e) {
            mostrarMensajeFlotante("El puerto debe ser un número entero entre 0 y 65535.", Color.RED);
            vistaInicioSesion.mostrar(); // Vuelve a mostrar la vista de inicio para corregir
        }catch (PuertoEnUsoException e){
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
            vistaInicioSesion.mostrar(); // Vuelve a mostrar la vista de inicio para corregir
        } catch (IOException e) {
            mostrarMensajeFlotante("Error de conexión inicial al servidor: " + e.getMessage(), Color.RED);
            e.printStackTrace();
            reconectar(); // Intenta reconectar si la conexión inicial falla
            // Si la reconexión es exitosa, la vista ya estará visible.
            // Si no, el método reconectar() puede manejar el cierre de la app.
        }
    }

    /**
     * Agrega un nuevo contacto a la agenda del usuario y a la vista de contactos.
     * Genera una clave AES para la futura conversación con este contacto.
     */
    public void agregarNuevoContacto() {
        Contacto nuevoContacto = vista.mostrarAgregarContacto(); // Obtiene el contacto de la UI

        if(nuevoContacto != null) {
            try {
                // Agregar el contacto al modelo de agenda del usuario
                agendaServicio.addContacto(nuevoContacto);
                // Agregar el contacto a la lista visible en la interfaz
                vista.getModeloContactos().addElement(nuevoContacto);
                mostrarMensajeFlotante("Contacto agregado: " + nuevoContacto.getNombre(), Color.GREEN);

                // Generar una clave AES para la futura conversación con este nuevo contacto
                if (!clavesConversacion.containsKey(nuevoContacto)) { // Solo si no tiene una clave ya
                    try {
                        SecretKey nuevaClave = Cifrador.generarClaveAES();
                        clavesConversacion.put(nuevoContacto, nuevaClave);
                        System.out.println("DEBUG: Clave AES generada para nuevo contacto " + nuevoContacto.getNombre() + ": " + Cifrador.claveATexto(nuevaClave));
                    } catch (NoSuchAlgorithmException e) {
                        mostrarMensajeFlotante("Error al generar clave para nuevo contacto: " + e.getMessage(), Color.RED);
                        e.printStackTrace();
                    }
                }
            } catch (ContactoRepetidoException e) {
                mostrarMensajeFlotante(e.getMessage(), Color.RED);
            }
        }
    }

    /**
     * Muestra un mensaje flotante temporal en la interfaz de usuario.
     * @param texto El texto del mensaje a mostrar.
     * @param fondo El color de fondo del mensaje.
     */
    private void mostrarMensajeFlotante(String texto, Color fondo) {
        JDialog mensaje = new JDialog((Frame) vista, false);
        mensaje.setUndecorated(true); // Sin bordes ni barra de título
        mensaje.getContentPane().setBackground(fondo);

        JLabel label = new JLabel("<html><div style='text-align: center;'>" + texto + "</div></html>", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Relleno interno
        mensaje.getContentPane().add(label);

        mensaje.pack(); // Ajustar tamaño al contenido
        mensaje.setLocationRelativeTo((Component) vista); // Centrar respecto a la vista principal
        mensaje.setAlwaysOnTop(true); // Siempre visible encima de otras ventanas
        mensaje.setVisible(true);

        // Temporizador para cerrar el mensaje automáticamente después de 2 segundos
        new Timer(2000, e -> mensaje.dispose()).start();
    }

    /**
     * Recibe un mensaje de la red, lo descifra, lo agrega a la conversación
     * y actualiza la interfaz de usuario.
     * Este método es llamado directamente por la clase `Conexion` (NO por `update`).
     * @param mensajeRecibido El mensaje recibido (contiene el contenido cifrado).
     */
    public void recibirMensaje(Mensaje mensajeRecibido){
        System.out.println("DEBUG: Mensaje recibido en Controlador: " + mensajeRecibido.getEmisor() + " -> " + mensajeRecibido.getReceptor());

        // 1. Encontrar el Contacto correspondiente al emisor en la agenda del usuario.
        // Es crucial usar la misma instancia de Contacto que está en el mapa de claves y conversaciones.
        // Asumo que Contacto.equals() y Contacto.hashCode() están bien implementados (basado en nombre, IP, Puerto).
        Optional<Contacto> emisorContactoOptional = usuarioServicio.getUsuario().getContactos().stream()
                .filter(c -> c.getNombre().equals(mensajeRecibido.getEmisor()))
                .findFirst();
        Contacto emisorContacto;

        // Si el emisor no es un contacto conocido en la agenda, crea un Contacto "temporal".
        if (emisorContactoOptional.isEmpty()) {
            // Nota: Aquí se crea un Contacto con IP y puerto nulos/0. Esto podría necesitar ser mejorado
            // si más adelante necesitas la IP/Puerto del emisor para alguna funcionalidad.
            emisorContacto = new Contacto(mensajeRecibido.getEmisor(), "0.0.0.0", 0); // IP y puerto dummy
            System.out.println("DEBUG: Emisor " + mensajeRecibido.getEmisor() + " no encontrado en contactos. Creando contacto temporal.");
            // Generar una clave para este nuevo contacto no conocido si no tiene una
            if (!clavesConversacion.containsKey(emisorContacto)) {
                try {
                    SecretKey nuevaClave = Cifrador.generarClaveAES();
                    clavesConversacion.put(emisorContacto, nuevaClave);
                    System.out.println("DEBUG: Clave AES generada para contacto recibido (nuevo): " + Cifrador.claveATexto(nuevaClave));
                } catch (NoSuchAlgorithmException e) {
                    System.err.println("Error al generar clave para contacto recibido temporal: " + e.getMessage());
                    mostrarMensajeFlotante("Error interno al procesar mensaje de un nuevo emisor.", Color.RED);
                }
            }
        } else {
            emisorContacto = emisorContactoOptional.get();
        }

        // 2. Obtener la clave de cifrado para el emisor
        SecretKey claveConversacion = clavesConversacion.get(emisorContacto);
        if (claveConversacion == null) {
            System.err.println("ERROR: No hay clave de cifrado para la conversación con " + mensajeRecibido.getEmisor() + ". Mensaje no descifrado.");
            mostrarMensajeFlotante("Mensaje cifrado de " + mensajeRecibido.getEmisor() + " no pudo ser descifrado (clave no disponible).", Color.RED);
            String contenidoVisible = "MENSAJE CIFRADO (sin clave)"; // Texto por defecto para la UI

            // Agregamos el mensaje "ilegible" al historial de conversaciones y a la vista
            Mensaje mensajeParaGuardarYMostrar = new Mensaje(mensajeRecibido.getEmisor(), mensajeRecibido.getReceptor(), contenidoVisible, mensajeRecibido.getTimestamp());
            this.conversacionServicio.addMensajeEntrante(mensajeParaGuardarYMostrar);
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensajeParaGuardarYMostrar,
                    false, // Es un mensaje ajeno
                    mensajeParaGuardarYMostrar.getTimestamp().format(dtf))); // Formatear con DateTimeFormatter
            return;
        }

        // 3. Descifrar el contenido del mensaje
        String contenidoDescifrado = null;
        try {
            contenidoDescifrado = Cifrador.descifrar(mensajeRecibido.getContenidoCifrado(), claveConversacion);
            System.out.println("DEBUG: Mensaje descifrado: " + contenidoDescifrado);
            mensajeRecibido.setContenido(contenidoDescifrado); // Almacenar el contenido descifrado en el Mensaje original
        } catch (Exception e) {
            System.err.println("ERROR: Fallo al descifrar el mensaje de " + mensajeRecibido.getEmisor() + ": " + e.getMessage());
            e.printStackTrace();
            contenidoDescifrado = "ERROR AL DESCIFRAR";
            mensajeRecibido.setContenido(contenidoDescifrado); // Almacenar el error en el Mensaje
        }

        // 4. Agregar el mensaje (con contenido descifrado/error) al historial de conversaciones del usuario
        this.conversacionServicio.addMensajeEntrante(mensajeRecibido);

        // 5. Encontrar o crear la ChatPantalla correspondiente en la UI
        ChatPantalla chatPantalla = new ChatPantalla(emisorContacto);
        int chatIndex = vista.getModeloChats().indexOf(chatPantalla);

        if(chatIndex == -1){ // Si el chat no existe en la lista de chats de la vista
            // Si el contacto es nuevo (no estaba en la agenda), añadirlo a la lista de contactos de la UI
            if(!vista.getModeloContactos().contains(emisorContacto)){
                vista.getModeloContactos().addElement(emisorContacto);
            }
            vista.getModeloChats().addElement(chatPantalla); // Agrega el nuevo chat a la vista
            vista.getListaChats().setSelectedValue(chatPantalla, true); // Seleccionar el chat recién añadido
        }

        // 6. Actualizar la vista de mensajes (burbujas de chat)
        // Comprobar si el chat actualmente visible es el que recibió el mensaje.
        if(vista.getListaChats().getSelectedValue() != null
                && emisorContacto.equals(vista.getListaChats().getSelectedValue().getContacto())) {
            // Si es el chat activo, añadir el mensaje directamente a la vista
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensajeRecibido, // Mensaje con contenido descifrado/error
                    false, // Es un mensaje ajeno
                    mensajeRecibido.getTimestamp().format(dtf))); // Formatear con DateTimeFormatter
            this.conversacionServicio.setConversacionPendiente(emisorContacto); // Marcar como NO pendiente si está abierto
        } else {
            // Si no es el chat activo, marcarlo como pendiente para notificar al usuario
            int index = vista.getModeloChats().indexOf(chatPantalla);
            if (index >= 0) {
                ChatPantalla chatConNotificacion = vista.getModeloChats().get(index);
                chatConNotificacion.setPendiente(); // Marcar como pendiente
                vista.getModeloChats().set(index, chatConNotificacion); // Actualizar el elemento en el modelo
                vista.getListaChats().repaint(); // Forzar repintado para mostrar la notificación visual
            }
        }
    }

    /**
     * Este método es el observador general que recibe notificaciones de la clase `Conexion`.
     * Solo se usa para información no-mensaje (ej: DirectorioDTO, errores de conexión).
     * Los mensajes de chat son manejados por `recibirMensaje(Mensaje)` directamente.
     * @param o El objeto observable (normalmente `Conexion`).
     * @param arg El argumento pasado al método `notifyObservers`.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof DirectorioDTO) {
            DirectorioDTO contactos = (DirectorioDTO) arg;
            System.out.println("DEBUG: DirectorioDTO recibido en update: " + contactos.getContactos().size() + " contactos.");
            // Remueve el propio usuario de la lista de contactos para no chatear consigo mismo en la UI
            contactos.getContactos().removeIf(c -> c.equals(usuarioDTO)); // Usa equals para comparar Contactos
            this.directorioDTO = contactos;
            vista.actualizarListaContactos(contactos.getContactos()); // Actualizar la vista de contactos
        } else if (arg instanceof PerdioConexionException) {
            PerdioConexionException e = (PerdioConexionException) arg;
            System.err.println("ERROR: Conexión perdida notificada al Controlador: " + e.getMessage());
            mostrarMensajeFlotante("Conexión perdida: " + e.getMessage(), Color.RED);
            // Podrías iniciar un intento de reconexión automática aquí o esperar la acción del usuario
        } else if (arg instanceof Exception) { // Captura otras excepciones generales
            Exception e = (Exception) arg;
            System.err.println("ERROR: Excepción general notificada al Controlador: " + e.getMessage());
            mostrarMensajeFlotante("Error inesperado: " + e.getMessage(), Color.RED);
            e.printStackTrace(); // Imprime el stack trace para depuración
        }
    }

    /**
     * Obtiene el DTO del directorio de contactos actual.
     * @return El objeto DirectorioDTO.
     */
    public DirectorioDTO getDirectorioDTO() {
        return directorioDTO;
    }

    /**
     * Establece la vista de inicio de sesión para el controlador.
     * @param vistaInicioSesion La implementación de IVistaInicioSesion.
     */
    public void setVistaInicioSesion(IVistaInicioSesion vistaInicioSesion) {
        this.vistaInicioSesion = vistaInicioSesion;
    }

    /**
     * Establece la vista principal para el controlador.
     * @param vista La implementación de IVistaPrincipal.
     */
    public void setVistaPrincipal(IVistaPrincipal vista) {
        this.vista = vista;
    }

    /**
     * Carga los mensajes de la conversación seleccionada en el panel de mensajes de la vista.
     * Descifra los mensajes guardados antes de mostrarlos.
     * @param selectedValue El objeto ChatPantalla seleccionado (que contiene el Contacto).
     */
    public void cargarConversacion(ChatPantalla selectedValue) {
        if (selectedValue == null) {
            vista.getPanelMensajes().removeAll(); // Limpiar el panel si no hay chat seleccionado
            vista.getPanelMensajes().revalidate();
            vista.getPanelMensajes().repaint();
            return;
        }

        // Obtener la instancia de Contacto gestionada por el modelo
        Optional<Contacto> contactoEnAgendaOptional = usuarioServicio.getUsuario().getContactos().stream()
                .filter(c -> c.equals(selectedValue.getContacto()))
                .findFirst();
        Contacto contactoParaCargar = contactoEnAgendaOptional.orElse(selectedValue.getContacto());

        int index = vista.getModeloChats().indexOf(new ChatPantalla(contactoParaCargar));

        if (index >= 0) {
            ChatPantalla chatEnModelo = vista.getModeloChats().get(index);
            chatEnModelo.setLeido(); // Marcar el chat como leído en el modelo de la vista
            vista.getModeloChats().set(index, chatEnModelo); // Actualizar el elemento en el DefaultListModel

            // Obtener la clave de cifrado para este contacto
            SecretKey claveConversacion = clavesConversacion.get(contactoParaCargar);
            if (claveConversacion == null) {
                mostrarMensajeFlotante("ERROR: No hay clave para cargar conversación con " + contactoParaCargar.getNombre(), Color.RED);
                System.err.println("No se puede cargar conversación: clave no disponible para " + contactoParaCargar.getNombre());
                vista.getPanelMensajes().removeAll(); // Limpiar el panel
                vista.getPanelMensajes().revalidate();
                vista.getPanelMensajes().repaint();
                return;
            }

            // Limpiar el panel de mensajes antes de cargar los nuevos
            vista.getPanelMensajes().removeAll();

            // Obtener los mensajes del servicio de conversación
            // La lista devuelta puede ser List<Mensaje> o ArrayList<Mensaje>
            List<Mensaje> mensajesGuardados = this.conversacionServicio.getMensajes(contactoParaCargar);

            for(Mensaje mensajeGuardado : mensajesGuardados) {
                String contenidoDescifrado = null;
                boolean esMensajePropio = mensajeGuardado.getEmisor().equals(usuarioDTO.getNombre()); // Determinar si es propio

                try {
                    // Si el mensaje fue guardado con contenido plano (ej. al enviar o ya descifrado al recibir),
                    // usa ese contenido. De lo contrario, descifra el contenido cifrado.
                    if (mensajeGuardado.getContenido() != null && !mensajeGuardado.getContenido().trim().isEmpty()) {
                        contenidoDescifrado = mensajeGuardado.getContenido();
                    } else if (mensajeGuardado.getContenidoCifrado() != null && !mensajeGuardado.getContenidoCifrado().trim().isEmpty()) {
                        contenidoDescifrado = Cifrador.descifrar(mensajeGuardado.getContenidoCifrado(), claveConversacion);
                    } else {
                        contenidoDescifrado = "MENSAJE VACÍO/ILEGIBLE"; // Caso de mensaje sin contenido ni cifrado válido
                    }
                } catch (Exception e) {
                    System.err.println("Error al descifrar mensaje al cargar conversación: " + e.getMessage());
                    e.printStackTrace();
                    contenidoDescifrado = "ERROR AL DESCIFRAR";
                }

                // Formatear el timestamp usando DateTimeFormatter
                String fechaFormateada = mensajeGuardado.getTimestamp().format(dtf);

                // Añadir el mensaje a la burbuja de la vista
                vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                        new Mensaje(mensajeGuardado.getEmisor(), mensajeGuardado.getReceptor(), contenidoDescifrado, mensajeGuardado.getTimestamp()), // Crear un nuevo Mensaje para la vista con contenido plano y timestamp original
                        esMensajePropio,
                        fechaFormateada));
            }

            vista.getPanelMensajes().revalidate();
            vista.getPanelMensajes().repaint();
        } else {
            System.err.println("ERROR: Chat seleccionado para cargar no encontrado en el modelo de chats de la vista. Esto no debería ocurrir.");
        }
    }

    /**
     * Solicita la lista de contactos al servidor y la actualiza en la vista.
     * @return Una lista de Contacto obtenida del servidor.
     */
    public List<Contacto> obtenerContactos() {
        List<Contacto> contactosObtenidos = new ArrayList<>();
        try {
            this.conexion.obtenerContactos();
            // El método 'update' del controlador ya debería manejar la actualización de la vista
            // una vez que el DirectorioDTO es recibido por la conexión.
            // Esta llamada es principalmente para disparar la solicitud.
        } catch (PerdioConexionException e) {
            mostrarMensajeFlotante("Conexión perdida al obtener contactos. Intentando reconectar.", Color.RED);
            reconectar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return contactosObtenidos;
    }

    /**
     * Intenta reconectar con el servidor si la conexión se ha perdido.
     * Muestra diálogos al usuario para reintentar o salir.
     */
    void reconectar(){
        abrirMensajeConectando(); // Muestra el mensaje "Conectando..."
        try {
            this.conexion.reconectar();
            cerrarMensajeConectando(); // Si la reconexión es exitosa, cierra el mensaje
            mostrarMensajeFlotante("Reconexión exitosa.", Color.GREEN);
        } catch (IOException e) {
            System.err.println("Fallo al reconectar: " + e.getMessage());
            e.printStackTrace();
            cerrarMensajeConectando(); // Cierra el mensaje de "Conectando..."
            mostrarMensajeFlotante("Fallo al reconectar. " + e.getMessage(), Color.RED);

            // Pregunta al usuario si quiere reintentar
            if(this.vista.mostrarDialogoReintentarConexion()){
                reconectar(); // Llamada recursiva para reintentar
            }else{
                this.vista.cerrarDialogoReconexion(); // Asegurarse de que el diálogo esté cerrado
                System.exit(0); // Cierra la aplicación si el usuario no quiere reintentar
            }
        }
    }

    /**
     * Cierra el diálogo de "Conectando..." en la interfaz de usuario.
     */
    public void cerrarMensajeConectando() {
        if (this.vista != null) {
            this.vista.cerrarDialogoReconexion();
        }
    }

    /**
     * Abre el diálogo de "Conectando..." en la interfaz de usuario.
     */
    public void abrirMensajeConectando() {
        if (this.vista != null) {
            this.vista.mostrarDialogoReconexion();
        }
    }
}