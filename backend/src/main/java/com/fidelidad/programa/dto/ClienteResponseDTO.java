package com.fidelidad.programa.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cliente tal como lo devuelve la API tras registrarlo o consultarlo.
 *
 * <h2>De dónde salen departamento y país</h2>
 *
 * <p>La entidad {@code Cliente} NO tiene campos de departamento ni de país: solo guarda
 * la ciudad, para que no puedan quedar datos contradictorios (ver la explicación en
 * {@code Cliente}). Pero el formulario sí los muestra, así que la API los entrega
 * navegando la relación en el servicio, dentro de la transacción:
 *
 * <pre>
 *   ciudad        -> cliente.getCiudad()
 *   departamento  -> cliente.getCiudad().getDepartamento()
 *   pais          -> cliente.getCiudad().getDepartamento().getPais()
 * </pre>
 *
 * <p>Es decir, se guardan sin redundancia y se leen ya resueltos: el frontend recibe los
 * tres niveles sin tener que hacer consultas adicionales.
 *
 * <p>Las referencias van como DTO anidados en vez de como texto suelto, para que el
 * frontend reciba también los id y pueda, por ejemplo, precargar los desplegables si más
 * adelante se agrega una pantalla de edición.
 */
public record ClienteResponseDTO(
        Long id,
        TipoIdentificacionDTO tipoIdentificacion,
        String numeroIdentificacion,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String direccion,
        CiudadDTO ciudad,
        DepartamentoDTO departamento,
        PaisDTO pais,
        MarcaDTO marca,
        LocalDateTime fechaRegistro
) {
}
