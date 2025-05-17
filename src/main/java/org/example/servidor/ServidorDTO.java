package org.example.servidor;

import java.io.Serializable;

public class ServidorDTO implements Serializable {
    private IDirectorio directorio;
    private IColaMensajes colaMensajes;

    public ServidorDTO(IDirectorio directorio, IColaMensajes colaMensajes) {
        this.directorio = directorio;
        this.colaMensajes = colaMensajes;
    }

    public IDirectorio getDirectorio() {
        return directorio;
    }

    public void setDirectorio(IDirectorio directorio) {
        this.directorio = directorio;
    }

    public IColaMensajes getColaMensajes() {
        return colaMensajes;
    }

    public void setColaMensajes(IColaMensajes colaMensajes) {
        this.colaMensajes = colaMensajes;
    }
}
