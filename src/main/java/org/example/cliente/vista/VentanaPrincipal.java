package org.example.cliente.vista;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Clase que representa la ventana principal de la aplicación de mensajería.
 * Extiende JFrame e implementa la interfaz IVistaPrincipal.
 */
public class VentanaPrincipal extends JFrame implements IVistaPrincipal {

    private JTextField textField_Mensaje;
    private JButton botonEnviar;
    private JList<ChatPantalla> listaChats;
    private DefaultListModel<ChatPantalla> modeloChats;
    private JList<Contacto> listaContactos;
    private DefaultListModel<Contacto> modeloContactos;
    private JLabel lblContactoChatActual;
    private JPanel panelMensajes;
    private JPanel panel_ChatActual;
    private JScrollPane scrollPane_MensajesChatActual;
    private JButton botonAgregarChat;
    IVistaAgregarContacto dialog;
    private JMenuItem itemAgregarContacto;

    // Nuevos componentes para mostrar datos del usuario
    private JLabel lblNombreUsuario;
    private JLabel lblIpUsuario;
    private JLabel lblPuertoUsuario;
    private JButton botonCerrarSesion;

    /**
     * Constructor de la clase VentanaPrincipal.
     * Configura la interfaz gráfica de la ventana principal.
     */
    public VentanaPrincipal() {
        getContentPane().setBackground(new Color(32, 32, 32));
        setTitle("App de Mensajeria");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);

        JPanel panel_Inicio = new JPanel();
        panel_Inicio.setBackground(new Color(32, 32, 32));
        panel_Inicio.setBounds(10, 11, 860, 540);
        panel_Inicio.setLayout(null);
        getContentPane().add(panel_Inicio);

        JPanel panel_Izquierda = new JPanel();
        panel_Izquierda.setBounds(10, 11, 218, 518);
        panel_Izquierda.setBackground(new Color(61, 61, 61));
        panel_Izquierda.setLayout(null);
        panel_Inicio.add(panel_Izquierda);

        // Panel para la sección de Contactos
        JPanel panel_TituloContactos = new JPanel();
        panel_TituloContactos.setBounds(0, 0, 218, 30);
        panel_TituloContactos.setBackground(new Color(61, 61, 61));
        panel_TituloContactos.setLayout(null);
        panel_Izquierda.add(panel_TituloContactos);

        JLabel labelContactos = new JLabel("Contactos");
        labelContactos.setBounds(10, 8, 100, 14);
        labelContactos.setFont(new Font("Tahoma", Font.PLAIN, 14));
        labelContactos.setForeground(Color.WHITE);
        panel_TituloContactos.add(labelContactos);

        JButton botonMenu = new JButton("⋮");
        botonMenu.setBounds(188, 5, 20, 20);
        botonMenu.setFocusPainted(false);
        botonMenu.setBackground(new Color(61, 61, 61));
        botonMenu.setForeground(Color.WHITE);
        botonMenu.setBorderPainted(false);
        panel_TituloContactos.add(botonMenu);

        // Crear menú emergente
        JPopupMenu menuOpciones = new JPopupMenu();
        itemAgregarContacto = new JMenuItem("Agregar contacto");  // se usa en el controlador
        itemAgregarContacto.addActionListener(Controlador.getInstancia());
        itemAgregarContacto.setActionCommand("botonAgregarContacto");
        menuOpciones.add(itemAgregarContacto);

        botonMenu.addActionListener(e -> menuOpciones.show(botonMenu, 0, botonMenu.getHeight()));

        // Panel para mostrar datos del usuario conectado
        JPanel panelDatosUsuario = new JPanel();
        panelDatosUsuario.setBounds(10, 40, 198, 80);
        panelDatosUsuario.setBackground(new Color(50, 50, 50));
        panelDatosUsuario.setLayout(null);
        panel_Izquierda.add(panelDatosUsuario);

