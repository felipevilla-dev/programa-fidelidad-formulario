package com.fidelidad.programa.validation;

import lombok.Getter;

/**
 * Se lanza cuando ese documento ya está inscrito <b>en la marca elegida</b>.
 *
 * <p>La regla del programa es una inscripción por marca: la misma persona puede
 * vincularse a Chevignon y también a Esprit, pero no dos veces a Chevignon.
 *
 * <p>Es una regla de negocio, no un error de formato: los campos que llegaron son
 * válidos, lo que ocurre es que chocan con el estado actual de la base. Por eso el
 * manejador global la traduce a <b>409 Conflict</b> y no a un 400.
 */
@Getter
public class ClienteDuplicadoException extends RuntimeException {

    /**
     * Campo del formulario al que corresponde el error.
     *
     * <p>Permite que el manejador global devuelva el mensaje también en el mapa
     * {@code errores}, y que el frontend lo pinte justo debajo del campo que hay que
     * corregir en vez de dejarlo solo en un aviso general.
     */
    private static final String CAMPO = "numeroIdentificacion";

    private final String campo = CAMPO;

    public ClienteDuplicadoException(String codigoTipo, String numeroIdentificacion, String marca) {
        super("El documento " + codigoTipo + " " + numeroIdentificacion
                + " ya está registrado en " + marca);
    }
}
