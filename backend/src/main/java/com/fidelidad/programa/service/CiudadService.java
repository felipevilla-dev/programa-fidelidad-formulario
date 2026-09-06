package com.fidelidad.programa.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelidad.programa.dto.CiudadDTO;
import com.fidelidad.programa.model.Ciudad;
import com.fidelidad.programa.repository.CiudadRepository;

import lombok.RequiredArgsConstructor;

/**
 * Lógica de consulta del catálogo de ciudades.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CiudadService {

    private final CiudadRepository ciudadRepository;

    public List<CiudadDTO> findAll() {
        return ciudadRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Ciudades de un departamento: último eslabón de los desplegables en cascada.
     */
    public List<CiudadDTO> findByDepartamentoId(Long departamentoId) {
        return ciudadRepository.findByDepartamentoIdOrderByNombreAsc(departamentoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private CiudadDTO toDTO(Ciudad ciudad) {
        return new CiudadDTO(ciudad.getId(), ciudad.getNombre());
    }
}
