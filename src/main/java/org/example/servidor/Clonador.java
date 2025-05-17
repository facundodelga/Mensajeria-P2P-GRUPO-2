package org.example.servidor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Clonador {
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setReferences(true); // Importante si hay referencias cíclicas
        kryo.setRegistrationRequired(false); // Para que no tengas que registrar todas las clases
    }

    @SuppressWarnings("unchecked")
    public static <T> T deepClone(T object) {
        kryo.setReferences(true); // importante para manejar ciclos

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, object);
        output.close();

        Input input = new Input(new ByteArrayInputStream(baos.toByteArray()));
        return (T) kryo.readClassAndObject(input);
    }
}
