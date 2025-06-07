// src/main/java/org/example/cliente/controlador/Controlador.java
package org.example.cliente.controlador;

import org.example.cliente.conexion.*;
import org.example.cliente.modelo.*;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.vista.*;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Usuario;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO;
import org.example.util.cifrado.ClaveUtil;
import org.example.util.Cifrador;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Clase Controlador que implementa ActionListener y Observer.
 * Maneja la lógica de la aplicación y la interacción entre la vista y el modelo.
 */
public class Controlador extends Observable implements ActionListener, Observer {
    private static Controlador instancia = null;
    private IVistaPrincipal vista;
    private IVistaInicioSesion vistaInicioSesion;

    private IAgenda agendaServicio;
    private IConversacion conversacionServicio;
    private IConexion conexion; // La instancia de Conexion
    private Contacto usuarioDTO;
    private DirectorioDTO directorioDTO;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private UsuarioServicio usuarioServicio;

    private KeyPair miParClavesDH;

    private Controlador() {
        directorioDTO = new DirectorioDTO();
        try {
            this.miParClavesDH = ClaveUtil.generarParClavesDH();
        } catch (Exception e) {
            System.err.println("Error al generar el par de claves Diffie-Hellman del controlador: " + e.getMessage());
            // Considerar System.exit(1) aquí si la generación de claves es crítica para la aplicación.
        }
    }

