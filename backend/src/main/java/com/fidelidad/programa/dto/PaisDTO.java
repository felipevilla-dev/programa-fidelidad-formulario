package com.fidelidad.programa.dto;

/**
 * País tal como lo necesita el desplegable del formulario.
 *
 * <p>Es un {@code record} de Java 17: una clase inmutable en la que el compilador
 * genera el constructor, los accesores ({@code id()}, {@code nombre()}),
 * {@code equals}, {@code hashCode} y {@code toString}.
 *
 * <p>Las entidades JPA no pueden ser records porque Hibernate necesita construirlas
 * sin argumentos y modificarlas por reflexión. Un DTO es lo contrario: un dato de
 * solo lectura que viaja una vez hacia el frontend y no vuelve a cambiar, así que la
 * inmutabilidad es una ventaja y no un estorbo.
 */
public record PaisDTO(Long id, String nombre) {
}
