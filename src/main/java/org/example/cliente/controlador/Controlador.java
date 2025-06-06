package org.example.cliente.controlador;

import org.example.cliente.conexion.*;
import org.example.cliente.factory.FactorySelector;
import org.example.cliente.factory.IPersistenciaAgenda;
import org.example.cliente.factory.IPersistenciaConversaciones;
import org.example.cliente.factory.IPersistenciaFactory;
import org.example.cliente.modelo.*;
import org.example.cliente.modelo.conversacion.Conversacion;
import org.example.cliente.vista.*;
import org.example.cliente.factory.PersistenciaManager;

import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Usuario;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO;


import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

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
    private PersistenciaManager persistenciaManager;

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

        guardarDatosUsuario();

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
        usuarioServicio = null;
        agendaServicio = null;
        conversacionServicio = null;
        usuarioDTO = null;
        persistenciaManager = null;

    }

    /**
     * Inicia un nuevo chat con el contacto seleccionado.
     */
    private void iniciarChat() {
        System.out.println("Inicio de chat");
        Contacto selectedValue = vista.getListaContactos().getSelectedValue();

        if (selectedValue != null && !vista.getModeloChats().contains(new ChatPantalla(selectedValue))) {
            System.out.println("inicio de chat "+selectedValue);
            this.conversacionServicio.agregarConversacion(selectedValue);

            vista.getModeloChats().addElement(new ChatPantalla(selectedValue));
            vista.getListaChats().setSelectedValue(selectedValue, true);
            vista.getPanelMensajes().revalidate();
            vista.getPanelMensajes().repaint();
        } else if(selectedValue != null && vista.getModeloChats().contains(new ChatPantalla(selectedValue))){
            mostrarMensajeFlotante("El contacto " + selectedValue.getNombre() + " ya tiene un chat iniciado", Color.RED);
        }else{
            mostrarMensajeFlotante("Seleccione un contacto", Color.RED);
        }
    }

    /**
     * Envía un mensaje al contacto seleccionado.
     */
    private void enviarMensaje()  {
        Contacto receptor = vista.getListaChats().getSelectedValue().getContacto();
        String contenido = vista.getCampoMensaje().getText();
        Mensaje mensaje = new Mensaje(contenido, this.usuarioDTO, receptor);

        try {

            conexion.enviarMensaje(vista.getListaChats().getSelectedValue().getContacto(), mensaje);
            this.conversacionServicio.addMensajeSaliente(vista.getListaChats().getSelectedValue().getContacto(), mensaje);
            vista.getCampoMensaje().setText("");

            //agregar el mensaje a la vista
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensaje,
                    true,
                    sdf.format(mensaje.getFecha())));
        }catch (PerdioConexionException e){
            // Intentar reconectar
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
            String formato = vistaInicioSesion.getFormatoSeleccionado();
            vistaInicioSesion.ocultar();

            Usuario usuario = new Usuario(nombre, "127.0.0.1", puerto);
            this.usuarioServicio = new UsuarioServicio(usuario);
            this.agendaServicio = new AgendaServicio(usuario);
            this.conversacionServicio = new ConversacionServicio(usuario);
            this.conexion = new Conexion();

            this.usuarioDTO = new Contacto(usuario);
            conexion.conectarServidor(usuarioDTO);

            this.persistenciaManager = new PersistenciaManager(formato, usuarioDTO);

            cargarDatosUsuario();

            // Registrar en el servidor de directorios
           // registrarEnServidorDirectorio(usuario);
            new Thread(conexion).start();
            vista.mostrar();
            vista.titulo("Usuario: " + nombre + " | Ip: "+ "127.0.0.1" + " | Puerto: " + puerto);
            vista.informacionDelUsuario(usuarioDTO);
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

    private void cargarDatosUsuario() {
        try {
            // 1. Cargar agenda
            agendaServicio.setContactos(persistenciaManager.cargarAgenda());

            // 2. Cargar conversaciones (usando agenda actualizada)
            Map<Contacto, Conversacion> conversaciones = persistenciaManager.cargarConversaciones(agendaServicio);
            conversacionServicio.setConversaciones(conversaciones);

            // 3. Actualizar vista
            for (Contacto c : agendaServicio.getContactos()) {
                vista.getModeloContactos().addElement(c);
            }

            for (Contacto c : conversaciones.keySet()) {
                if (!vista.getModeloChats().contains(new ChatPantalla(c))) {
                    vista.getModeloChats().addElement(new ChatPantalla(c));
                }
            }

        } catch (Exception e) {
            mostrarMensajeFlotante("Error al cargar datos del usuario", Color.RED);
            e.printStackTrace();
        }
    }

    private void guardarDatosUsuario() {
        try {
            // 1. Guardar agenda
            System.out.println("AGENDA DEL USUARIO:" + agendaServicio.getContactos());
            persistenciaManager.guardarAgenda(agendaServicio.getContactos());

            // 2. Guardar conversaciones
            persistenciaManager.guardarConversaciones(conversacionServicio.getConversaciones());

            System.out.println("Datos del usuario guardados correctamente.");
        } catch (Exception e) {
            mostrarMensajeFlotante("Error al guardar los datos del usuario", Color.RED);
            e.printStackTrace();
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
     * @param mensaje el mensaje recibido
     */
    public void recibirMensaje(Mensaje mensaje) {

        // Verificar si el contacto existe
        if (agendaServicio.buscaNombreContacto(mensaje.getEmisor().getNombre()) == null) {
            // Si no existe, crear un nuevo contacto (nombre puede ser temporal o sacado del mensaje)
            String nombre = mensaje.getEmisor().getNombre(); // si tenés este campo
            String ip = mensaje.getEmisor().getIp();
            int puerto = mensaje.getEmisor().getPuerto();

            Contacto nuevoContacto = new Contacto(nombre, ip, puerto);
            try {
                agendaServicio.addContacto(nuevoContacto);
            } catch (ContactoRepetidoException e) {
                throw new RuntimeException(e);
            }
        }

        this.conversacionServicio.addMensajeEntrante(mensaje);
        String fechaFormateada = sdf.format(mensaje.getFecha());

        //Creo un chat para el mensaje si no existe opero
        ChatPantalla chatPantalla = new ChatPantalla(mensaje.getEmisor());

        if(!vista.getModeloChats().contains(chatPantalla)){
            // Si el contacto no existe en la lista de contactos, lo agrego
            if(!vista.getModeloContactos().contains(chatPantalla.getContacto())){
                vista.getModeloChats().addElement(chatPantalla);
                vista.getModeloContactos().addElement(mensaje.getEmisor());
            }else {
                vista.getModeloChats().addElement(new ChatPantalla(this.agendaServicio.buscaNombreContacto(mensaje.getEmisor().getNombre())));
            }
        }

        if(vista.getListaChats().getSelectedValue() != null
                && mensaje.getEmisor().equals(vista.getListaChats().getSelectedValue().getContacto())) {
            //agregar el mensaje a la vista
            vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                    mensaje,
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
            System.out.println("Mensaje recibido: " + mensaje);
            recibirMensaje(mensaje);
            // Aquí puedes manejar el mensaje de texto recibido
        } else if (arg instanceof Contacto) {
            Contacto contacto = (Contacto) arg;
            System.out.println("Contacto recibido: " + contacto);
            // Aquí puedes manejar el contacto recibido
        } else if (arg instanceof DirectorioDTO) {
            DirectorioDTO contactos = (DirectorioDTO) arg;
            System.out.println("Contactos recibidos: " + contactos);
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
            ArrayList<Mensaje> mensajes = (ArrayList<Mensaje>) this.conversacionServicio.getMensajes(chatConNotificacion.getContacto());
            // Limpiar el panel de mensajes
            vista.getPanelMensajes().removeAll();

            for(Mensaje mensaje : mensajes) {
                String fechaFormateada = sdf.format(mensaje.getFecha());
                // Agregar el mensaje a la vista
                vista.addMensajeBurbuja(MensajePantalla.mensajeToMensajePantalla(
                        mensaje,
                        mensaje.getEmisor().equals(usuarioDTO),
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