    public static Controlador getInstancia() {
        if (instancia == null) {
            instancia = new Controlador();
        }
        return instancia;
    }

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
                cargarContactosIniciales();
                break;
        }
    }

    private void cerrarSesion() {
        if (conexion != null) {
            conexion.cerrarConexiones();
            conexion = null;
        }
        vista.ocultar();
        vista.limpiarCampos();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        vistaInicioSesion.mostrar();
    }

    private void iniciarChat() {
        System.out.println("Inicio de chat");
        Contacto selectedValue = vista.getListaContactos().getSelectedValue();

        if (selectedValue != null) {
            ChatPantalla chatPantallaExistente = new ChatPantalla(selectedValue);
            if (vista.getModeloChats().contains(chatPantallaExistente)) {
                vista.getListaChats().setSelectedValue(chatPantallaExistente, true);
                mostrarMensajeFlotante("El chat con " + selectedValue.getNombre() + " ya está iniciado.", Color.ORANGE);
                return;
            }

            System.out.println("Iniciando chat con " + selectedValue);
            Conversacion conversacionActual = conversacionServicio.getConversacion(selectedValue);
            if (conversacionActual == null) {
                this.conversacionServicio.agregarConversacion(selectedValue);
                conversacionActual = conversacionServicio.getConversacion(selectedValue);
            }

            // MODIFICACIÓN CLAVE DH: Solo iniciar el intercambio si la clave AES no está establecida
            // Y SI NO HEMOS ENVIADO YA NUESTRA CLAVE PÚBLICA para esta conversación.
            if (conversacionActual != null && conversacionActual.getClaveSecretaAes() == null && !conversacionActual.isMyPublicKeySent()) {
                try {
                    mostrarMensajeFlotante("Iniciando intercambio de claves con " + selectedValue.getNombre() + "...", Color.BLUE);
                    conexion.iniciarIntercambioDeClaves(selectedValue);
                    conversacionActual.setMyPublicKeySent(true); // Marcar que ya enviamos nuestra clave
                } catch (Exception e) {
                    mostrarMensajeFlotante("Error al iniciar intercambio de claves: " + e.getMessage(), Color.RED);
                    e.printStackTrace();
                    return;
                }
            } else if (conversacionActual != null && conversacionActual.getClaveSecretaAes() != null) {
                mostrarMensajeFlotante("Clave secreta ya establecida con " + selectedValue.getNombre(), Color.GREEN);
            } else if (conversacionActual != null && conversacionActual.isMyPublicKeySent()) {
                mostrarMensajeFlotante("Clave pública ya enviada a " + selectedValue.getNombre() + ". Esperando su respuesta.", Color.ORANGE);
            }


            vista.getModeloChats().addElement(chatPantallaExistente);
            vista.getListaChats().setSelectedValue(chatPantallaExistente, true);
            vista.getPanelMensajes().revalidate();
            vista.getPanelMensajes().repaint();

        } else {
            mostrarMensajeFlotante("Seleccione un contacto", Color.RED);
        }
    }

    private void enviarMensaje() {
        ChatPantalla chatSeleccionado = vista.getListaChats().getSelectedValue();
        if (chatSeleccionado == null) {
            mostrarMensajeFlotante("Seleccione un chat.", Color.RED);
            return;
        }

        Contacto receptor = chatSeleccionado.getContacto();
        String contenidoTextoPlano = vista.getCampoMensaje().getText();

        if (receptor == null || contenidoTextoPlano.isEmpty()) {
            mostrarMensajeFlotante("Escriba un mensaje.", Color.RED);
            return;
        }

        Mensaje mensaje = new Mensaje(contenidoTextoPlano, this.usuarioDTO, receptor);

        try {
            conexion.enviarMensaje(receptor, mensaje);
            this.conversacionServicio.addMensajeSaliente(receptor, mensaje);
            vista.getCampoMensaje().setText("");

            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensaje,
                    true,
                    sdf.format(mensaje.getFecha())));
        } catch (PerdioConexionException e) {
            reconectar();
        } catch (EnviarMensajeException | IOException e) {
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    public void iniciarServidor() {
        vistaInicioSesion.mostrar();
        String nombre = vistaInicioSesion.getNombre();
        if (nombre.isEmpty()) {
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

            if (this.conexion != null) {
                this.conexion.cerrarConexiones();
            }
            this.conexion = new Conexion();
            this.conexion.addObserver(this);
            this.conexion.setMiParClavesDH(this.miParClavesDH);

            this.usuarioDTO = new Contacto(usuario);

            conexion.conectarServidor(usuarioDTO);

            new Thread(conexion).start();
            vista.mostrar();
            vista.titulo("Usuario: " + nombre + " | Ip: " + "127.0.0.1" + " | Puerto: " + puerto);
            vista.informacionDelUsuario(usuarioDTO);

            cargarContactosIniciales();

        } catch (NumberFormatException e) {
            mostrarMensajeFlotante("El puerto debe ser un número entre 0 y 65535", Color.RED);
        } catch (PuertoEnUsoException e) {
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
            vistaInicioSesion.mostrar();
        } catch (IOException | PerdioConexionException e) {
            mostrarMensajeFlotante("Error al conectar inicialmente: " + e.getMessage(), Color.RED);
            e.printStackTrace();
            vistaInicioSesion.mostrar();
        }
    }

    public void agregarNuevoContacto() {
        Contacto nuevoContacto = null;
        nuevoContacto = vista.mostrarAgregarContacto();

        if (nuevoContacto != null) {
            try {
                agendaServicio.addContacto(nuevoContacto);
                // Si la vista de contactos debe mostrar solo los añadidos manualmente,
                // la línea de abajo es correcta. Si es para "todos los online",
                // la actualización la haría el DirectorioDTO.
                vista.getModeloContactos().addElement(nuevoContacto);
                mostrarMensajeFlotante("Contacto agregado: " + nuevoContacto.getNombre(), Color.GREEN);
            } catch (ContactoRepetidoException e) {
                mostrarMensajeFlotante(e.getMessage(), Color.RED);
            }
        }
    }

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

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Mensaje) {
            Mensaje mensajeCifrado = (Mensaje) arg;
            System.out.println("Controlador: Mensaje cifrado recibido de " + mensajeCifrado.getEmisor().getNombre() + ": " + mensajeCifrado.getContenido());

            Conversacion conversacion = conversacionServicio.getConversacion(mensajeCifrado.getEmisor());
            if (conversacion == null || conversacion.getClaveSecretaAes() == null) {
                mostrarMensajeFlotante("Error: No se puede descifrar el mensaje de " + mensajeCifrado.getEmisor().getNombre() + ". Clave no establecida.", Color.RED);
                System.err.println("Controlador: Mensaje cifrado recibido sin clave establecida para descifrar: " + mensajeCifrado.getContenido());
                return;
            }

            try {
                String contenidoDescifrado = Cifrador.descifrar(
                        mensajeCifrado.getContenido(),
                        conversacion.getClaveSecretaAes()
                );
                Mensaje mensajeDescifrado = new Mensaje(contenidoDescifrado, mensajeCifrado.getEmisor(), mensajeCifrado.getReceptor());

                this.conversacionServicio.addMensajeEntrante(mensajeDescifrado);
                String fechaFormateada = sdf.format(mensajeDescifrado.getFecha());

                ChatPantalla chatPantalla = new ChatPantalla(mensajeDescifrado.getEmisor());

                if (!vista.getModeloChats().contains(chatPantalla)) {
                    // MODIFICACIÓN: NO añadir automáticamente a vista.getModeloContactos() aquí.
                    // Si el contacto está en el modelo de contactos (añadido), usar esa instancia.
                    // Si no, añadir el chat con la instancia de mensajeDescifrado.getEmisor().
                    if (vista.getModeloContactos().contains(chatPantalla.getContacto())) {
                        vista.getModeloChats().addElement(new ChatPantalla(this.agendaServicio.buscaNombreContacto(mensajeDescifrado.getEmisor())));
                    } else {
                        vista.getModeloChats().addElement(chatPantalla);
                    }
                }

                if (vista.getListaChats().getSelectedValue() != null
                        && mensajeDescifrado.getEmisor().equals(vista.getListaChats().getSelectedValue().getContacto())) {
                    vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                            mensajeDescifrado,
                            false,
                            fechaFormateada));
                } else {
                    int index = vista.getModeloChats().indexOf(chatPantalla);
                    if (index >= 0) {
                        ChatPantalla chatConNotificacion = vista.getModeloChats().get(index);
                        chatConNotificacion.setPendiente();
                        vista.getModeloChats().set(index, chatConNotificacion);
                    }
                }
            } catch (Exception e) {
                mostrarMensajeFlotante("Error al descifrar el mensaje: " + e.getMessage(), Color.RED);
                System.err.println("Error de descifrado: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (arg instanceof DirectorioDTO) {
            DirectorioDTO directorioRecibido = (DirectorioDTO) arg;
            System.out.println("Controlador: Contactos de directorio recibidos: " + directorioRecibido);
            ArrayList<Contacto> contactosFiltrados = new ArrayList<>(directorioRecibido.getContactos());
            contactosFiltrados.removeIf(c -> c.getNombre().equals(usuarioDTO.getNombre()));

            this.directorioDTO.setContactos(contactosFiltrados);

            // Este es el punto donde la vista de contactos disponibles se actualiza.
            // Si vista.getModeloContactos() es para "todos los conectados", esto es correcto.
            vista.actualizarDirectorio(contactosFiltrados);
            mostrarMensajeFlotante("Directorio de contactos actualizado.", Color.BLUE);
        } else if (arg instanceof Map && ((Map) arg).containsKey("tipo") && ((Map) arg).get("tipo").equals("CLAVE_PUBLICA_DH")) {
            Map<String, Serializable> keyExchangeMessage = (Map<String, Serializable>) arg;
            try {
                String encodedPublicKeyRemota = (String) keyExchangeMessage.get("clavePublica");
                PublicKey publicKeyRemota = ClaveUtil.stringAPublicKey(encodedPublicKeyRemota);
                Contacto emisorRemoto = (Contacto) keyExchangeMessage.get("emisor");

                if (publicKeyRemota != null && emisorRemoto != null) {
                    System.out.println("Controlador: Recibiendo clave pública DH de " + emisorRemoto.getNombre());

                    SecretKey claveSecretaAes = ClaveUtil.derivarClaveSecretaAES(
                            miParClavesDH.getPrivate(),
                            publicKeyRemota
                    );

                    establecerClaveConversacion(emisorRemoto, claveSecretaAes);

                    // *******************************************************************
                    // *** MODIFICACIÓN CLAVE PARA DETENER EL LOOP DH ***
                    // *** Solo enviar mi clave pública si no la he enviado ya para esta conversación ***
                    // *******************************************************************
                    Conversacion conversacionDeRespuesta = conversacionServicio.getConversacion(emisorRemoto);
                    // Comprobar si la clave AES ya está establecida Y si ya hemos enviado nuestra clave.
                    // Si ya está establecida, el intercambio está completo y no necesitamos enviar más claves.
                    // Si no está establecida, pero ya enviamos nuestra clave, significa que estamos esperando la suya.
                    // Si no está establecida Y no hemos enviado nuestra clave, entonces este es el momento de responder.
                    if (conversacionDeRespuesta != null && conversacionDeRespuesta.getClaveSecretaAes() == null && !conversacionDeRespuesta.isMyPublicKeySent()) {
                        System.out.println("Controlador: Respondiendo al intercambio de claves con " + emisorRemoto.getNombre() + " enviando mi clave pública...");
                        try {
                            conexion.iniciarIntercambioDeClaves(emisorRemoto);
                            conversacionDeRespuesta.setMyPublicKeySent(true); // Marcar como enviada
                        } catch (IOException e) {
                            System.err.println("Controlador: Error al enviar mi clave pública en respuesta: " + e.getMessage());
                            mostrarMensajeFlotante("Error al enviar mi clave pública a " + emisorRemoto.getNombre(), Color.RED);
                            e.printStackTrace();
                        }
                    } else if (conversacionDeRespuesta != null && conversacionDeRespuesta.getClaveSecretaAes() != null) {
                        System.out.println("Controlador: Clave AES establecida con " + emisorRemoto.getNombre() + ". Intercambio completo.");
                    } else if (conversacionDeRespuesta != null && conversacionDeRespuesta.isMyPublicKeySent()) {
                        System.out.println("Controlador: Clave AES no establecida con " + emisorRemoto.getNombre() + ", pero mi clave ya fue enviada. Esperando la suya.");
                    }
                }
            } catch (Exception e) {
                mostrarMensajeFlotante("Error al procesar clave pública DH recibida: " + e.getMessage(), Color.RED);
                e.printStackTrace();
            }
        } else if (arg instanceof String) {
            String message = (String) arg;
            if ("CONNECTION_LOST".equals(message)) {
                System.out.println("Controlador: Recibida notificación de CONEXION PERDIDA. Iniciando reconexión...");
                if (this.conexion != null) {
                    reconectar();
                } else {
                    System.out.println("Controlador: No hay una conexión activa para reconectar.");
                    mostrarMensajeFlotante("Conexión perdida. Reinicie la aplicación.", Color.RED);
                    vistaInicioSesion.mostrar();
                    vista.ocultar();
                }
            } else {
                System.out.println("Controlador: Mensaje de texto simple recibido: " + message);
            }
        } else {
            System.out.println("Controlador: Objeto desconocido recibido en update: " + arg.getClass().getName());
        }
    }

    public void establecerClaveConversacion(Contacto contacto, SecretKey claveSecreta) {
        Conversacion conversacion = conversacionServicio.getConversacion(contacto);
        if (conversacion == null) {
            conversacionServicio.agregarConversacion(contacto);
            conversacion = conversacionServicio.getConversacion(contacto);
        }
        if (conversacion != null) {
            conversacion.setClaveSecretaAes(claveSecreta);
            System.out.println("Controlador: Clave AES establecida para la conversación con " + contacto.getNombre());
            mostrarMensajeFlotante("¡Chat seguro con " + contacto.getNombre() + "!", Color.GREEN);
        }
    }

    public DirectorioDTO getDirectorioDTO() {
        return directorioDTO;
    }

    public void setVistaInicioSesion(IVistaInicioSesion vistaInicioSesion) {
        this.vistaInicioSesion = vistaInicioSesion;
    }

    public void setVistaPrincipal(IVistaPrincipal vista) {
        this.vista = vista;
    }

    public Conversacion getConversacion(Contacto contacto) {
        return conversacionServicio.getConversacion(contacto);
    }

    public PublicKey getMiClavePublicaDH() {
        if (miParClavesDH != null) {
            return miParClavesDH.getPublic();
        }
        return null;
    }

    public void cargarConversacion(ChatPantalla selectedValue) {
        int index = vista.getModeloChats().indexOf(selectedValue);

        if (index >= 0) {
            ChatPantalla chatConNotificacion = vista.getModeloChats().get(index);
            chatConNotificacion.setLeido();
            vista.getModeloChats().set(index, chatConNotificacion);
            ArrayList<Mensaje> mensajes = (ArrayList<Mensaje>) this.conversacionServicio.getMensajes(chatConNotificacion.getContacto());
            vista.getPanelMensajes().removeAll();

            for (Mensaje mensaje : mensajes) {
                String fechaFormateada = sdf.format(mensaje.getFecha());
                vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                        mensaje,
                        mensaje.getEmisor().equals(usuarioDTO),
                        fechaFormateada));
            }

            vista.getPanelMensajes().revalidate();
            vista.getPanelMensajes().repaint();
        }
    }

    private void cargarContactosIniciales() {
        try {
            conexion.obtenerContactos();
            System.out.println("Controlador: Solicitud de directorio de contactos enviada.");
        } catch (PerdioConexionException e) {
            reconectar();
        } catch (IOException e) {
            mostrarMensajeFlotante("Error al solicitar contactos: " + e.getMessage(), Color.RED);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Contacto> obtenerContactos() {
        ArrayList<Contacto> contactos = new ArrayList<>();
        if (this.conexion != null) {
            try {
                this.conexion.obtenerContactos();
                contactos = this.directorioDTO.getContactos();
            } catch (PerdioConexionException | IOException | ClassNotFoundException e) {
                reconectar();
            }
        } else {
            System.err.println("Controlador: No hay conexión activa para obtener contactos.");
            mostrarMensajeFlotante("No conectado. Inicie sesión primero.", Color.ORANGE);
        }
        return contactos;
    }

    void reconectar() {
        if (this.conexion == null) {
            System.err.println("Controlador: No hay una instancia de conexión para reconectar.");
            mostrarMensajeFlotante("Error de reconexión: No hay conexión. Reinicie.", Color.RED);
            return;
        }

        try {
            this.conexion.cerrarConexiones();
            this.conexion = new Conexion();
            this.conexion.addObserver(this);
            this.conexion.setMiParClavesDH(this.miParClavesDH);

            this.conexion.conectarServidor(usuarioDTO);
            new Thread(this.conexion).start();

            System.out.println("Controlador: Reconexión exitosa y nuevo hilo de Conexion lanzado.");
            mostrarMensajeFlotante("Reconectado al servidor.", Color.GREEN);
            cargarContactosIniciales();
        } catch (IOException | PuertoEnUsoException | PerdioConexionException e) {
            System.err.println("Controlador: Fallo al reconectar: " + e.getMessage());
            e.printStackTrace();
            if (this.vista.mostrarDialogoReintentarConexion()) {
                mostrarMensajeFlotante("Falló el reintento de reconexión.", Color.RED);
                this.vista.cerrarDialogoReconexion();
                System.exit(0);
            } else {
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

    public void addObserver(Observer o) {
        super.addObserver(o);
    }
}