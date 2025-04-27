package org.example.servidor;

import org.example.cliente.modelo.usuario.Contacto;

import java.io.Serializable;
import java.util.ArrayList;

public class DirectorioDTO implements Serializable {
    private ArrayList<Contacto> contactos;

    public DirectorioDTO() {
        this.contactos = new ArrayList<>();
    }

    public DirectorioDTO(ArrayList<Contacto> contactos) {
        this.contactos = contactos;
    }

    public ArrayList<Contacto> getContactos() {
        return contactos;
    }
}
