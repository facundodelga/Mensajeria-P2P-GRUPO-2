package org.example.cliente.vista;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.modelo.usuario.Contacto;
import org.example.servidor.DirectorioDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class VentanaAgregarContacto extends JDialog implements IVistaAgregarContacto {

    private final JTextField campoNombre;
    private final JTextField campoIP;
    private final JTextField campoPuerto;
    private final JButton botonAceptar;
    private JList<Contacto> listaContactosDisponibles;
    private DefaultListModel<Contacto> modeloContactos;

    // Placeholders, los simulo ya que Swing no tiene
    private final String PLACEHOLDER_NOMBRE = "Usuario123";
    private final String PLACEHOLDER_IP = "192.168.0.1";
    private final String PLACEHOLDER_PUERTO = "8009";

    /**
     * Constructor de la clase VentanaAgregarContacto.
     * @param parent El JFrame padre de esta ventana de diálogo.
     */
    public VentanaAgregarContacto(JFrame parent, DirectorioDTO directorio) {
        super(parent, "Nuevo contacto", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(32, 32, 32));

        // Panel izquierdo para formulario manual
        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(null);
        panelFormulario.setBackground(new Color(32, 32, 32));
        panelFormulario.setPreferredSize(new Dimension(250, 400));

        Color colorTexto = Color.WHITE;

        JLabel lblFormulario = new JLabel("Agregar manualmente:");
        lblFormulario.setForeground(colorTexto);
        lblFormulario.setBounds(20, 20, 200, 20);
        lblFormulario.setFont(new Font("Tahoma", Font.BOLD, 12));
        panelFormulario.add(lblFormulario);

        JLabel lblNombre = new JLabel("Nombre");
        lblNombre.setForeground(colorTexto);
        lblNombre.setBounds(20, 50, 200, 20);
        panelFormulario.add(lblNombre);

        campoNombre = new JTextField();
        campoNombre.setBounds(20, 70, 200, 25);
        agregarPlaceholder(campoNombre, PLACEHOLDER_NOMBRE);
        panelFormulario.add(campoNombre);

        JLabel lblIP = new JLabel("IP");
        lblIP.setForeground(colorTexto);
        lblIP.setBounds(20, 105, 200, 20);
        panelFormulario.add(lblIP);

        campoIP = new JTextField();
        campoIP.setBounds(20, 125, 200, 25);
        agregarPlaceholder(campoIP, PLACEHOLDER_IP);
        panelFormulario.add(campoIP);

        JLabel lblPuerto = new JLabel("Puerto");
        lblPuerto.setForeground(colorTexto);
        lblPuerto.setBounds(20, 160, 200, 20);
        panelFormulario.add(lblPuerto);

        campoPuerto = new JTextField(PLACEHOLDER_PUERTO);
        campoPuerto.setBounds(20, 180, 200, 25);
        agregarPlaceholder(campoPuerto, PLACEHOLDER_PUERTO);
        panelFormulario.add(campoPuerto);

        botonAceptar = new JButton("Agregar Contacto");
        botonAceptar.setBackground(new Color(144, 238, 144));
        botonAceptar.setBounds(20, 230, 200, 30);
        panelFormulario.add(botonAceptar);

        botonAceptar.addActionListener(e -> {
            if (getNombre().isEmpty() || getIP().isEmpty() || getPuerto().isEmpty()) {
                System.out.println("Todos los campos deben estar llenos");
                JOptionPane.showMessageDialog(this, "Todos los campos deben estar llenos", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!getIP().matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
                System.out.println("La IP no es válida");
                JOptionPane.showMessageDialog(this, "La IP no es válida", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println("Nombre: " + getNombre());
                System.out.println("IP: " + getIP());
                System.out.println("Puerto: " + getPuerto());

                try {
                    int puerto = Integer.parseInt(getPuerto());
                    if (puerto < 0 || puerto > 65535) {
                        JOptionPane.showMessageDialog(this, "El puerto debe estar entre 0 y 65535", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        dispose();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "El puerto debe ser un número", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Panel derecho para lista de contactos disponibles
        JPanel panelListaContactos = new JPanel();
        panelListaContactos.setLayout(new BorderLayout());
        panelListaContactos.setBackground(new Color(40, 40, 40));

        JLabel lblContactosDisponibles = new JLabel("Contactos disponibles:");
        lblContactosDisponibles.setForeground(Color.WHITE);
        lblContactosDisponibles.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblContactosDisponibles.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelListaContactos.add(lblContactosDisponibles, BorderLayout.NORTH);

        // Crear el modelo y la lista de contactos
        modeloContactos = new DefaultListModel<>();
        listaContactosDisponibles = new JList<>(modeloContactos);
        listaContactosDisponibles.setBackground(new Color(50, 50, 50));
        listaContactosDisponibles.setForeground(Color.WHITE);
        listaContactosDisponibles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurar el renderizador de celdas para mostrar solo el nombre
        listaContactosDisponibles.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Contacto) {
                    Contacto contacto = (Contacto) value;
                    setText(contacto.getNombre() + " - " + contacto.getIp() + ":" + contacto.getPuerto());
                }
                return this;
            }
        });

        // Al seleccionar un contacto de la lista, rellenar los campos
        listaContactosDisponibles.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listaContactosDisponibles.getSelectedValue() != null) {
                Contacto contactoSeleccionado = listaContactosDisponibles.getSelectedValue();
                campoNombre.setText(contactoSeleccionado.getNombre());
                campoNombre.setForeground(Color.BLACK);
                campoIP.setText(contactoSeleccionado.getIp());
                campoIP.setForeground(Color.BLACK);
                campoPuerto.setText(String.valueOf(contactoSeleccionado.getPuerto()));
                campoPuerto.setForeground(Color.BLACK);
            }
        });

        JScrollPane scrollPane = new JScrollPane(listaContactosDisponibles);
        panelListaContactos.add(scrollPane, BorderLayout.CENTER);

        // Botón para seleccionar directamente un contacto de la lista
        JButton botonSeleccionar = new JButton("Seleccionar contacto");
        botonSeleccionar.setBackground(new Color(100, 149, 237));
        botonSeleccionar.setForeground(Color.WHITE);
        botonSeleccionar.addActionListener(e -> {
            if (listaContactosDisponibles.getSelectedValue() != null) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un contacto de la lista",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });
        panelListaContactos.add(botonSeleccionar, BorderLayout.SOUTH);

        // Cargar la lista de contactos disponibles desde el controlador
        cargarContactosDisponibles(directorio);

        // Agregar los paneles al contenedor principal
        getContentPane().add(panelFormulario, BorderLayout.WEST);
        getContentPane().add(panelListaContactos, BorderLayout.CENTER);

        // Dentro del constructor de VentanaAgregarContacto
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                limpiarCampos();
            }
        });
    }

    /**
     * Carga la lista de contactos disponibles desde el controlador
     */
    private void cargarContactosDisponibles(DirectorioDTO directorio) {
        modeloContactos.clear();
        for (Contacto contacto : directorio.getContactos()) {
            modeloContactos.addElement(contacto);
        }
    }





    /**
     * Método para limpiar los campos de texto.
     * Se llama cuando se cierra la ventana para restablecer los placeholders.
     */
    private void limpiarCampos() {
        campoNombre.setText("");
        campoIP.setText("");
        campoPuerto.setText("");
    }

    /**
     * Método para agregar un placeholder a un campo de texto.
     * @param campo El campo de texto al que se le agregará el placeholder.
     * @param placeholder El texto del placeholder.
     */
    private void agregarPlaceholder(JTextField campo, String placeholder) {
        campo.setForeground(Color.GRAY);
        campo.setText(placeholder);

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (campo.getText().equals(placeholder)) {
                    campo.setText("");
                    campo.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (campo.getText().isEmpty()) {
                    campo.putClientProperty(placeholder, true);
                    campo.setForeground(Color.GRAY);
                    campo.setText(placeholder);
                }
            }
        });
    }

    /**
     * Obtiene el nombre ingresado en el campo de texto.
     * @return El nombre ingresado.
     */
    @Override
    public String getNombre() {
        String texto = campoNombre.getText().trim();
        if (texto.equals(PLACEHOLDER_NOMBRE)) {
            return "";
        }
        return texto;
    }

    /**
     * Obtiene la dirección IP ingresada en el campo de texto.
     * @return La dirección IP ingresada.
     */
    @Override
    public String getIP() {
        String texto = campoIP.getText().trim();
        if (texto.equals(PLACEHOLDER_IP)) {
            return "";
        }
        return texto;
    }

    /**
     * Obtiene el puerto ingresado en el campo de texto.
     * @return El puerto ingresado.
     */
    @Override
    public String getPuerto() {
        String texto = campoPuerto.getText().trim();
        if (texto.equals(PLACEHOLDER_PUERTO)) {
            return "";
        }
        return texto;
    }

    @Override
    public void mostrar() {
        setVisible(true);
    }

    @Override
    public void ocultar() {
        setVisible(false);
    }

    @Override
    public void actualizarDirectorio(ArrayList<Contacto> contactos) {
        modeloContactos.clear();
        for (Contacto contacto : contactos) {
            modeloContactos.addElement(contacto);
        }
        listaContactosDisponibles.setModel(modeloContactos);
    }

    /**
     * Obtiene el contacto seleccionado de la lista.
     * @return El contacto seleccionado.
     */
    public Contacto getContactoSeleccionado() {
        return listaContactosDisponibles.getSelectedValue();
    }
}