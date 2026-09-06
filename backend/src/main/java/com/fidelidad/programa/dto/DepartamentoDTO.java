package com.fidelidad.programa.dto;

/**
 * Departamento para el desplegable del formulario.
 *
 * <p>Solo lleva {@code id} y {@code nombre}: el país al que pertenece no viaja porque
 * el frontend ya lo eligió antes (los desplegables van en cascada
 * país → departamento → ciudad) y enviarlo sería repetir un dato que el cliente ya tiene.
 */
public record DepartamentoDTO(Long id, String nombre) {
}
