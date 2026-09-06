package com.fidelidad.programa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fidelidad.programa.dto.MarcaDTO;
import com.fidelidad.programa.service.MarcaService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints del catálogo de marcas del programa de fidelidad.
 */
@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private final MarcaService marcaService;

    /**
     * @return 200 OK con las seis marcas del programa, ordenadas alfabéticamente
     */
    @GetMapping
    public ResponseEntity<List<MarcaDTO>> listar() {
        return ResponseEntity.ok(marcaService.findAll());
    }
}
