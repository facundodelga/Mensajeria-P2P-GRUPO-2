package org.example.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class MensajeBubble extends JPanel {
    private final Mensaje mensaje;

    public MensajeBubble(Mensaje mensaje) {
        this.mensaje = mensaje;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = g2.getFontMetrics();
        int padding = 10;
        int maxAncho = 250;
        int textoDisponible = maxAncho - 2 * padding;

        // Medir líneas de texto
        String[] palabras = mensaje.getTexto().split(" ");
        StringBuilder linea = new StringBuilder();
        java.util.List<String> lineas = new java.util.ArrayList<>();

        for (String palabra : palabras) {
            if (fm.stringWidth(linea + palabra + " ") < textoDisponible) {
                linea.append(palabra).append(" ");
            } else {
                lineas.add(linea.toString());
                linea = new StringBuilder(palabra + " ");
            }
        }
        if (!linea.toString().isEmpty()) lineas.add(linea.toString());

        int burbujaAncho = maxAncho;
        int burbujaAlto = padding * 2 + lineas.size() * fm.getHeight();

        int x = mensaje.esMio() ? getWidth() - burbujaAncho : 0;
        int y = 0;

        // Fondo
        Color color = mensaje.esMio() ? new Color(37, 211, 102) : new Color(60, 60, 60);
        g2.setColor(color);
        g2.fillRoundRect(x, y, burbujaAncho, burbujaAlto, 20, 20);

        // Texto
        g2.setColor(Color.WHITE);
        int yTexto = y + padding + fm.getAscent();
        for (String l : lineas) {
            g2.drawString(l, x + padding, yTexto);
            yTexto += fm.getHeight();
        }

        // Hora
        g2.setFont(g2.getFont().deriveFont(10f));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(mensaje.getHora(), x + burbujaAncho - 50, y + burbujaAlto - 8);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int maxAncho = 250;
        int textoDisponible = maxAncho - 20;

        String[] palabras = mensaje.getTexto().split(" ");
        StringBuilder linea = new StringBuilder();
        int lineas = 1;

        for (String palabra : palabras) {
            if (fm.stringWidth(linea + palabra + " ") < textoDisponible) {
                linea.append(palabra).append(" ");
            } else {
                lineas++;
                linea = new StringBuilder(palabra + " ");
            }
        }

        int altura = fm.getHeight() * lineas + 20;
        return new Dimension(maxAncho, altura);
    }
}

