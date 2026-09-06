package com.fidelidad.programa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fidelidad.programa.dto.PaisDTO;
import com.fidelidad.programa.service.PaisService;

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
public class PaisController {

    private final PaisService paisService;

    /**
     * Lista los países para el primer desplegable del formulario.
     *
     * @return 200 OK con la lista de países
     */
    @GetMapping
    public ResponseEntity<List<PaisDTO>> listar() {
        return ResponseEntity.ok(paisService.findAll());
    }
}
