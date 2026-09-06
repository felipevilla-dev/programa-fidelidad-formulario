package com.fidelidad.programa.validation;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato único de error de la API.
 *
 * <p>Que todos los errores tengan la misma forma le facilita la vida al frontend: puede
 * escribir un solo bloque que lea {@code message} y, si viene, recorra {@code errores}
 * para pintar el mensaje debajo de cada campo del formulario.
 *
 * <p>{@code @JsonInclude(NON_NULL)} omite del JSON los campos nulos, así el mapa
 * {@code errores} solo aparece en los errores de validación y no ensucia las demás
 * respuestas.
 *
 * @param timestamp momento en que ocurrió el error
 * @param status    código HTTP, por ejemplo 400
 * @param error     nombre del código HTTP, por ejemplo "Bad Request"
 * @param message   descripción legible del problema
 * @param errores   mapa campo -> mensaje; solo se llena en los errores de validación
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> errores
) {

    /** Error sin detalle por campo (404, 409, 500). */
    public ErrorResponseDTO(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message, null);
    }

    /** Error de validación, con el detalle de qué campo falló y por qué. */
    public ErrorResponseDTO(int status, String error, String message, Map<String, String> errores) {
        this(LocalDateTime.now(), status, error, message, errores);
    }
}
