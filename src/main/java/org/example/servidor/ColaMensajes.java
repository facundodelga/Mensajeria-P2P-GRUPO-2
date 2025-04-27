package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;

import java.util.ArrayList;
import java.util.HashMap;

public class ColaMensajes implements IColaMensajes {
    private ArrayList<Mensaje> mensajesRecibidos;

    public ColaMensajes(){
        mensajesRecibidos=new ArrayList<>();
    }


    @Override
    public void agregarMensajePendiente() {

    }

    public ArrayList<Mensaje> getMensajesRecibidos() {
        return mensajesRecibidos;
    }
}
