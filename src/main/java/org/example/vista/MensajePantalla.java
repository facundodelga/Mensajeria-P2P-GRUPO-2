package org.example.vista;

    import org.example.modelo.mensaje.Mensaje;

    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;

    /**
     * Clase que representa un mensaje en la pantalla.
     */
    public class MensajePantalla {
        private String texto;
        private boolean esMio;
        private String hora;

        /**
         * Constructor de la clase MensajePantalla.
         * @param texto El contenido del mensaje.
         * @param esMio Indica si el mensaje es del usuario actual.
         * @param hora La hora en que se envió o recibió el mensaje.
         */
        public MensajePantalla(String texto, boolean esMio, String hora) {
            this.texto = texto;
            this.esMio = esMio;
            this.hora = hora;
        }

        /**
         * Obtiene el texto del mensaje.
         * @return El texto del mensaje.
         */
        public String getTexto() {
            return texto;
        }

        /**
         * Indica si el mensaje es del usuario actual.
         * @return true si el mensaje es del usuario actual, false en caso contrario.
         */
        public boolean esMio() {
            return esMio;
        }

        /**
         * Obtiene la hora en que se envió o recibió el mensaje.
         * @return La hora del mensaje.
         */
        public String getHora() {
            return hora;
        }

        /**
         * Convierte un objeto Mensaje a un objeto MensajePantalla.
         * @param mensaje El objeto Mensaje a convertir.
         * @param esMio Indica si el mensaje es del usuario actual.
         * @param hora La hora en que se envió o recibió el mensaje.
         * @return Un nuevo objeto MensajePantalla.
         */
        public static MensajePantalla mensajeToMensajePantalla(Mensaje mensaje, boolean esMio, String hora) {
            return new MensajePantalla(mensaje.getContenido(), esMio, hora);
        }
    }