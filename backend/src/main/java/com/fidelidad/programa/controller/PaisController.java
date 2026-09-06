package com.fidelidad.programa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fidelidad.programa.dto.PaisDTO;
import com.fidelidad.programa.service.PaisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints del catálogo de países.
 *
 * <p>El prefijo de la URL se declara una sola vez en {@code @RequestMapping} de la clase,
 * y cada método solo añade lo que le falta. Así, si mañana la ruta base cambiara, se
 * tocaría un único punto.
 *
 * <p>El controlador no tiene lógica: recibe la petición, llama al servicio y devuelve el
 * resultado. Toda la lógica de negocio vive en la capa {@code service}.
 */
@RestController
@RequestMapping("/api/paises")
@RequiredArgsConstructor
@Tag(name = "Catálogo: países")
public class PaisController {

    private final PaisService paisService;

    /**
     * Lista los países para el primer desplegable del formulario.
     *
     * @return 200 OK con la lista de países
     */
    @Operation(summary = "Listar los países",
            description = "Alimenta el primer desplegable del formulario.")
    @GetMapping
    public ResponseEntity<List<PaisDTO>> listar() {
        return ResponseEntity.ok(paisService.findAll());
    }
}
