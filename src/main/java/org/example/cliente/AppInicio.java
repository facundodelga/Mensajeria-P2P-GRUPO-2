package org.example.cliente;

import org.example.cliente.controlador.Controlador;
import org.example.cliente.vista.VentanaInicioSesion;
import org.example.cliente.vista.VentanaPrincipal; // Asegúrate de que esta clase exista y esté correctamente importada

import javax.swing.SwingUtilities; // ¡IMPORTANTE! Necesitas esto
import javax.swing.JOptionPane; // Útil para mensajes de error al inicio

public class AppInicio {
    public static final int PUERTO_SERVIDOR_DIRECTORIO = 8000;

    public static void main(String[] args) {
        // MUY IMPORTANTE: Todo el código que crea o manipula componentes Swing
        // DEBE EJECUTARSE EN EL EVENT DISPATCH THREAD (EDT).
        SwingUtilities.invokeLater(() -> {
            try {
                // Obtener la instancia del Controlador (Singleton)
                Controlador controlador = Controlador.getInstancia();

                // Crear las instancias de las vistas.
                // Es crucial que se creen DENTRO de este bloque invokeLater.
                VentanaInicioSesion vistaInicioSesion = new VentanaInicioSesion();
                VentanaPrincipal vistaPrincipal = new VentanaPrincipal();

                // Asignar las vistas al Controlador.
                // El controlador ahora tiene referencias a las ventanas.
                controlador.setVistaInicioSesion(vistaInicioSesion);
                controlador.setVistaPrincipal(vistaPrincipal);

                // Mostrar la primera ventana de la aplicación, que es la de inicio de sesión.
                // La VentanaPrincipal se hará visible más tarde, después de un inicio de sesión exitoso.
                vistaInicioSesion.mostrar();

            } catch (Exception e) {
                // Si ocurre CUALQUIER error durante la inicialización de la UI,
                // se capturará aquí. Esto es VITAL para depurar.
                System.err.println("¡ERROR FATAL AL INICIAR LA APLICACIÓN! Detalles a continuación:");
                e.printStackTrace(); // Imprime el rastro de la excepción para ver dónde falló.
                JOptionPane.showMessageDialog(null,
                        "Ha ocurrido un error inesperado al iniciar la aplicación.\nPor favor, consulte la consola.",
                        "Error Crítico de Inicio",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Forzar la salida de la aplicación si no puede iniciarse.
            }
        });
        // Cualquier código que pongas aquí FUERA del invokeLater
        // no debería interactuar con la UI, o podría causar problemas.
    }
}