        JLabel lblTituloUsuario = new JLabel("Datos del Usuario:");
        lblTituloUsuario.setBounds(10, 5, 180, 14);
        lblTituloUsuario.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblTituloUsuario.setForeground(Color.WHITE);
        panelDatosUsuario.add(lblTituloUsuario);

        lblNombreUsuario = new JLabel("Nombre: ");
        lblNombreUsuario.setBounds(10, 25, 180, 14);
        lblNombreUsuario.setForeground(Color.WHITE);
        panelDatosUsuario.add(lblNombreUsuario);

        lblIpUsuario = new JLabel("IP: ");
        lblIpUsuario.setBounds(10, 40, 180, 14);
        lblIpUsuario.setForeground(Color.WHITE);
        panelDatosUsuario.add(lblIpUsuario);

        lblPuertoUsuario = new JLabel("Puerto: ");
        lblPuertoUsuario.setBounds(10, 55, 180, 14);
        lblPuertoUsuario.setForeground(Color.WHITE);
        panelDatosUsuario.add(lblPuertoUsuario);

        // Botón para cerrar sesión
        botonCerrarSesion = new JButton("Cerrar Sesión");
        botonCerrarSesion.setBounds(10, 130, 198, 25);
        botonCerrarSesion.setActionCommand("CerrarSesion");
        botonCerrarSesion.addActionListener(Controlador.getInstancia());
        panel_Izquierda.add(botonCerrarSesion);

