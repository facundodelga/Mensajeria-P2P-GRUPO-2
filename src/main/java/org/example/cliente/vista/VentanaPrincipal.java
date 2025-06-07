package org.example.cliente.vista;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.usuario.Contacto;

import java.awt.*;
import java.awt.event.WindowAdapter;
import javax.swing.*;
import javax.swing.border.TitledBorder;

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
    private JDialog dialogoReconexion;

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

        // Cambiamos a BorderLayout para el contenido principal
        getContentPane().setLayout(new BorderLayout());

        // Panel principal con márgenes
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(32, 32, 32));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(panelPrincipal, BorderLayout.CENTER);

        // Panel izquierdo (contactos y chats)
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setBackground(new Color(61, 61, 61));
        panelIzquierdo.setPreferredSize(new Dimension(300, Integer.MAX_VALUE));
        panelIzquierdo.setLayout(new BorderLayout(0, 10));
        panelPrincipal.add(panelIzquierdo, BorderLayout.WEST);

        // Panel de datos del usuario
        JPanel panelDatosUsuario = new JPanel();
        panelDatosUsuario.setBackground(new Color(50, 50, 50));
        panelDatosUsuario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelDatosUsuario.setLayout(new GridLayout(4, 1, 0, 5));

        JLabel lblTituloUsuario = new JLabel("Datos del Usuario:");
        lblTituloUsuario.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblTituloUsuario.setForeground(Color.WHITE);
        panelDatosUsuario.add(lblTituloUsuario);

        lblNombreUsuario = new JLabel("Nombre: ");
        lblNombreUsuario.setForeground(Color.WHITE);
        panelDatosUsuario.add(lblNombreUsuario);

        lblIpUsuario = new JLabel("IP: ");
        lblIpUsuario.setForeground(Color.WHITE);
        panelDatosUsuario.add(lblIpUsuario);

        lblPuertoUsuario = new JLabel("Puerto: ");
        lblPuertoUsuario.setForeground(Color.WHITE);
        panelDatosUsuario.add(lblPuertoUsuario);

        panelIzquierdo.add(panelDatosUsuario, BorderLayout.NORTH);

        // Botón cerrar sesión
        botonCerrarSesion = new JButton("Cerrar Sesión");
        botonCerrarSesion.setActionCommand("CerrarSesion");
        botonCerrarSesion.addActionListener(Controlador.getInstancia());
        panelIzquierdo.add(botonCerrarSesion, BorderLayout.SOUTH);

        // Panel central para contactos y chats
        JPanel panelCentralIzquierdo = new JPanel();
        panelCentralIzquierdo.setLayout(new BorderLayout(0, 10));
        panelCentralIzquierdo.setOpaque(false);
        panelIzquierdo.add(panelCentralIzquierdo, BorderLayout.CENTER);

        // Panel de contactos
        JPanel panelContactos = new JPanel(new BorderLayout());
        panelContactos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(), "Contactos",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Tahoma", Font.PLAIN, 14), Color.WHITE));
        panelContactos.setOpaque(false);

        modeloContactos = new DefaultListModel<>();
        listaContactos = new JList<>(modeloContactos);
        listaContactos.setBackground(new Color(61, 61, 61));
        listaContactos.setForeground(Color.WHITE);
        listaContactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaContactos.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Contacto) {
                    setText(((Contacto) value).getNombre());
                }
                return this;
            }
        });

        JScrollPane scrollContactos = new JScrollPane(listaContactos);
        scrollContactos.setPreferredSize(new Dimension(0, 100)); // Altura fija para contactos
        panelContactos.add(scrollContactos, BorderLayout.CENTER);

        // Botones para contactos
        JPanel panelBotonesContactos = new JPanel(new GridLayout(1, 2, 5, 0));
        panelBotonesContactos.setOpaque(false);

        JButton botonAgregarContacto = new JButton("Nuevo Contacto");
        botonAgregarContacto.setActionCommand("botonAgregarContacto");
        botonAgregarContacto.addActionListener(Controlador.getInstancia());
        panelBotonesContactos.add(botonAgregarContacto);

        botonAgregarChat = new JButton("Iniciar Chat");
        botonAgregarChat.setActionCommand("IniciarChat");
        botonAgregarChat.addActionListener(Controlador.getInstancia());
        panelBotonesContactos.add(botonAgregarChat);

        panelContactos.add(panelBotonesContactos, BorderLayout.SOUTH);
        panelCentralIzquierdo.add(panelContactos, BorderLayout.NORTH);

        // Panel de chats
        JPanel panelChats = new JPanel(new BorderLayout());
        panelChats.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(), "Chats",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Tahoma", Font.PLAIN, 14), Color.WHITE));
        panelChats.setOpaque(false);

        modeloChats = new DefaultListModel<>();
        listaChats = new JList<>(modeloChats);
        listaChats.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaChats.getSelectedValue() != null) {
                ChatPantalla chatSeleccionado = listaChats.getSelectedValue();
                lblContactoChatActual.setText(chatSeleccionado.getContacto().getNombre());
                panel_ChatActual.setVisible(true);
                Controlador.getInstancia().cargarConversacion(chatSeleccionado);
                chatSeleccionado.setLeido();
                modeloChats.setElementAt(chatSeleccionado, modeloChats.indexOf(chatSeleccionado));
            }
        });
        listaChats.setBackground(new Color(61, 61, 61));
        listaChats.setForeground(Color.WHITE);
        listaChats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaChats.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ChatPantalla) {
                    setText(((ChatPantalla) value).getNombre());
                }
                return this;
            }
        });

        panelChats.add(new JScrollPane(listaChats), BorderLayout.CENTER);
        panelCentralIzquierdo.add(panelChats, BorderLayout.CENTER);

        // Panel derecho (área de chat)
        JPanel panel_Derecho = new JPanel(new BorderLayout()) {
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
        panel_Derecho.setBackground(new Color(61, 61, 61));
        panelPrincipal.add(panel_Derecho, BorderLayout.CENTER);

        // Panel de chat actual (oculto inicialmente)
        panel_ChatActual = new JPanel(new BorderLayout());
        panel_ChatActual.setBackground(new Color(44, 44, 44));
        panel_ChatActual.setVisible(false);
        panel_Derecho.add(panel_ChatActual, BorderLayout.CENTER);

        // Cabecera del chat
        JPanel panelCabeceraChat = new JPanel(new BorderLayout());
        panelCabeceraChat.setBackground(new Color(44, 44, 44));
        panelCabeceraChat.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        lblContactoChatActual = new JLabel("Contacto Chat Actual");
        lblContactoChatActual.setForeground(Color.WHITE);
        panelCabeceraChat.add(lblContactoChatActual, BorderLayout.WEST);
        panel_ChatActual.add(panelCabeceraChat, BorderLayout.NORTH);

        // Área de mensajes
        panelMensajes = new JPanel();
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMensajes.setOpaque(false);

        scrollPane_MensajesChatActual = new JScrollPane(panelMensajes);
        scrollPane_MensajesChatActual.setBorder(null);
        scrollPane_MensajesChatActual.setOpaque(false);
        scrollPane_MensajesChatActual.getViewport().setOpaque(false);
        panel_ChatActual.add(scrollPane_MensajesChatActual, BorderLayout.CENTER);

        // Panel de entrada de mensaje
        JPanel panelEntradaMensaje = new JPanel(new BorderLayout(5, 0));
        panelEntradaMensaje.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelEntradaMensaje.setBackground(new Color(44, 44, 44));

        // Configurar fuente común
        Font fuente = new Font("Tahoma", Font.BOLD, 14);

        // Campo de texto
        textField_Mensaje = new JTextField();
        textField_Mensaje.setFont(fuente);
        textField_Mensaje.setPreferredSize(new Dimension(0, 36)); // Alto fijo de 36px
        panelEntradaMensaje.add(textField_Mensaje, BorderLayout.CENTER);

        // Botón enviar
        botonEnviar = new JButton("Enviar");
        botonEnviar.setFont(fuente);
        botonEnviar.setPreferredSize(new Dimension(100, 36)); // Mismo alto que el campo de texto
        botonEnviar.addActionListener(Controlador.getInstancia());
        panelEntradaMensaje.add(botonEnviar, BorderLayout.EAST);

        panel_ChatActual.add(panelEntradaMensaje, BorderLayout.SOUTH);
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

    /**
     * Muestra un diálogo de reconexión cuando se pierde la conexión con el servidor.
     *
     * @return true si el diálogo fue mostrado correctamente
     */
    public boolean mostrarDialogoReconexion() {
        new Thread(() -> {
            this.dialogoReconexion = new JDialog(this, "Error de Conexión", true);
            dialogoReconexion.setSize(400, 200);
            dialogoReconexion.setLocationRelativeTo(this);
            dialogoReconexion.setLayout(new BorderLayout());

            // Panel con mensaje
            JPanel panelMensaje = new JPanel();
            panelMensaje.setLayout(new BorderLayout());
            panelMensaje.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Icono de advertencia
            JLabel iconoLabel = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
            panelMensaje.add(iconoLabel, BorderLayout.WEST);

            // Mensaje de error
            JPanel panelTexto = new JPanel();
            panelTexto.setLayout(new BoxLayout(panelTexto, BoxLayout.Y_AXIS));
            panelTexto.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

            JLabel mensajeLabel = new JLabel("Se ha perdido la conexión con el servidor.");
            mensajeLabel.setFont(new Font("Tahoma", Font.BOLD, 16));

            JLabel preguntaLabel = new JLabel("");
            preguntaLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));

            panelTexto.add(mensajeLabel);
            panelTexto.add(Box.createRigidArea(new Dimension(0, 10)));
            panelTexto.add(preguntaLabel);

            panelMensaje.add(panelTexto, BorderLayout.CENTER);

            // Panel de botones
            JPanel panelBotones = new JPanel();
            panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panelBotones.setLayout(new FlowLayout(FlowLayout.RIGHT));

            dialogoReconexion.add(panelMensaje, BorderLayout.CENTER);
            dialogoReconexion.add(panelBotones, BorderLayout.SOUTH);

            // Mostrar el diálogo
            dialogoReconexion.setVisible(true);
        }).start();

        return true;
    }

    public void cerrarDialogoReconexion() {
        if (dialogoReconexion != null) {
            dialogoReconexion.dispose();
        }
    }

    /**
     * Muestra un diálogo para intentar reconectar o salir del programa
     * @return true si el usuario decidió intentar reconectar, false si decidió salir
     */
    public boolean mostrarDialogoReintentarConexion() {
        final boolean[] resultado = new boolean[1];

        this.dialogoReconexion = new JDialog(this, "Error de Conexión", true);
        dialogoReconexion.setSize(400, 200);
        dialogoReconexion.setLocationRelativeTo(this);
        dialogoReconexion.setLayout(new BorderLayout());

        // Panel con mensaje
        JPanel panelMensaje = new JPanel();
        panelMensaje.setLayout(new BorderLayout());
        panelMensaje.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Icono de advertencia
        JLabel iconoLabel = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
        panelMensaje.add(iconoLabel, BorderLayout.WEST);

        // Mensaje de error
        JPanel panelTexto = new JPanel();
        panelTexto.setLayout(new BoxLayout(panelTexto, BoxLayout.Y_AXIS));
        panelTexto.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JLabel mensajeLabel = new JLabel("Se ha perdido la conexión con el servidor.");
        mensajeLabel.setFont(new Font("Tahoma", Font.BOLD, 14));

        JLabel preguntaLabel = new JLabel("¿Desea intentar reconectar o salir del programa?");
        preguntaLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));

        panelTexto.add(mensajeLabel);
        panelTexto.add(Box.createRigidArea(new Dimension(0, 10)));
        panelTexto.add(preguntaLabel);

        panelMensaje.add(panelTexto, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelBotones.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton botonReconectar = new JButton("Reintentar conexión");
        botonReconectar.addActionListener(e -> {
            resultado[0] = true;
            dialogoReconexion.dispose();
        });

        JButton botonSalir = new JButton("Salir");
        botonSalir.addActionListener(e -> {
            resultado[0] = false;
            dialogoReconexion.dispose();
        });

        panelBotones.add(botonReconectar);
        panelBotones.add(botonSalir);

        dialogoReconexion.add(panelMensaje, BorderLayout.CENTER);
        dialogoReconexion.add(panelBotones, BorderLayout.SOUTH);

        // Mostrar el diálogo
        dialogoReconexion.setVisible(true);

        return resultado[0];
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
        //itemAgregarContacto.addActionListener(e -> accion.run());
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

    @Override
    public void informacionDelUsuario(Contacto contacto) {
        this.lblNombreUsuario.setText("Nombre: " + contacto.getNombre());
        this.lblIpUsuario.setText("IP: " + contacto.getIp());
        this.lblPuertoUsuario.setText("Puerto: " + contacto.getPuerto());
    }

    public void ocultar() {
        setVisible(false);
    }

    /**
     * Muestra un diálogo de confirmación para cerrar sesión.
     * @return true si el usuario confirma que quiere cerrar sesión, false en caso contrario
     */
    public boolean mostrarConfirmacionCerrarSesion() {
        int respuesta = JOptionPane.showOptionDialog(
                this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar cierre de sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Sí", "No"}, // Botones personalizados
                "No" // Opción por defecto
        );

        return respuesta == JOptionPane.YES_OPTION;
    }

    @Override
    public void limpiarCampos() {
        textField_Mensaje.setText("");
        panelMensajes.removeAll();
        panelMensajes.revalidate();
        panelMensajes.repaint();
        lblContactoChatActual.setText("Contacto Chat Actual");
        panel_ChatActual.setVisible(false);
        modeloChats.clear();
        modeloContactos.clear();
        listaContactos.clearSelection();
        listaChats.clearSelection();

    }
}