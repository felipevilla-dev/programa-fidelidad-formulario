package com.fidelidad.programa.validation;

import lombok.Getter;

/**
 * Se lanza cuando un campo es inválido por una regla que las anotaciones de Bean
 * Validation no pueden expresar.
 *
 * <p>Las anotaciones del DTO solo pueden mirar el campo que decoran. Cuando la regla
 * depende de otro campo —o de algo que hay que consultar en la base de datos— la
 * comprobación tiene que hacerse en el servicio, y esta excepción es la forma de
 * reportarla con el mismo formato.
 *
 * <p>El caso que la motiva: el número de identificación admite solo dígitos, salvo
 * cuando el tipo es Pasaporte, que también lleva letras. Saber cuál de las dos reglas
 * aplica exige conocer el código del tipo, y el DTO solo trae su id.
 *
 * <p>El manejador global la traduce a <b>400 Bad Request</b> con el campo culpable en
 * el mapa {@code errores}, igual que un fallo de validación normal. Para quien consume
 * la API, y para el formulario, es indistinguible de los demás errores de validación.
 */
@Getter
public class DatoInvalidoException extends RuntimeException {

    /** Campo del formulario al que corresponde el error. */
    private final String campo;

    public DatoInvalidoException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }
}
