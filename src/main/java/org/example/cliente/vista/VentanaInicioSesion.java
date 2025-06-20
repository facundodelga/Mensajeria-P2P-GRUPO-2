package org.example.cliente.vista;

import org.example.cliente.controlador.Controlador;

import java.awt.*;
import javax.swing.*;

/**
 * Clase que representa la ventana de inicio de sesión.
 * Extiende JFrame e implementa la interfaz IVistaInicioSesion.
 */
public class VentanaInicioSesion extends JFrame implements IVistaInicioSesion {

    private JTextField textField_CampoNombre;
    private JTextField TextField_CampoPuerto;
    private JButton botonLogin;
    private JComboBox<String> comboFormato;

    /**
     * Constructor de la clase VentanaInicioSesion.
     * Configura la interfaz gráfica de la ventana de inicio de sesión.
     */
    public VentanaInicioSesion() {
        setBackground(new Color(32, 32, 32));
        setTitle("Inicio de Sesión");
        setSize(350, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(61, 61, 61));
        getContentPane().setLayout(null);

        JLabel Label_Nickname = new JLabel("Nombre de Usuario:");
        Label_Nickname.setBounds(82, 111, 100, 25);
        Label_Nickname.setFont(new Font("Tahoma", Font.PLAIN, 14));
        Label_Nickname.setForeground(Color.WHITE);
        getContentPane().add(Label_Nickname);

        textField_CampoNombre = new JTextField();
        textField_CampoNombre.setBounds(82, 150, 180, 25);
        getContentPane().add(textField_CampoNombre);

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
        botonLogin.setActionCommand("Iniciar");
        botonLogin.addActionListener(Controlador.getInstancia());

        JLabel Label_ConfigurarUsuario = new JLabel("Configurar Usuario");
        Label_ConfigurarUsuario.setBounds(91, 31, 171, 25);
        Label_ConfigurarUsuario.setForeground(new Color(255, 255, 255));
        Label_ConfigurarUsuario.setFont(new Font("Tahoma", Font.PLAIN, 20));
        getContentPane().add(Label_ConfigurarUsuario);

        JLabel Label_Formato = new JLabel("Formato de almacenamiento:");
        Label_Formato.setBounds(82, 260, 180, 25);
        Label_Formato.setFont(new Font("Tahoma", Font.PLAIN, 14));
        Label_Formato.setForeground(Color.WHITE);
        getContentPane().add(Label_Formato);

        String[] formatos = {"TXT", "XML", "JSON"};
        comboFormato = new JComboBox<>(formatos);
        comboFormato.setBounds(82, 290, 180, 25);
        getContentPane().add(comboFormato);

        // Reposicionar botón login para no superponerse
        botonLogin.setBounds(102, 330, 130, 30);

        setVisible(true);
    }

    /**
     * Oculta la ventana de inicio de sesión.
     */
    @Override
    public void ocultar() {
        dispose();
    }

    /**
     * Obtiene el nombre de usuario ingresado.
     * @return El nombre de usuario.
     */
    public String getNombre() {
        return textField_CampoNombre.getText().trim();
    }

    /**
     * Obtiene el puerto ingresado.
     * @return El puerto.
     */
    public String getPuerto() {
        return TextField_CampoPuerto.getText().trim();
    }

    @Override
    public void mostrar() {
        setVisible(true);
    }

    public String getFormatoSeleccionado() {
        return (String) comboFormato.getSelectedItem();
    }
}