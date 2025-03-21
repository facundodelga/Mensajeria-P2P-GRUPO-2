
package org.example.vista;

import javax.swing.*;
import java.awt.*;

public class Vista extends JFrame {
    private JTextField nombreField;
    private JTextField hostField;
    private JTextField puertoField;
    private JButton iniciarServidorButton;
    private JButton conectarButton;
    private JTextArea mensajesArea;
    private JTextField mensajeField;
    private JButton enviarMensajeButton;

    public Vista() {
        setTitle("Servidor");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Nombre:"));
        nombreField = new JTextField();
        panel.add(nombreField);

        panel.add(new JLabel("Host:"));
        hostField = new JTextField();
        panel.add(hostField);

        panel.add(new JLabel("Puerto:"));
        puertoField = new JTextField();
        panel.add(puertoField);

        iniciarServidorButton = new JButton("Iniciar Servidor");
        panel.add(iniciarServidorButton);

        conectarButton = new JButton("Conectar");
        panel.add(conectarButton);

        add(panel, BorderLayout.NORTH);

        mensajesArea = new JTextArea();
        mensajesArea.setEditable(false);
        add(new JScrollPane(mensajesArea), BorderLayout.CENTER);

        JPanel mensajePanel = new JPanel(new BorderLayout());
        mensajeField = new JTextField();
        mensajePanel.add(mensajeField, BorderLayout.CENTER);
        enviarMensajeButton = new JButton("Enviar Mensaje");
        mensajePanel.add(enviarMensajeButton, BorderLayout.EAST);

        add(mensajePanel, BorderLayout.SOUTH);
        this.setVisible(true);
    }

    public String getNombre() {
        return nombreField.getText();
    }

    public String getHost() {
        return hostField.getText();
    }

    public int getPuerto() {
        return Integer.parseInt(puertoField.getText());
    }

    public String getMensaje() {
        return mensajeField.getText();
    }

    public void addMensaje(String mensaje) {
        mensajesArea.append(mensaje + "\n");
    }

    public JButton getIniciarServidorButton() {
        return iniciarServidorButton;
    }

    public JButton getConectarButton() {
        return conectarButton;
    }

    public JButton getEnviarMensajeButton() {
        return enviarMensajeButton;
    }
}