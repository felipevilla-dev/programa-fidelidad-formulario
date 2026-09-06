package com.fidelidad.programa.dto;

/**
 * Ciudad para el desplegable del formulario.
 *
 * <p>Es el último nivel de la cascada: el {@code id} que el usuario seleccione aquí es
 * el único dato geográfico que se guardará en {@code Cliente}.
 */
public record CiudadDTO(Long id, String nombre) {
}
