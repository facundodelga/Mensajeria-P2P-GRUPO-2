package org.example.cliente.modelo;

import org.example.cliente.modelo.usuario.Contacto;
import org.example.cliente.modelo.usuario.Usuario;

/**
 * Clase que proporciona servicios relacionados con la agenda de contactos de un usuario.
 * Implementa la interfaz IAgenda.
 */
public class AgendaServicio implements IAgenda {
    private Usuario usuario;

    /**
     * Constructor de la clase AgendaServicio.
     * @param usuario El usuario al que pertenece la agenda.
     */
    public AgendaServicio(Usuario usuario){
        this.usuario = usuario;
    }

    /**
     * Añade un contacto a la lista de contactos del usuario.
     * @param contacto El contacto que se añadirá.
     */
    @Override
    public void addContacto(Contacto contacto) throws ContactoRepetidoException {
        if(usuario.getContactos().contains(contacto)){

            throw new ContactoRepetidoException("El contacto "+contacto.getNombre()+" ya existe.");
        }
        usuario.getContactos().add(contacto);
        System.out.println(contacto.toString());
    }

    /**
     * Busca un contacto en la lista de contactos del usuario por nombre.
     * @param contacto El contacto a buscar.
     * @return El contacto encontrado, o null si no se encuentra.
     */
    @Override
    public Contacto buscaNombreContacto(String nombreContacto) {
        for (Contacto c : usuario.getContactos()) {
            if (c.getNombre().equals(nombreContacto)) {
                return c;
            }
        }
        return null;
    }
}