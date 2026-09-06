package com.fidelidad.programa.dto;

/**
 * Tipo de documento para el desplegable del formulario.
 *
 * <p>Lleva los dos campos porque cumplen papeles distintos en la interfaz:
 * {@code codigo} es la etiqueta corta (CC, CE, TI, PA, NIT) y {@code nombre} el texto
 * completo que hace entendible la opción ("Cédula de Ciudadanía").
 */
public record TipoIdentificacionDTO(Long id, String codigo, String nombre) {
}
