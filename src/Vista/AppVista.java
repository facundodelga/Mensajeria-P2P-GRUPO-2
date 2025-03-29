package Vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import javax.swing.*;

public class AppVista extends JFrame {

    private JTextField textField_BarraBusqueda;
    private JTextField textField_Mensaje;
    private JButton botonEnviar;
    private JList<String> listaChats;
    private DefaultListModel<String> modeloChats;
    private JLabel lblContactoChatActual;
    private JPanel panelMensajes;
    private JPanel panel_ChatActual;
    private JScrollPane scrollPane_MensajesChatActual;

    public AppVista() {
        setTitle("App de Mensajeria");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JPanel panel_Inicio = new JPanel();
        panel_Inicio.setBackground(new Color(32, 32, 32));
        panel_Inicio.setBounds(10, 11, 860, 540);
        panel_Inicio.setLayout(null);
        getContentPane().setBackground(new Color(32, 32, 32));
        add(panel_Inicio);

        JPanel panel_Izquierda = new JPanel();
        panel_Izquierda.setBounds(10, 11, 200, 518);
        panel_Izquierda.setBackground(new Color(61, 61, 61));
        panel_Izquierda.setLayout(null);
        panel_Inicio.add(panel_Izquierda);

        JPanel panel_TituloChats = new JPanel();
        panel_TituloChats.setBounds(0, 0, 200, 30);
        panel_TituloChats.setBackground(new Color(61, 61, 61));
        panel_TituloChats.setLayout(null);
        panel_Izquierda.add(panel_TituloChats);

        JLabel lblChats = new JLabel("Chats");
        lblChats.setForeground(Color.WHITE);
        lblChats.setBounds(10, 5, 100, 20);
        panel_TituloChats.add(lblChats);

        JButton botonMenu = new JButton("⋮");
        botonMenu.setBounds(170, 5, 20, 20);
        botonMenu.setFocusPainted(false);
        botonMenu.setBackground(new Color(61, 61, 61));
        botonMenu.setForeground(Color.WHITE);
        botonMenu.setBorderPainted(false);
        panel_TituloChats.add(botonMenu);

        JPopupMenu menuOpciones = new JPopupMenu();
        JMenuItem itemConfiguracion = new JMenuItem("Configuración");
        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar sesión");
        menuOpciones.add(itemConfiguracion);

        JMenuItem itemAgregarContacto = new JMenuItem("Agregar contacto");
        menuOpciones.add(itemAgregarContacto);
        itemAgregarContacto.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            JTextField nombreField = new JTextField();
            JTextField ipField = new JTextField();
            JTextField puertoField = new JTextField();
            panel.add(new JLabel("Nombre:"));
            panel.add(nombreField);
            panel.add(new JLabel("Dirección IP:"));
            panel.add(ipField);
            panel.add(new JLabel("Puerto:"));
            panel.add(puertoField);
            int result = JOptionPane.showConfirmDialog(this, panel, "Agregar nuevo contacto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String nombre = nombreField.getText().trim();
                if (!nombre.isEmpty()) {
                    modeloChats.addElement(nombre);
                }
            }
        });
        menuOpciones.addSeparator();
        menuOpciones.add(itemCerrarSesion);
        botonMenu.addActionListener(e -> menuOpciones.show(botonMenu, 0, botonMenu.getHeight()));

        textField_BarraBusqueda = new JTextField();
        textField_BarraBusqueda.setBounds(10, 40, 180, 25);
        textField_BarraBusqueda.setBackground(new Color(44, 44, 44));
        textField_BarraBusqueda.setForeground(Color.WHITE);
        panel_Izquierda.add(textField_BarraBusqueda);

        modeloChats = new DefaultListModel<>();
        listaChats = new JList<>(modeloChats);
        listaChats.setComponentPopupMenu(crearPopupChat());
        listaChats.setBackground(new Color(61, 61, 61));
        listaChats.setForeground(Color.WHITE);

        JScrollPane scrollPanel_Chats = new JScrollPane(listaChats);
        scrollPanel_Chats.setBounds(10, 75, 180, 433);
        panel_Izquierda.add(scrollPanel_Chats);

        JPanel panel_Derecho = new JPanel() {
            private final ImageIcon iconoFondo = new ImageIcon(getClass().getResource("/Vista/img/icono_central.png"));
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

        panel_Derecho.setBounds(220, 11, 620, 518);
        panel_Derecho.setBackground(new Color(61, 61, 61));
        panel_Derecho.setLayout(null);
        panel_Inicio.add(panel_Derecho);

        panel_ChatActual = new JPanel();
        panel_ChatActual.setBackground(new Color(44, 44, 44));
        panel_ChatActual.setBounds(0, 0, 620, 518);
        panel_ChatActual.setLayout(null);
        panel_ChatActual.setVisible(false);
        panel_Derecho.add(panel_ChatActual);

        JPanel panel_TituloChatActual = new JPanel();
        panel_TituloChatActual.setBackground(new Color(44, 44, 44));
        panel_TituloChatActual.setBounds(0, 0, 620, 30);
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

        // Scroll chat actual
        scrollPane_MensajesChatActual = new JScrollPane(panelMensajes);
        scrollPane_MensajesChatActual.setBounds(0, 30, 620, 420);
        scrollPane_MensajesChatActual.setBorder(null);
        scrollPane_MensajesChatActual.setOpaque(false);
        scrollPane_MensajesChatActual.getViewport().setOpaque(false);
        panel_ChatActual.add(scrollPane_MensajesChatActual);

        JPanel panel_TextoChatActual = new JPanel();
        panel_TextoChatActual.setLayout(null);
        panel_TextoChatActual.setBounds(0, 460, 620, 58);
        panel_TextoChatActual.setBackground(new Color(44, 44, 44));
        panel_ChatActual.add(panel_TextoChatActual);

        textField_Mensaje = new JTextField();
        textField_Mensaje.setBounds(10, 12, 500, 34);
        panel_TextoChatActual.add(textField_Mensaje);
        textField_Mensaje.setColumns(10);

        botonEnviar = new JButton("Enviar");
        botonEnviar.setBounds(520, 12, 90, 34);
        panel_TextoChatActual.add(botonEnviar);

        setVisible(true);
    }

    private JPopupMenu crearPopupChat() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem eliminarContacto = new JMenuItem("Borrar contacto");
        JMenuItem eliminarChat = new JMenuItem("Borrar chat");

        eliminarContacto.addActionListener(e -> {
            int index = listaChats.getSelectedIndex();
            if (index != -1) {
                modeloChats.remove(index);
            }
        });

        eliminarChat.addActionListener(e -> {
            int index = listaChats.getSelectedIndex();
            if (index != -1) {
                String contacto = modeloChats.getElementAt(index);
                AppControlador.borrarMensajesDeContacto(contacto);
                if (contacto.equals(lblContactoChatActual.getText())) {
                    panelMensajes.removeAll();
                    panelMensajes.revalidate();
                    panelMensajes.repaint();
                }
                JOptionPane.showMessageDialog(this, "Mensajes del chat con '" + contacto + "' eliminados.");
            }
        });

        menu.add(eliminarContacto);
        menu.add(eliminarChat);
        return menu;
    }

    // Getters
    public JTextField getCampoBusqueda() { return textField_BarraBusqueda; }
    public JTextField getCampoMensaje() { return textField_Mensaje; }
    public JButton getBotonEnviar() { return botonEnviar; }
    public JList<String> getListaChats() { return listaChats; }
    public DefaultListModel<String> getModeloChats() { return modeloChats; }
    public JLabel getEtiquetaContacto() { return lblContactoChatActual; }
    public JPanel getPanelMensajes() { return panelMensajes; }
    public JPanel getPanelChatActual() { return panel_ChatActual; }
    public JScrollPane getScrollMensajes() { return scrollPane_MensajesChatActual; }
}





