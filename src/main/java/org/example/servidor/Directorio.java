package org.example.servidor;

import org.example.cliente.modelo.usuario.Contacto;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Directorio implements IDirectorio {
    private  Map<String, Contacto> usuarios;


    public Directorio() {
        usuarios = new HashMap<>();

    }

    @Override
    public Map<String, Contacto> getUsuarios() {
        return usuarios;
    }

    @Override
    public void addUsuario(String nombre, Contacto usuario) {
        usuarios.put(nombre, usuario);
    }

    @Override
    public void removeUsuario(String nombre, Contacto usuario) {
        usuarios.remove(nombre, usuario);
    }

    public Contacto getContacto(String nombre) {
        return usuarios.get(nombre);
    }

    @Override
    public Directorio clonar() {
        Directorio copia = Clonador.deepClone(this);

        return copia;
    }

}