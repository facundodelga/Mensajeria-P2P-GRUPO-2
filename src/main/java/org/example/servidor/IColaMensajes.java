package org.example.servidor;

import org.example.cliente.modelo.mensaje.Mensaje;

import java.util.ArrayList;

public interface IColaMensajes {
    void agregarMensajePendiente();
    ArrayList<Mensaje> getMensajesRecibidos();



}