        // Crear renderizador de celdas personalizado para mostrar solo el nombre
        DefaultListCellRenderer userRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Contacto) {
                    setText(((Contacto) value).getNombre());
                }
                if (value instanceof ChatPantalla) {
                    setText(((ChatPantalla) value).getNombre());
                }
                return this;
            }
        };

        // Crear y agregar la lista de Contactos (sección superior)
        modeloContactos = new DefaultListModel<>();
        listaContactos = new JList<>(modeloContactos);
        listaContactos.setBackground(new Color(61, 61, 61));
        listaContactos.setForeground(Color.WHITE);
        listaContactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaContactos.setCellRenderer(userRenderer);

        JScrollPane scrollPanel_Contactos = new JScrollPane(listaContactos);
        scrollPanel_Contactos.setBounds(10, 165, 198, 70);
        panel_Izquierda.add(scrollPanel_Contactos);

        // Agregar un botón para iniciar un nuevo chat desde el contacto seleccionado
        botonAgregarChat = new JButton("Iniciar Chat");
        botonAgregarChat.setBounds(10, 245, 198, 25);
        panel_Izquierda.add(botonAgregarChat);
        botonAgregarChat.setActionCommand("IniciarChat");
        botonAgregarChat.addActionListener(Controlador.getInstancia());

        // Etiqueta para la sección de Chats
        JPanel panel_TituloChats = new JPanel();
        panel_TituloChats.setBounds(0, 280, 218, 30);
        panel_TituloChats.setBackground(new Color(61, 61, 61));
        panel_TituloChats.setLayout(null);
        panel_Izquierda.add(panel_TituloChats);

        JLabel labelChats = new JLabel("Chats");
        labelChats.setBounds(10, 8, 100, 14);
        labelChats.setFont(new Font("Tahoma", Font.PLAIN, 14));
        labelChats.setForeground(Color.WHITE);
        panel_TituloChats.add(labelChats);

        // Crear y agregar la lista de Chats (sección inferior)
        modeloChats = new DefaultListModel<>();
        listaChats = new JList<>(modeloChats);
        listaChats.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaChats.getSelectedValue() != null) {
                Controlador.getInstancia().cargarConversacion(listaChats.getSelectedValue());
            }
        });
        listaChats.setBackground(new Color(61, 61, 61));
        listaChats.setForeground(Color.WHITE);
        listaChats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaChats.setCellRenderer(userRenderer);

        // Agregar listener de selección para abrir el chat cuando se selecciona
        listaChats.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaChats.getSelectedValue() != null) {
                ChatPantalla selectedUser = listaChats.getSelectedValue();
                lblContactoChatActual.setText(selectedUser.getNombre());
                panel_ChatActual.setVisible(true);
                Controlador.getInstancia().cargarConversacion(selectedUser);
            }
        });

        JScrollPane scrollPanel_Chats = new JScrollPane(listaChats);
        scrollPanel_Chats.setBounds(10, 320, 198, 188);
        panel_Izquierda.add(scrollPanel_Chats);

        // Panel derecho
        JPanel panel_Derecho = new JPanel() {
            private final ImageIcon iconoFondo = new ImageIcon(getClass().getResource("/Img/icono_central.png"));
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!panel_ChatActual.isVisible()) {
                    int x = (getWidth() - iconoFondo.getIconWidth()) / 2;
                    int y = (getHeight() - iconoFondo.getIconHeight()) / 2;
                    g.drawImage(iconoFondo.getImage(), x, y, this);
                    g.setFont(new Font("Arial", Font.BOLD, 24));
                    g.setColor(Color.LIGHT_GRAY);
                    FontMetrics fm = g.getFontMetrics();
                    String texto = "Bienvenido";
                    int textWidth = fm.stringWidth(texto);
                    g.drawString(texto, (getWidth() - textWidth) / 2, y + iconoFondo.getIconHeight() + 40);
                }
            }
        };
        panel_Derecho.setBounds(238, 11, 612, 518);
        panel_Derecho.setBackground(new Color(61, 61, 61));
        panel_Derecho.setLayout(null);
        panel_Inicio.add(panel_Derecho);

        panel_ChatActual = new JPanel();
        panel_ChatActual.setBackground(new Color(44, 44, 44));
        panel_ChatActual.setBounds(0, 0, 612, 518);
        panel_ChatActual.setLayout(null);
        panel_ChatActual.setVisible(false);
        panel_Derecho.add(panel_ChatActual);

        JPanel panel_TituloChatActual = new JPanel();
        panel_TituloChatActual.setBackground(new Color(44, 44, 44));
        panel_TituloChatActual.setBounds(0, 0, 612, 30);
        panel_TituloChatActual.setLayout(null);
        panel_ChatActual.add(panel_TituloChatActual);

        lblContactoChatActual = new JLabel("Contacto Chat Actual");
        lblContactoChatActual.setForeground(Color.WHITE);
        lblContactoChatActual.setBounds(10, 5, 200, 20);
        panel_TituloChatActual.add(lblContactoChatActual);

        panelMensajes = new JPanel();
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMensajes.setOpaque(false);
        panelMensajes.setBackground(new Color(44, 44, 44));

        scrollPane_MensajesChatActual = new JScrollPane(panelMensajes);
        scrollPane_MensajesChatActual.setBounds(0, 30, 612, 420);
        scrollPane_MensajesChatActual.setBorder(null);
        scrollPane_MensajesChatActual.setOpaque(false);
        scrollPane_MensajesChatActual.getViewport().setOpaque(false);
        panel_ChatActual.add(scrollPane_MensajesChatActual);

        JPanel panel_TextoChatActual = new JPanel();
        panel_TextoChatActual.setLayout(null);
        panel_TextoChatActual.setBounds(0, 460, 612, 58);
        panel_TextoChatActual.setBackground(new Color(44, 44, 44));
        panel_ChatActual.add(panel_TextoChatActual);

        textField_Mensaje = new JTextField();
        textField_Mensaje.setBounds(10, 12, 492, 34);
        panel_TextoChatActual.add(textField_Mensaje);
        textField_Mensaje.setColumns(10);

        botonEnviar = new JButton("Enviar");
        botonEnviar.setBounds(512, 12, 89, 34);
        botonEnviar.addActionListener(Controlador.getInstancia());
        panel_TextoChatActual.add(botonEnviar);


    }

    /**
     * Establece los datos del usuario conectado
     * @param nombre Nombre del usuario
     * @param ip IP del usuario
     * @param puerto Puerto del usuario
     */
    public void setDatosUsuario(String nombre, String ip, int puerto) {
        lblNombreUsuario.setText("Nombre: " + nombre);
        lblIpUsuario.setText("IP: " + ip);
        lblPuertoUsuario.setText("Puerto: " + puerto);
    }

    /**
     * Método auxiliar para verificar si un UsuarioDTO existe en el modelo.
     * @param modelo El modelo de lista de usuarios.
     * @param usuario El usuario a verificar.
     * @return true si el usuario existe en el modelo, false en caso contrario.
     */
    private boolean modeloContainsUsuario(DefaultListModel<Contacto> modelo, Contacto usuario) {
        for (int i = 0; i < modelo.getSize(); i++) {
            Contacto item = modelo.getElementAt(i);
            // Verificar si tienen el mismo nombre, IP y puerto
            if (item.getNombre().equals(usuario.getNombre()) &&
                    item.getIp().equals(usuario.getIp()) &&
                    item.getPuerto() == usuario.getPuerto()) {
                return true;
            }
        }
        return false;
    }



    /**
     * Muestra la ventana principal.
     */
    @Override
    public void mostrar() {
        setVisible(true);
    }



    /**
     * Agrega un listener de ventana.
     * @param listener El listener de ventana a agregar.
     */
    @Override
    public void addWindowListener(WindowAdapter listener) {
        super.addWindowListener(listener);
    }

    /**
     * Establece el título de la ventana.
     * @param texto El texto a agregar al título.
     */
    public void titulo(String texto) {
        setTitle("App de Mensajeria - " + texto);
    }

    // Getters
    public JTextField getCampoMensaje() { return textField_Mensaje; }
    public JButton getBotonEnviar() { return botonEnviar; }
    public JList<ChatPantalla> getListaChats() { return listaChats; }
    public DefaultListModel<ChatPantalla> getModeloChats() { return modeloChats; }
    public JLabel getEtiquetaContacto() { return lblContactoChatActual; }
    public JPanel getPanelMensajes() { return panelMensajes; }
    public JPanel getPanelChatActual() { return panel_ChatActual; }
    public JScrollPane getScrollMensajes() { return scrollPane_MensajesChatActual; }
    public JList<Contacto> getListaContactos() { return listaContactos; }
    public DefaultListModel<Contacto> getModeloContactos() { return modeloContactos; }
    public JButton getBotonCerrarSesion() { return botonCerrarSesion; }

    /**
     * Muestra la ventana para agregar un nuevo contacto.
     * @return El nuevo contacto agregado.
     */
    @Override
    public Contacto mostrarAgregarContacto() {

        try {
            Controlador.getInstancia().obtenerContactos();
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dialog = new VentanaAgregarContacto(this, Controlador.getInstancia().getDirectorioDTO());

        dialog.mostrar();


        if(dialog.getIP().isEmpty() || dialog.getNombre().isEmpty() || dialog.getPuerto().isEmpty()){
            return null;
        }
        return new Contacto(dialog.getNombre(), dialog.getIP(), Integer.parseInt(dialog.getPuerto()));
    }





    /**
     * Método para que el controlador registre la acción de agregar contacto.
     * @param accion La acción a ejecutar.
     */
    public void setAccionAgregarContacto(Runnable accion) {
        itemAgregarContacto.addActionListener(e -> accion.run());
    }

    /**
     * Método para agregar una burbuja de mensaje al panel de chat.
     * @param mensaje El mensaje a agregar.
     */
    public void addMensajeBurbuja(MensajePantalla mensaje) {
        BurbujaMensaje burbuja = new BurbujaMensaje(mensaje);
        panelMensajes.add(burbuja);
        panelMensajes.revalidate();
        panelMensajes.repaint();
    }
}