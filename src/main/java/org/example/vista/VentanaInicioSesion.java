package org.example.vista;

import java.awt.*;
import javax.swing.*;

public class VentanaInicioSesion extends JFrame {

    private JTextField textField_CampoNickname;
    private JTextField TextField_CampoPuerto;
    private JButton botonLogin;
    private Runnable onLoginExitoso;

    public VentanaInicioSesion() {
    	setBackground(new Color(32, 32, 32));
        setTitle("Inicio de Sesión");
        setSize(350, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(61, 61, 61));
        getContentPane().setLayout(null);

        JLabel Label_Nickname = new JLabel("Nickname:");
        Label_Nickname.setBounds(82, 111, 100, 25);
        Label_Nickname.setFont(new Font("Tahoma", Font.PLAIN, 14));
        Label_Nickname.setForeground(Color.WHITE);
        getContentPane().add(Label_Nickname);

        textField_CampoNickname = new JTextField();
        textField_CampoNickname.setBounds(82, 150, 180, 25);
        getContentPane().add(textField_CampoNickname);

        JLabel Label_Puerto = new JLabel("Puerto:");
        Label_Puerto.setBounds(82, 186, 100, 25);
        Label_Puerto.setFont(new Font("Tahoma", Font.PLAIN, 14));
        Label_Puerto.setForeground(Color.WHITE);
        getContentPane().add(Label_Puerto);

        TextField_CampoPuerto = new JTextField();
        TextField_CampoPuerto.setBounds(82, 222, 180, 25);
        getContentPane().add(TextField_CampoPuerto);

        botonLogin = new JButton("Aceptar");
        botonLogin.setBounds(102, 291, 130, 30);
        getContentPane().add(botonLogin);
        
        JLabel Label_ConfigurarUsuario = new JLabel("Configurar Usuario");
        Label_ConfigurarUsuario.setBounds(91, 31, 171, 25);
        Label_ConfigurarUsuario.setForeground(new Color(255, 255, 255));
        Label_ConfigurarUsuario.setFont(new Font("Tahoma", Font.PLAIN, 20));
        getContentPane().add(Label_ConfigurarUsuario);

        botonLogin.addActionListener(e -> {
            String nickname = textField_CampoNickname.getText().trim();
            String puerto = TextField_CampoPuerto.getText().trim();

            if (!nickname.isEmpty() && !puerto.isEmpty() && puerto.matches("\\d+")) {
                if (onLoginExitoso != null) {
                    dispose();
                    onLoginExitoso.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Debes ingresar un nickname y un puerto válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void setOnLoginExitoso(Runnable r) {
        this.onLoginExitoso = r;
    }

    public String getNickname() {
        return textField_CampoNickname.getText().trim();
    }

    public String getPuerto() {
        return TextField_CampoPuerto.getText().trim();
    }
} 




