package com.fidelidad.programa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fidelidad.programa.dto.CiudadDTO;
import com.fidelidad.programa.service.CiudadService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints del catálogo de ciudades.
 */
@RestController
@RequestMapping("/api/ciudades")
@RequiredArgsConstructor
public class CiudadController {

    private final CiudadService ciudadService;

    /**
     * Lista ciudades, opcionalmente filtradas por departamento.
     *
     * <p>Último eslabón de los desplegables en cascada. La ciudad seleccionada aquí es el
     * único dato geográfico que se guarda en el cliente: el departamento y el país se
     * derivan de ella.
     *
     * @param departamentoId filtro opcional por departamento
     * @return 200 OK con la lista de ciudades
     */
    @GetMapping
    public ResponseEntity<List<CiudadDTO>> listar(
            @RequestParam(required = false) Long departamentoId) {

        List<CiudadDTO> ciudades = (departamentoId == null)
                ? ciudadService.findAll()
                : ciudadService.findByDepartamentoId(departamentoId);

        return ResponseEntity.ok(ciudades);
    }
}
