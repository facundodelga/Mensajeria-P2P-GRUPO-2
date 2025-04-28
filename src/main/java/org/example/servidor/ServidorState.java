package org.example.servidor;

public interface ServidorState {
    void esperarConexiones();
    void cambiarEstado();
}
