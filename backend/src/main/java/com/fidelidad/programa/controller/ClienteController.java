package com.fidelidad.programa.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fidelidad.programa.dto.ClienteRegistroDTO;
import com.fidelidad.programa.dto.ClienteResponseDTO;
import com.fidelidad.programa.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de inscripción de clientes al programa de fidelidad.
 *
 * <h2>Por qué este controlador no tiene try/catch</h2>
 *
 * <p>Podría parecer que falta manejar los errores, pero no: de eso se encarga el
 * {@code GlobalExceptionHandler} de la capa {@code validation}, anotado con
 * {@code @RestControllerAdvice}. Spring lo coloca alrededor de todos los controladores, así
 * que cuando el servicio lanza {@code RecursoNoEncontradoException} o
 * {@code ClienteDuplicadoException}, la excepción sube sola y allí se traduce a 404 o 409.
 *
 * <p>Meter aquí un {@code try/catch} duplicaría esa lógica en cada método y en cada
 * controlador nuevo; bastaría olvidarlo una vez para devolverle al usuario un 500 sin
 * explicación. Con el manejador global, la traducción se escribe una sola vez y este
 * controlador solo describe el camino feliz.
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes",
        description = "Inscripción de clientes al programa de fidelidad")
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Registra un cliente en el programa.
     *
     * <h3>Por qué 201 Created y no 200 OK</h3>
     *
     * <p>200 OK solo dice "la petición salió bien". <b>201 Created</b> dice además que la
     * petición <b>creó un recurso nuevo</b>, y va acompañado de la cabecera {@code Location}
     * con la dirección de ese recurso. Es información que cualquier cliente HTTP entiende sin
     * tener que leer el cuerpo de la respuesta ni conocer nuestra API: sabe que antes no
     * existía nada y ahora sí, y dónde encontrarlo.
     *
     * <p>{@code @Valid} es lo que activa las validaciones declaradas en
     * {@link ClienteRegistroDTO}. Si alguna falla, Spring lanza
     * {@code MethodArgumentNotValidException} <b>antes</b> de ejecutar este método, y el
     * manejador global responde 400 con el detalle campo por campo. Sin esta anotación, las
     * validaciones del DTO sencillamente no se ejecutarían.
     *
     * @param dto datos del formulario
     * @return 201 Created con el cliente registrado y la cabecera Location
     */
    @Operation(
            summary = "Registrar un cliente",
            description = """
                    Inscribe a una persona en una de las seis marcas del grupo.

                    Una misma persona puede inscribirse en varias marcas, pero no dos \
                    veces en la misma. El número de documento solo admite dígitos, \
                    salvo en el pasaporte (código PA), que también acepta letras.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Cliente registrado. La cabecera Location apunta al recurso"),
            @ApiResponse(responseCode = "400",
                    description = "Datos inválidos. El campo 'errores' detalla qué falló",
                    content = @Content()),
            @ApiResponse(responseCode = "404",
                    description = "El tipo de documento, la ciudad o la marca no existen",
                    content = @Content()),
            @ApiResponse(responseCode = "409",
                    description = "El documento ya está inscrito en esa marca",
                    content = @Content())
    })
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> registrar(
            @Valid @RequestBody ClienteRegistroDTO dto) {

        ClienteResponseDTO creado = clienteService.registrarCliente(dto);

        return ResponseEntity
                .created(URI.create("/api/clientes/" + creado.id()))
                .body(creado);
    }

    /**
     * Consulta un cliente por su id.
     *
     * <p>Es el recurso al que apunta la cabecera {@code Location} del POST: la convención REST
     * espera que esa dirección se pueda consultar. Si el id no existe, el servicio lanza
     * {@code RecursoNoEncontradoException} y el manejador global responde 404.
     *
     * @return 200 OK con el cliente
     */
    @Operation(
            summary = "Consultar un cliente por su id",
            description = "Es el recurso al que apunta la cabecera Location devuelta por el POST.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un cliente con ese id",
                    content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }
}
