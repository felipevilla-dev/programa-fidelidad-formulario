package com.fidelidad.programa.validation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo centralizado de errores de toda la API.
 *
 * <h2>Qué es un {@code @RestControllerAdvice}</h2>
 *
 * <p>Es una clase que Spring coloca "alrededor" de todos los controladores. Cuando un
 * método de un controlador —o cualquier cosa que él llame, como un servicio— lanza una
 * excepción y no la atrapa, Spring busca aquí un método anotado con
 * {@code @ExceptionHandler} para ese tipo de excepción y usa lo que devuelva como
 * respuesta HTTP.
 *
 * <h2>Por qué evita repetir try/catch</h2>
 *
 * <p>Sin esta clase, cada endpoint tendría que envolver su lógica en algo así:
 *
 * <pre>
 *   try {
 *       return ResponseEntity.ok(clienteService.registrarCliente(dto));
 *   } catch (RecursoNoEncontradoException e) {
 *       return ResponseEntity.status(404).body(...);
 *   } catch (ClienteDuplicadoException e) {
 *       return ResponseEntity.status(409).body(...);
 *   }
 * </pre>
 *
 * <p>Ese bloque se repetiría en cada método y en cada controlador nuevo, y bastaría con
 * olvidarlo una vez para que el cliente recibiera un 500 sin explicación. Con el advice,
 * la traducción excepción → código HTTP se escribe **una sola vez** y aplica a todos los
 * endpoints, presentes y futuros. Los controladores quedan limpios: solo describen el
 * camino feliz.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Fallos de las anotaciones de Bean Validation del {@code ClienteRegistroDTO}.
     *
     * <p>Spring lanza esta excepción antes de entrar al método del controlador, cuando el
     * parámetro está marcado con {@code @Valid}. Se recorren todos los errores para
     * devolver el detalle de cada campo de una vez, en lugar de obligar al usuario a
     * corregir uno, reenviar, y descubrir el siguiente.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidacion(MethodArgumentNotValidException ex) {
        // LinkedHashMap conserva el orden en que se detectaron los errores.
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        ErrorResponseDTO cuerpo = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Hay errores de validación en los datos enviados",
                errores);

        return ResponseEntity.badRequest().body(cuerpo);
    }

    /** Un id enviado no existe en el catálogo: 404 Not Found. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        ErrorResponseDTO cuerpo = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cuerpo);
    }

    /**
     * Un campo es inválido por una regla que Bean Validation no puede expresar: 400.
     *
     * <p>Se devuelve con la misma forma que un fallo de las anotaciones —incluido el
     * mapa {@code errores} con el campo culpable— para que el frontend no tenga que
     * distinguir de dónde vino la validación.
     */
    @ExceptionHandler(DatoInvalidoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarDatoInvalido(DatoInvalidoException ex) {
        ErrorResponseDTO cuerpo = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Hay errores de validación en los datos enviados",
                Map.of(ex.getCampo(), ex.getMessage()));

        return ResponseEntity.badRequest().body(cuerpo);
    }

    /**
     * El cuerpo de la petición no se puede leer: 400 Bad Request.
     *
     * <p>Cubre un JSON mal formado, con codificación inválida o con un tipo que no
     * encaja (por ejemplo {@code "ciudadId": "Medellín"} donde se espera un número).
     *
     * <p>Sin este manejador el caso caería en la red de seguridad y devolvería un 500,
     * que es engañoso: 500 significa "el servidor falló", y aquí quien envió algo
     * incorrecto fue el cliente. No se expone el detalle de Jackson porque describe la
     * estructura interna de las clases.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de petición ilegible: {}", ex.getMessage());

        ErrorResponseDTO cuerpo = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "El cuerpo de la petición no es un JSON válido o algún campo tiene un tipo incorrecto");

        return ResponseEntity.badRequest().body(cuerpo);
    }

    /**
     * El documento ya está inscrito en esa marca: 409 Conflict.
     *
     * <p>409 y no 400 porque los datos enviados son correctos; lo que choca es el estado
     * actual de la base de datos.
     *
     * <p>La respuesta incluye además el mapa {@code errores} con el campo culpable, igual
     * que hace un 400. Así el frontend puede pintar el mensaje justo debajo del número de
     * identificación —el campo que hay que corregir— sin tener que interpretar el texto.
     */
    @ExceptionHandler(ClienteDuplicadoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarDuplicado(ClienteDuplicadoException ex) {
        ErrorResponseDTO cuerpo = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                Map.of(ex.getCampo(), ex.getMessage()));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
    }

    /**
     * Violación de una restricción de la base de datos: también 409.
     *
     * <p>Cubre la carrera entre dos peticiones simultáneas con el mismo documento: ambas
     * pueden pasar la comprobación de duplicado antes de que cualquiera guarde, y la
     * segunda choca contra la restricción uk_cliente_identificacion. Sin este manejador
     * ese caso terminaría en un 500 confuso.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad en la base de datos", ex);

        ErrorResponseDTO cuerpo = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "La operación viola una restricción de integridad de la base de datos");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
    }

    /**
     * Red de seguridad para cualquier error no previsto: 500.
     *
     * <p>La traza completa va al log del servidor, pero NO a la respuesta: exponerla
     * filtraría detalles internos (rutas, versiones, estructura de la base) que le sirven
     * a un atacante.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado procesando la petición", ex);

        ErrorResponseDTO cuerpo = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocurrió un error inesperado. Intenta de nuevo más tarde.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(cuerpo);
    }
}
