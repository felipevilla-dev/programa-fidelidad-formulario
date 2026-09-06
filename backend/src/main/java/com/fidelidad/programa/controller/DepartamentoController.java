package com.fidelidad.programa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fidelidad.programa.dto.DepartamentoDTO;
import com.fidelidad.programa.service.DepartamentoService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints del catálogo de departamentos.
 */
@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    /**
     * Lista departamentos, opcionalmente filtrados por país.
     *
     * <p>Es el segundo eslabón de los desplegables en cascada: cuando el usuario elige un
     * país, el frontend pide {@code /api/departamentos?paisId=1} y llena la siguiente
     * lista.
     *
     * <p>{@code required = false} hace que el parámetro sea opcional: si no viene, Spring
     * pasa {@code null} y se devuelve el catálogo completo. Se usa {@code Long} y no
     * {@code long} justamente porque el tipo primitivo no puede ser nulo.
     *
     * @param paisId filtro opcional por país
     * @return 200 OK con la lista de departamentos
     */
    @GetMapping
    public ResponseEntity<List<DepartamentoDTO>> listar(
            @RequestParam(required = false) Long paisId) {

        List<DepartamentoDTO> departamentos = (paisId == null)
                ? departamentoService.findAll()
                : departamentoService.findByPaisId(paisId);

        return ResponseEntity.ok(departamentos);
    }
}
