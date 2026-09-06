package com.fidelidad.programa.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que el formulario envía al registrar un cliente.
 *
 * <p>Las referencias viajan como id ({@code tipoIdentificacionId}, {@code ciudadId},
 * {@code marcaId}) y no como objetos completos: el frontend solo conoce el valor
 * seleccionado en cada desplegable, y es el backend quien resuelve esos id contra la
 * base de datos.
 *
 * <p>Las anotaciones son de {@code jakarta.validation} (Bean Validation), el paquete
 * que usa Spring Boot 3; en Spring Boot 2 era {@code javax.validation}. Se ejecutan
 * cuando el controlador marca el parámetro con {@code @Valid}: si alguna falla, Spring
 * lanza {@code MethodArgumentNotValidException} antes de entrar al método, y el
 * manejador global la convierte en una respuesta 400 con el detalle por campo.
 *
 * <p>Los {@code max} de {@code @Size} coinciden con la longitud de las columnas de la
 * entidad {@code Cliente}, para que el error se detecte como un 400 legible y no como
 * un fallo de la base de datos.
 */
public record ClienteRegistroDTO(

        @NotNull(message = "El tipo de identificación es obligatorio")
        Long tipoIdentificacionId,

        // Regla de FORMATO, común a todos los tipos: letras y números, sin espacios ni
        // símbolos como puntos o guiones.
        //
        // La regla de que casi todos los tipos admitan SOLO dígitos no puede vivir aquí:
        // depende del tipo elegido, y una anotación de campo no puede mirar otro campo.
        // Además el DTO solo conoce el id del tipo, no su código. Esa comprobación está
        // en ClienteService, que ya resuelve el tipo contra la base de datos.
        //
        // Se usa * y no + para que una cadena vacía la reporte @NotBlank con su mensaje,
        // en vez de duplicar dos errores sobre el mismo campo.
        @NotBlank(message = "El número de identificación es obligatorio")
        @Size(max = 20, message = "El número de identificación no puede superar 20 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9]*$",
                message = "El número de identificación no puede contener espacios ni símbolos")
        String numeroIdentificacion,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
        String apellidos,

        // @Past exige una fecha anterior a hoy: nadie puede haber nacido en el futuro.
        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion,

        @NotNull(message = "La ciudad es obligatoria")
        Long ciudadId,

        @NotNull(message = "La marca es obligatoria")
        Long marcaId
) {
}
