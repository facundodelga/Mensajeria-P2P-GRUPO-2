package org.example.cliente.factory; // Nuevo paquete

import org.example.cliente.cifrado.ICifradorMensajes;
import org.example.cliente.modelo.mensaje.Mensaje;
import org.example.cliente.modelo.usuario.Contacto;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

public class CreadorMensaje {

    /**
     * Factory Method para crear un nuevo mensaje a enviar (texto plano que se cifrará).
     * @param emisor El Contacto del emisor.
     * @param receptor El Contacto del receptor.
     * @param textoPlano El contenido del mensaje en texto plano.
     * @param claveCifrado La clave secreta para cifrar.
     * @param cifrador La estrategia de cifrado a usar.
     * @return Un objeto Mensaje con el contenido ya cifrado y desencriptado temporalmente para uso.
     * @throws Exception Si ocurre un error durante el cifrado.
     */
    public Mensaje crearMensajeParaEnviar(Contacto emisor, Contacto receptor, String textoPlano,
                                          SecretKey claveCifrado, ICifradorMensajes cifrador) throws Exception {
        Mensaje nuevoMensaje = new Mensaje(textoPlano, emisor, receptor); // Constructor con texto plano

        byte[] contenidoCifrado = cifrador.encriptar(textoPlano, claveCifrado);
        nuevoMensaje.setContenidoCifrado(contenidoCifrado);
        // El contenido desencriptado se mantiene para mostrarlo inmediatamente al emisor
        nuevoMensaje.setContenidoDesencriptado(textoPlano);

        return nuevoMensaje;
    }

    /**
     * Factory Method para crear un mensaje a partir de datos cifrados recibidos o cargados de persistencia.
     * El contenido desencriptado se establecerá después.
     * @param id El ID único del mensaje.
     * @param fecha La fecha/hora del mensaje.
     * @param contenidoCifrado El contenido cifrado del mensaje (IV + texto cifrado + GCM tag).
     * @param emisor El Contacto del emisor.
     * @param receptor El Contacto del receptor.
     * @return Un objeto Mensaje listo para ser desencriptado.
     */
    public Mensaje crearMensajeCifradoRecibidoOCargado(String id, Date fecha, byte[] contenidoCifrado,
                                                       Contacto emisor, Contacto receptor) {
        return new Mensaje(id, fecha, contenidoCifrado, emisor, receptor);
    }
}