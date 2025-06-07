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
public class Controlador implements ActionListener, Observer {
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
                obtenerContactos();
                break;
        }
    }

    private void cerrarSesion() {
        if (conexion != null) {
            conexion.cerrarConexiones();
            // Limpiar la instancia de conexión para que se cree una nueva al iniciar sesión
            conexion = null;
        }
        vista.ocultar();
        vista.limpiarCampos();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
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

            if (conversacionActual != null && conversacionActual.getClaveSecretaAes() == null) {
                try {
                    mostrarMensajeFlotante("Iniciando intercambio de claves con " + selectedValue.getNombre() + "...", Color.BLUE);
                    conexion.iniciarIntercambioDeClaves(selectedValue);
                } catch (Exception e) {
                    mostrarMensajeFlotante("Error al iniciar intercambio de claves: " + e.getMessage(), Color.RED);
                    e.printStackTrace();
                    return;
                }
            } else if (conversacionActual != null && conversacionActual.getClaveSecretaAes() != null) {
                mostrarMensajeFlotante("Clave secreta ya establecida con " + selectedValue.getNombre(), Color.GREEN);
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

            // Si la conexión ya existe de una sesión anterior, asegúrate de cerrarla antes de crear una nueva.
            if (this.conexion != null) {
                this.conexion.cerrarConexiones();
            }
            this.conexion = new Conexion(); // ¡Importante! Creamos una nueva instancia de Conexion por sesión.

            this.usuarioDTO = new Contacto(usuario);

            conexion.conectarServidor(usuarioDTO);

            new Thread(conexion).start(); // Inicia el hilo de la conexión para esperar mensajes
            vista.mostrar();
            vista.titulo("Usuario: " + nombre + " | Ip: " + "127.0.0.1" + " | Puerto: " + puerto);
            vista.informacionDelUsuario(usuarioDTO);
        } catch (NumberFormatException e) {
            mostrarMensajeFlotante("El puerto debe ser un número entre 0 y 65535", Color.RED);
        } catch (PuertoEnUsoException e) {
            mostrarMensajeFlotante(e.getMessage(), Color.RED);
            vistaInicioSesion.mostrar();
        } catch (IOException | PerdioConexionException e) {
            // Si hay un error inicial de conexión, intentar reconectar o informar al usuario.
            mostrarMensajeFlotante("Error al conectar inicialmente: " + e.getMessage(), Color.RED);
            e.printStackTrace();
            // Decide si quieres llamar a reconectar aquí o simplemente volver a la vista de inicio de sesión.
            vistaInicioSesion.mostrar(); // Vuelve a la pantalla de inicio de sesión
        }
    }

    public void agregarNuevoContacto() {
        Contacto nuevoContacto = null;
        nuevoContacto = vista.mostrarAgregarContacto();

        if (nuevoContacto != null) {
            try {
                agendaServicio.addContacto(nuevoContacto);
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
                    if (!vista.getModeloContactos().contains(chatPantalla.getContacto())) {
                        vista.getModeloChats().addElement(chatPantalla);
                        vista.getModeloContactos().addElement(mensajeDescifrado.getEmisor());
                    } else {
                        vista.getModeloChats().addElement(new ChatPantalla(this.agendaServicio.buscaNombreContacto(mensajeDescifrado.getEmisor())));
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
            DirectorioDTO contactos = (DirectorioDTO) arg;
            System.out.println("Controlador: Contactos de directorio recibidos: " + contactos);
            contactos.getContactos().removeIf(c -> c.getNombre().equals(usuarioDTO.getNombre()));
            this.directorioDTO = contactos;
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
                    mostrarMensajeFlotante("Clave secreta AES establecida con " + emisorRemoto.getNombre(), Color.GREEN);

                    // Si la otra parte ya nos envió su clave, y nosotros todavía no enviamos la nuestra,
                    // podríamos iniciar el intercambio aquí (aunque el flujo usual es que quien inicia el chat, inicia el intercambio).
                    // Para robustez, se podría llamar a conexion.iniciarIntercambioDeClaves(emisorRemoto)
                    // aquí también, pero la lógica de 'iniciarChat' ya lo hace, y la de 'Conexion' debería evitar duplicados.
                }
            } catch (Exception e) {
                mostrarMensajeFlotante("Error al procesar clave pública DH recibida: " + e.getMessage(), Color.RED);
                e.printStackTrace();
            }
        } else if (arg instanceof String) {
            String message = (String) arg;
            if ("CONNECTION_LOST".equals(message)) {
                System.out.println("Controlador: Recibida notificación de CONEXION PERDIDA. Iniciando reconexión...");
                // Solo llamar a reconectar si hay una instancia de conexión y no estamos ya en proceso
                // de reconexión (esto último se manejaría con un flag si la lógica fuera más compleja).
                if (this.conexion != null) {
                    reconectar();
                } else {
                    System.out.println("Controlador: No hay una conexión activa para reconectar.");
                    mostrarMensajeFlotante("Conexión perdida. Reinicie la aplicación.", Color.RED);
                    // Podrías forzar la vuelta a la vista de inicio de sesión o cerrar la app.
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

    public ArrayList<Contacto> obtenerContactos() {
        ArrayList<Contacto> contactos = new ArrayList<>();
        if (this.conexion != null) { // Asegurarse de que la conexión exista
            try {
                contactos = this.conexion.obtenerContactos();
            } catch (PerdioConexionException e) {
                reconectar();
            }
        } else {
            System.err.println("Controlador: No hay conexión activa para obtener contactos.");
            mostrarMensajeFlotante("No conectado. Inicie sesión primero.", Color.ORANGE);
        }
        return contactos;
    }

    // Método para la reconexión, ahora manejado solo por el Controlador
    void reconectar() {
        if (this.conexion == null) {
            System.err.println("Controlador: No hay una instancia de conexión para reconectar.");
            mostrarMensajeFlotante("Error de reconexión: No hay conexión. Reinicie.", Color.RED);
            return;
        }

        try {
            this.conexion.reconectar(); // Intenta reconectar el socket y streams
            // Si la reconexión de la conexión es exitosa, iniciamos un *nuevo* ManejadorEntradas hilo
            // para la *nueva* conexión establecida.
            // Para esto, es crucial que Conexion.reconectar() o Conexion.conectar()
            // haya creado un nuevo socket y streams.
            new Thread(conexion).start(); // Esto iniciará el método run() de Conexion, que a su vez creará un nuevo ManejadorEntradas.
            System.out.println("Controlador: Reconexión iniciada y nuevo hilo de Conexion lanzado.");
        } catch (IOException e) {
            System.err.println("Controlador: Fallo al reconectar: " + e.getMessage());
            if (this.vista.mostrarDialogoReintentarConexion()) {
                try {
                    this.conexion.reconectar();
                    new Thread(conexion).start();
                } catch (IOException ex) {
                    System.err.println("Controlador: Fallo fatal al reintentar reconexión: " + ex.getMessage());
                    this.vista.cerrarDialogoReconexion();
                    System.exit(0);
                }
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
}