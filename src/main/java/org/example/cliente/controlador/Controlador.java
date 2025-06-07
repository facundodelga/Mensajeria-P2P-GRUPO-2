package org.example.cliente.controlador;

import org.example.cliente.conexion.*;
import org.example.cliente.modelo.*;
import org.example.cliente.vista.*;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Usuario;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.util.cifrado.Cifrador; // Asegúrate de que esta ruta es correcta
import org.example.servidor.DirectorioDTO;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime; // Necesario para trabajar con Mensaje.getTimestamp()
import java.time.format.DateTimeFormatter; // Opcional, si quieres usarlo en lugar de SimpleDateFormat para LocalDateTime
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private Conexion conexion; // Tipo concreto Conexion, ya que es quien tendrá setControlador
    private Contacto usuarioDTO; // Representa al usuario actual, tipo Contacto para consistencia
    private DirectorioDTO directorioDTO;

    // Usaremos SimpleDateFormat para Date/Timestamp para compatibilidad con java.sql.Timestamp
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    // Si Mensaje.getTimestamp() devuelve LocalDateTime, el formato correcto es:
    // private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
                // Este botón debería ser para refrescar la lista de contactos del servidor
                // pero ya se hace automáticamente al iniciar sesión o se puede hacer por eventos de red.
                // Si realmente quieres un botón manual, asegúrate de que el servidor lo soporte.
                // Por ahora, simplemente llamaremos a obtenerContactos, que solo envía la petición.
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
        // Un pequeño retraso para asegurar que la vista se oculte antes de mostrar la de inicio de sesión
        try {
            Thread.sleep(500); // Reducido a 0.5 segundos, 1 segundo puede ser mucho.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            e.printStackTrace();
        }

        vistaInicioSesion.mostrar();
        conexion = null;
        clavesConversacion.clear();
        // Limpiar también los modelos de la vista
        vista.getModeloContactos().clear();
        vista.getModeloChats().clear();
        vista.getPanelMensajes().removeAll(); // Limpiar el panel de mensajes
        vista.getPanelMensajes().revalidate();
        vista.getPanelMensajes().repaint();
    }

    /**
     * Inicia un nuevo chat con el contacto seleccionado.
     */
    private void iniciarChat() {
        System.out.println("Inicio de chat");
        Contacto selectedValue = vista.getListaContactos().getSelectedValue();

        if (selectedValue != null) {
            // Se usa el Contacto directamente para buscar en el modelo de chats
            ChatPantalla chatPantallaExistente = new ChatPantalla(selectedValue);

            // Verifica si el chat ya está en la lista de chats
            boolean chatYaExiste = false;
            for (int i = 0; i < vista.getModeloChats().size(); i++) {
                if (vista.getModeloChats().getElementAt(i).getContacto().equals(selectedValue)) {
                    chatYaExiste = true;
                    break;
                }
            }

            if (!chatYaExiste) {
                System.out.println("Iniciando chat con " + selectedValue.getNombre());
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

                vista.getModeloChats().addElement(chatPantallaExistente); // Añadir el nuevo chat
                vista.getListaChats().setSelectedValue(chatPantallaExistente, true); // Seleccionar el nuevo chat
                vista.getPanelMensajes().revalidate();
                vista.getPanelMensajes().repaint();
            } else {
                mostrarMensajeFlotante("El contacto " + selectedValue.getNombre() + " ya tiene un chat iniciado", Color.ORANGE);
                // Si el chat ya existe, simplemente lo seleccionamos
                for (int i = 0; i < vista.getModeloChats().size(); i++) {
                    if (vista.getModeloChats().getElementAt(i).getContacto().equals(selectedValue)) {
                        vista.getListaChats().setSelectedValue(vista.getModeloChats().getElementAt(i), true);
                        break;
                    }
                }
            }
        } else {
            mostrarMensajeFlotante("Seleccione un contacto de la lista para iniciar un chat.", Color.ORANGE);
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

        // 1. Mensaje que se envía por la red (DEBE IR CIFRADO)
        Mensaje mensajeParaEnviar = new Mensaje(this.usuarioDTO.getNombre(), receptor.getNombre(), contenidoCifrado);

        try {
            conexion.enviarMensaje(receptor, mensajeParaEnviar);

            // 2. Mensaje que se guarda en el servicio de conversación (con contenido PLAIN)
            Mensaje mensajeParaGuardar = new Mensaje(
                    this.usuarioDTO.getNombre(),
                    receptor.getNombre(),
                    contenidoTextoPlano, // Aquí el contenido plano
                    mensajeParaEnviar.getTimestamp() // Usamos el timestamp original
            );
            this.conversacionServicio.addMensajeSaliente(receptor, mensajeParaGuardar);

            vista.getCampoMensaje().setText("");

            // 3. Mensaje que se muestra en la burbuja de la vista
            // Directamente pasamos el contenidoTextoPlano y el timestamp original
            vista.addMensajeBurbuja(new MensajePantalla(
                    contenidoTextoPlano, // Contenido ya en texto plano
                    true, // Es un mensaje propio
                    sdf.format(java.sql.Timestamp.valueOf(mensajeParaEnviar.getTimestamp())) // Formatear el timestamp original
            ));
        } catch (PerdioConexionException e){
            System.err.println("PerdioConexionException al enviar mensaje: " + e.getMessage());
            mostrarMensajeFlotante("Error de conexión: " + e.getMessage() + ". Intentando reconectar...", Color.RED);
            reconectar();
        } catch (EnviarMensajeException | IOException e) {
            mostrarMensajeFlotante("Error al enviar mensaje: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Inicia el servidor (establece la conexión con el servidor remoto) con los datos proporcionados por la vista de inicio de sesión.
     */
    public void iniciarServidor() {
        vistaInicioSesion.mostrar();
        String nombre = vistaInicioSesion.getNombre();
        if(nombre.isEmpty()){
            mostrarMensajeFlotante("El nombre de usuario no puede estar vacío", Color.RED);
            return;
        }
        try {
            int puerto = Integer.parseInt(vistaInicioSesion.getPuerto());
            // Validar puerto
            if (puerto < 0 || puerto > 65535) {
                throw new NumberFormatException("Puerto fuera de rango (0-65535).");
            }
            vistaInicioSesion.ocultar();

            Usuario usuario = new Usuario(nombre, "127.0.0.1", puerto); // IP local para el usuario en el modelo
            this.usuarioServicio = new UsuarioServicio(usuario);
            this.agendaServicio = new AgendaServicio(usuario);
            this.conversacionServicio = new ConversacionServicio(usuario);
            this.conexion = new Conexion(); // Instanciar la clase concreta Conexion

            this.usuarioDTO = new Contacto(usuario.getNombre(), usuario.getIp(), usuario.getPuerto()); // Crear Contacto del propio usuario

            conexion.setControlador(this); // Pasar la instancia de Controlador a Conexion
            conexion.conectarServidor(usuarioDTO); // Intenta conectar
            new Thread(conexion).start(); // Inicia el hilo de Conexion (que a su vez iniciará ManejadorEntradas)

            vista.mostrar();
            vista.titulo("Usuario: " + nombre + " | Ip: "+ usuario.getIp() + " | Puerto: " + usuario.getPuerto());
            vista.informacionDelUsuario(usuarioDTO);

            // Para el propio usuario, inicializa su clave de conversación consigo mismo
            // Esto es crucial para que el propio usuario pueda descifrar mensajes que se envía a sí mismo
            // (si el servidor los reenvía o si se maneja lógica de mensajes a uno mismo).
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
            vistaInicioSesion.mostrar(); // Vuelve a mostrar la vista de inicio de sesión
        }catch (PuertoEnUsoException e){
            // Esta excepción ahora significa que el nickname ya está en uso en el servidor
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
            vistaInicioSesion.mostrar(); // Vuelve a mostrar la vista de inicio de sesión
        } catch (IOException | PerdioConexionException e) {
            System.err.println("Error inicial de conexión en iniciarServidor: " + e.getMessage());
            mostrarMensajeFlotante("Error de conexión inicial: " + e.getMessage() + ". Intentando reconectar...", Color.RED);
            reconectar(); // Intenta reconectar si la conexión inicial falla
            // Si la reconexión es exitosa, la vista ya estará visible.
            // Si falla, reconectar() manejará la salida o el diálogo.
            vista.mostrar(); // Asegurarse de que la vista principal se muestre incluso si reconectar falla.
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
                // Asegúrate de que el contacto no se añada duplicado en la vista
                if (!vista.getModeloContactos().contains(nuevoContacto)) {
                    vista.getModeloContactos().addElement(nuevoContacto);
                }
                mostrarMensajeFlotante("Contacto agregado: " + nuevoContacto.getNombre(), Color.GREEN);

                // Si se agrega un nuevo contacto, establecer una clave para la conversación con él.
                // Esto se hace una vez por contacto, ya sea que se agregue manualmente o se reciba un mensaje de él.
                if (!clavesConversacion.containsKey(nuevoContacto)) {
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
     * Muestra un mensaje flotante en la interfaz de usuario.
     * @param texto el texto del mensaje
     * @param fondo el color de fondo del mensaje
     */
    private void mostrarMensajeFlotante(String texto, Color fondo) {
        // Asegúrate de que este método se ejecuta en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
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

            new Timer(3000, e -> mensaje.dispose()).start(); // Aumentado a 3 segundos para mejor legibilidad
        });
    }

    /**
     * Recibe un mensaje y lo agrega a la conversación correspondiente.
     * Este método es llamado directamente por la clase `ManejadorEntradas`.
     * @param mensajeRecibido el mensaje recibido (el Mensaje que viene cifrado de la red)
     */
    public void recibirMensaje(Mensaje mensajeRecibido){
        SwingUtilities.invokeLater(() -> { // Asegura que las actualizaciones de la UI se hagan en el EDT
            Contacto emisorContacto = null;

            // Primero, intenta encontrar el contacto en la agenda existente del usuario actual
            // CORRECCIÓN: Acceder a los contactos a través de agendaServicio
            // Asumo que agendaServicio.getContactos() devuelve la lista de contactos gestionados.
            if (agendaServicio != null) { // Add null check for robustness
                for (Contacto c : agendaServicio.getContactos()) { // CORRECTED LINE
                    if (c.getNombre().equals(mensajeRecibido.getEmisor())) {
                        emisorContacto = c;
                        break;
                    }
                }
            }

            // Si no se encuentra en la agenda, crea un nuevo Contacto "temporal"
            // y lo añade a las claves de conversación con una clave generada.
            if (emisorContacto == null) {
                // Se crea un nuevo Contacto solo con el nombre, ya que IP y puerto no son conocidos aún
                emisorContacto = new Contacto(mensajeRecibido.getEmisor(), null, 0);
                // Además de agregar la clave, si este contacto no está en la agenda, se debería añadir a la vista
                // y posiblemente a la agenda del usuario para futuras referencias.
                if (!vista.getModeloContactos().contains(emisorContacto)) {
                    vista.getModeloContactos().addElement(emisorContacto);
                    // Opcional: agendaServicio.addContacto(emisorContacto); // Si quieres que se persista este nuevo contacto automáticamente
                }
                if (!clavesConversacion.containsKey(emisorContacto)) {
                    try {
                        SecretKey nuevaClave = Cifrador.generarClaveAES();
                        clavesConversacion.put(emisorContacto, nuevaClave);
                        System.out.println("DEBUG: Clave AES generada para contacto recibido (nuevo): " + Cifrador.claveATexto(nuevaClave));
                    } catch (NoSuchAlgorithmException e) {
                        System.err.println("Error al generar clave para contacto recibido (nuevo): " + e.getMessage());
                        // Podrías mostrar un mensaje de error al usuario o loguear
                    }
                }
            }

            SecretKey claveConversacion = clavesConversacion.get(emisorContacto);
            String contenidoDescifrado = null;

            if (claveConversacion == null) {
                System.err.println("ERROR: No hay clave de cifrado para la conversación con " + mensajeRecibido.getEmisor() + ". Mensaje no descifrado.");
                mostrarMensajeFlotante("Mensaje cifrado de " + mensajeRecibido.getEmisor() + " no pudo ser descifrado (clave no disponible).", Color.RED);
                contenidoDescifrado = "MENSAJE CIFRADO (sin clave)";
            } else {
                try {
                    contenidoDescifrado = Cifrador.descifrar(mensajeRecibido.getContenidoCifrado(), claveConversacion);
                    System.out.println("DEBUG: Mensaje descifrado: " + contenidoDescifrado);
                } catch (Exception e) {
                    System.err.println("Error al descifrar el mensaje: " + e.getMessage());
                    e.printStackTrace();
                    contenidoDescifrado = "ERROR AL DESCIFRAR"; // Placeholder si falla el descifrado
                }
            }

            // Crear un Mensaje con el contenido DESCIFRADO para el servicio de conversación y la vista.
            // Este mensaje ya lleva el contenido en texto plano.
            Mensaje mensajeParaProcesar = new Mensaje(
                    mensajeRecibido.getEmisor(),
                    mensajeRecibido.getReceptor(),
                    contenidoDescifrado, // Aquí el contenido plano (o placeholder)
                    mensajeRecibido.getTimestamp() // Mantener el timestamp original
            );

            String fechaFormateada = sdf.format(java.sql.Timestamp.valueOf(mensajeRecibido.getTimestamp()));

            // Asegurarse de que el chat exista o crearlo
            ChatPantalla chatPantallaParaMensaje = new ChatPantalla(emisorContacto);
            int chatIndex = -1;
            for (int i = 0; i < vista.getModeloChats().size(); i++) {
                if (vista.getModeloChats().getElementAt(i).getContacto().equals(emisorContacto)) {
                    chatPantallaParaMensaje = vista.getModeloChats().getElementAt(i);
                    chatIndex = i;
                    break;
                }
            }

            if (chatIndex == -1) { // Si el chat no existe, agregarlo
                this.conversacionServicio.agregarConversacion(emisorContacto); // Asegúrate de que la conversación se agregue al modelo
                vista.getModeloChats().addElement(chatPantallaParaMensaje);
                chatIndex = vista.getModeloChats().size() - 1; // Obtener el índice del nuevo elemento
            }


            this.conversacionServicio.addMensajeEntrante(mensajeParaProcesar);

            // Actualizar la vista de mensajes si el chat actual es el que recibió el mensaje
            ChatPantalla currentSelectedChat = vista.getListaChats().getSelectedValue();
            if(currentSelectedChat != null && emisorContacto.equals(currentSelectedChat.getContacto())) {
                vista.addMensajeBurbuja(new MensajePantalla(
                        contenidoDescifrado, // Contenido ya descifrado
                        false, // Es un mensaje entrante
                        fechaFormateada));
            } else {
                // Notificar visualmente que hay un nuevo mensaje en un chat no seleccionado
                if (chatIndex >= 0) {
                    chatPantallaParaMensaje.setPendiente(); // Marcar como pendiente
                    vista.getModeloChats().set(chatIndex, chatPantallaParaMensaje); // Actualizar el elemento en el modelo
                    vista.getListaChats().repaint(); // Forzar repintado para mostrar notificación
                }
            }
        });
    }

    /**
     * Actualiza la vista con la información recibida (DirectorioDTO o excepciones).
     * Este método solo debería ser llamado por `Conexion` (o `ManejadorEntradas`) para información NO-MENSAJE.
     * Los mensajes son manejados por `recibirMensaje(Mensaje)`.
     * @param o el objeto observable (será null si es llamado por Conexion.update o ManejadorEntradas.update)
     * @param arg el argumento pasado al método notifyObservers
     */
    @Override
    public void update(Observable o, Object arg) {
        SwingUtilities.invokeLater(() -> { // Asegura que las actualizaciones de la UI se hagan en el EDT
            if (arg instanceof DirectorioDTO) {
                DirectorioDTO contactos = (DirectorioDTO) arg;
                System.out.println("Contactos recibidos en update: " + contactos);
                // Remover el propio usuario de la lista de contactos para mostrar
                contactos.getContactos().removeIf(c -> c.getNombre().equals(usuarioDTO.getNombre()));
                this.directorioDTO = contactos;
                vista.actualizarListaContactos(contactos.getContactos()); // Actualizar la vista de contactos
            } else if (arg instanceof PerdioConexionException) {
                PerdioConexionException e = (PerdioConexionException) arg;
                System.err.println("Error de conexión notificado al Controlador (update): " + e.getMessage());
                mostrarMensajeFlotante("Error de conexión: " + e.getMessage() + ". Intentando reconectar...", Color.RED);
                reconectar(); // Disparar la reconexión
            } else if (arg instanceof Exception) { // Captura otras excepciones generales
                Exception e = (Exception) arg;
                System.err.println("Excepción general notificada al Controlador (update): " + e.getMessage());
                mostrarMensajeFlotante("Error inesperado: " + e.getMessage(), Color.RED);
            } else if (arg instanceof String) {
                String stringMessage = (String) arg;
                System.out.println("Mensaje de texto del servidor: " + stringMessage);
                mostrarMensajeFlotante("Notificación del servidor: " + stringMessage, Color.BLUE);
            }
            // No procesar Mensaje aquí, ya se maneja en recibirMensaje(Mensaje)
        });
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
        SwingUtilities.invokeLater(() -> { // Asegura que las actualizaciones de la UI se hagan en el EDT
            int index = vista.getModeloChats().indexOf(selectedValue);

            if (index >= 0) {
                ChatPantalla chatConNotificacion = vista.getModeloChats().getElementAt(index);
                chatConNotificacion.setLeido(); // Marcar como leído
                vista.getModeloChats().set(index, chatConNotificacion); // Actualizar el elemento en el modelo para repintar

                Contacto contactoDelChat = chatConNotificacion.getContacto();
                ArrayList<Mensaje> mensajesGuardados = (ArrayList<Mensaje>) this.conversacionServicio.getMensajes(contactoDelChat);

                SecretKey claveConversacion = clavesConversacion.get(contactoDelChat);
                if (claveConversacion == null) {
                    mostrarMensajeFlotante("ERROR: No hay clave para cargar conversación con " + contactoDelChat.getNombre(), Color.RED);
                    System.err.println("No se puede cargar conversación: clave no disponible para " + contactoDelChat.getNombre());
                    return;
                }

                vista.getPanelMensajes().removeAll(); // Limpiar el panel de mensajes actual

                for(Mensaje mensaje : mensajesGuardados) {
                    String contenidoParaMostrar = null;
                    boolean esMensajePropio = mensaje.getEmisor().equals(usuarioDTO.getNombre());

                    // Asumimos que conversacionServicio almacena mensajes con `contenidoPlano`
                    if (mensaje.getContenidoPlano() != null) {
                        contenidoParaMostrar = mensaje.getContenidoPlano();
                    } else if (mensaje.getContenidoCifrado() != null) {
                        // Fallback: si por alguna razón no tiene contenido plano, intentar descifrar
                        try {
                            contenidoParaMostrar = Cifrador.descifrar(mensaje.getContenidoCifrado(), claveConversacion);
                        } catch (Exception e) {
                            System.err.println("Error al descifrar mensaje (al cargar la conversación): " + e.getMessage());
                            e.printStackTrace();
                            contenidoParaMostrar = "ERROR AL DESCIFRAR";
                        }
                    } else {
                        contenidoParaMostrar = "CONTENIDO NO DISPONIBLE";
                    }

                    String fechaFormateada = sdf.format(java.sql.Timestamp.valueOf(mensaje.getTimestamp()));

                    vista.addMensajeBurbuja(new MensajePantalla(
                            contenidoParaMostrar,
                            esMensajePropio,
                            fechaFormateada));
                }

                vista.getPanelMensajes().revalidate();
                vista.getPanelMensajes().repaint();
            }
        });
    }

    public ArrayList<Contacto> obtenerContactos() {
        ArrayList<Contacto> contactos = new ArrayList<>();
        try {
            // Este método solo envía la petición al servidor. La respuesta (DirectorioDTO)
            // será recibida por ManejadorEntradas y notificada al Controlador.update.
            conexion.obtenerContactos();
            System.out.println("Solicitud de contactos enviada al servidor.");
            // No se devuelve directamente aquí, la vista se actualizará via update.
            return null; // O una lista vacía, ya que el resultado es asíncrono.
        } catch (PerdioConexionException e) {
            System.err.println("PerdioConexionException al obtener contactos: " + e.getMessage());
            mostrarMensajeFlotante("Error de conexión: " + e.getMessage() + ". Intentando reconectar...", Color.RED);
            reconectar();
            return null; // O una lista vacía
        }
    }

    /**
     * Intenta reconectar al servidor principal o de respaldo.
     * Muestra diálogos al usuario durante el proceso.
     */
    void reconectar(){
        SwingUtilities.invokeLater(() -> { // Asegura que las actualizaciones de la UI se hagan en el EDT
            try {
                this.conexion.reconectar();
                // Si la reconexión es exitosa, el diálogo se cierra automáticamente
                // y se puede hacer un refresh de la vista si es necesario (ej. obtener contactos pendientes)
                if (this.conexion != null && this.conexion.getSocket() != null && this.conexion.getSocket().isConnected()) {
                    mostrarMensajeFlotante("Reconexión exitosa.", Color.GREEN);
                    // Opcional: Solicitar mensajes pendientes y contactos después de reconectar
                    this.conexion.obtenerMensajesPendientes();
                    this.conexion.obtenerContactos();
                }

            } catch (IOException | PerdioConexionException e) {
                System.err.println("Fallo la reconexión después de varios intentos: " + e.getMessage());
                // Si la reconexión falla, preguntarle al usuario si desea reintentar
                if(this.vista.mostrarDialogoReintentarConexion()){
                    reconectar(); // Recursivamente intentar de nuevo
                } else {
                    this.vista.cerrarDialogoReconexion(); // Cierra el diálogo de conexión
                    mostrarMensajeFlotante("No se pudo establecer la conexión. Saliendo de la aplicación.", Color.RED);
                    System.exit(0); // Salir de la aplicación si el usuario no quiere reintentar
                }
            }
        });
    }

    public void cerrarMensajeConectando() {
        SwingUtilities.invokeLater(() -> {
            this.vista.cerrarDialogoReconexion();
        });
    }

    public void abrirMensajeConectando() {
        SwingUtilities.invokeLater(() -> {
            this.vista.mostrarDialogoReconexion();
        });
    }
}