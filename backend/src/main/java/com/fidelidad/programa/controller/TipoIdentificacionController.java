package com.fidelidad.programa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fidelidad.programa.dto.TipoIdentificacionDTO;
import com.fidelidad.programa.service.TipoIdentificacionService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints del catálogo de tipos de identificación (CC, CE, TI, PA, NIT).
 */
@RestController
@RequestMapping("/api/tipos-identificacion")
@RequiredArgsConstructor
public class TipoIdentificacionController {

    private final TipoIdentificacionService tipoIdentificacionService;

    /**
     * @return 200 OK con los tipos de documento, ordenados por código
     */
    @GetMapping
    public ResponseEntity<List<TipoIdentificacionDTO>> listar() {
        return ResponseEntity.ok(tipoIdentificacionService.findAll());
    }
}
