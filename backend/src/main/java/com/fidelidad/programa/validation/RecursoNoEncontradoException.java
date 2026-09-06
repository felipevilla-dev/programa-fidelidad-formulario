package com.fidelidad.programa.validation;

/**
 * Se lanza cuando un id enviado por el frontend no existe en la base de datos:
 * un tipo de identificación, una ciudad o una marca que no están en el catálogo.
 *
 * <p>Extiende {@link RuntimeException} (excepción "no verificada"), así los servicios
 * no tienen que declarar {@code throws} ni los controladores envolver las llamadas en
 * {@code try/catch}: la excepción sube sola por la pila hasta el
 * {@link GlobalExceptionHandler}, que decide el código HTTP.
 *
 * <p>El manejador global la traduce a <b>404 Not Found</b>.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Construye el mensaje a partir del recurso y el id buscado, para que el error diga
     * exactamente qué faltó. Ejemplo: {@code No se encontró Ciudad con id 9999}.
     *
     * @param recurso nombre legible del catálogo, por ejemplo "Ciudad" o "Marca"
     * @param id      identificador que no existe
     */
    public RecursoNoEncontradoException(String recurso, Long id) {
        super("No se encontró " + recurso + " con id " + id);
    }

    /** Variante para mensajes que no encajan en el formato recurso + id. */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
