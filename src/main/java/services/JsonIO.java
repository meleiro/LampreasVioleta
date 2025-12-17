package services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * Utilidad genérica para exportar/importar JSON usando Jackson.
 * - write(file, data): serializa cualquier objeto a JSON
 * - read(file, Class<T>): deserializa JSON a un tipo concreto
 */
public final class JsonIO {

    // ObjectMapper es el motor de Jackson: convierte Java <-> JSON
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // JSON "bonito" (pretty print)

    private JsonIO() { }

    /** Escribe un objeto Java como JSON en el fichero. */
    public static <T> void write(File file, T data) throws IOException {
        // Si el fichero está en una carpeta que no existe, la creamos
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();

        MAPPER.writeValue(file, data);
    }

    /** Lee un JSON desde fichero y lo convierte al tipo indicado. */
    public static <T> T read(File file, Class<T> type) throws IOException {
        return MAPPER.readValue(file, type);
    }
}
