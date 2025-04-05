package org.example.vista;

    import org.example.modelo.usuario.UsuarioDTO;

    import java.util.Objects;

    /**
     * Clase que representa la pantalla de chat para un contacto específico.
     */
    public class ChatPantalla {
        private UsuarioDTO contacto;
        private String nombre;

        /**
         * Constructor de la clase ChatPantalla.
         * @param contacto El contacto asociado a esta pantalla de chat.
         */
        public ChatPantalla(UsuarioDTO contacto) {
            this.contacto = contacto;
            this.nombre = contacto.getNombre();
        }

        /**
         * Obtiene el nombre del contacto.
         * @return El nombre del contacto.
         */
        public String getNombre() {
            return nombre;
        }

        /**
         * Obtiene el contacto asociado a esta pantalla de chat.
         * @return El contacto asociado.
         */
        public UsuarioDTO getContacto() {
            return contacto;
        }

        /**
         * Establece el nombre del contacto.
         * @param nombre El nuevo nombre del contacto.
         */
        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        /**
         * Marca el contacto como pendiente añadiendo un asterisco al nombre.
         */
        public void setPendiente(){
            this.nombre = contacto.getNombre() + "*";
        }

        /**
         * Marca el contacto como leído eliminando el asterisco del nombre.
         */
        public void setLeido(){
            this.nombre = contacto.getNombre();
        }

        /**
         * Compara este objeto con otro para determinar si son iguales.
         * @param o El objeto a comparar.
         * @return true si los objetos son iguales, false en caso contrario.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UsuarioDTO that = ((ChatPantalla) o).contacto;
            UsuarioDTO esteContacto = this.contacto;
            System.out.println(esteContacto);
            System.out.println(that);
            System.out.println("equals CHAT PANTALLA" + esteContacto.equals(that));
            return esteContacto.equals(that);
        }

        /**
         * Calcula el código hash de este objeto.
         * @return El código hash de este objeto.
         */
        @Override
        public int hashCode() {
            return Objects.hash(contacto, nombre);
        }
    }