package org.example.vista;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class VentanaAgregarContacto extends JDialog implements IVistaAgregarContacto {

    private final JTextField campoNombre;
    private final JTextField campoIP;
    private final JTextField campoPuerto;
    private final JButton botonAceptar;

    // Placeholders, los simulo ya que Swing no tiene
    private final String PLACEHOLDER_NOMBRE = "Usuario123";
    private final String PLACEHOLDER_IP = "192.168.0.1";
    private final String PLACEHOLDER_PUERTO = "8009";

    public VentanaAgregarContacto(JFrame parent) {
        super(parent, "Nuevo contacto", true);
        setSize(250, 280);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(null);
        getContentPane().setBackground(new Color(32, 32, 32)); 

        Color colorTexto = Color.WHITE;

        JLabel lblNombre = new JLabel("Nombre");
        lblNombre.setForeground(colorTexto);
        lblNombre.setBounds(20, 20, 200, 20);
        getContentPane().add(lblNombre);

        campoNombre = new JTextField();
        campoNombre.setBounds(20, 40, 200, 25);
        agregarPlaceholder(campoNombre, PLACEHOLDER_NOMBRE);
        getContentPane().add(campoNombre);

        JLabel lblIP = new JLabel("IP");
        lblIP.setForeground(colorTexto);
        lblIP.setBounds(20, 75, 200, 20);
        getContentPane().add(lblIP);

        campoIP = new JTextField();
        campoIP.setBounds(20, 95, 200, 25);
        agregarPlaceholder(campoIP, PLACEHOLDER_IP);
        getContentPane().add(campoIP);

        JLabel lblPuerto = new JLabel("Puerto");
        lblPuerto.setForeground(colorTexto);
        lblPuerto.setBounds(20, 130, 200, 20);
        getContentPane().add(lblPuerto);

        campoPuerto = new JTextField();
        campoPuerto.setBounds(20, 150, 200, 25);
        agregarPlaceholder(campoPuerto, PLACEHOLDER_PUERTO);
        getContentPane().add(campoPuerto);

        botonAceptar = new JButton("Agregar Contacto");
        botonAceptar.setBackground(new Color(144, 238, 144));
        botonAceptar.setBounds(70, 200, 100, 30);
        getContentPane().add(botonAceptar);


        botonAceptar.addActionListener(e -> {
                    if (getNombre().isEmpty() || getIP().isEmpty() || getPuerto().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Todos los campos deben estar llenos", "Error", JOptionPane.ERROR_MESSAGE);
                    } else if (!getIP().matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
                        JOptionPane.showMessageDialog(this, "La IP no es válida", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
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
    }

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
                    campo.setText(placeholder);
                    campo.setForeground(Color.GRAY);
                }
            }
        });
    }

    // Getters para acceder a los campos (sin tomar los placeholders)
    @Override
    public String getNombre() {
        String texto = campoNombre.getText().trim();
        return texto.equals(PLACEHOLDER_NOMBRE) ? "" : texto;
    }

    @Override
    public String getIP() {
        String texto = campoIP.getText().trim();
        return texto.equals(PLACEHOLDER_IP) ? "" : texto;
    }

    @Override
    public String getPuerto() {
        String texto = campoPuerto.getText().trim();
        return texto.equals(PLACEHOLDER_PUERTO) ? "" : texto;
    }

}
