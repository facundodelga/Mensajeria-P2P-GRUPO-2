package org.example.vista;

import org.example.modelo.usuario.UsuarioDTO;

import java.util.Objects;

public class ChatPantalla {
    private UsuarioDTO contacto;
    private String nombre;

    public ChatPantalla(UsuarioDTO contacto) {
        this.contacto = contacto;
        this.nombre = contacto.getNombre();
    }
    public String getNombre() {
        return nombre;
    }
    public UsuarioDTO getContacto() {
        return contacto;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setPendiente(){
        this.nombre = contacto.getNombre() + "*";
    }

    public void setLeido(){
        this.nombre = contacto.getNombre();
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(contacto, nombre);
    }
}
