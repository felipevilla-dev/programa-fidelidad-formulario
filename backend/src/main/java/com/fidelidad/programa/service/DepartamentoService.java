package com.fidelidad.programa.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelidad.programa.dto.DepartamentoDTO;
import com.fidelidad.programa.model.Departamento;
import com.fidelidad.programa.repository.DepartamentoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Lógica de consulta del catálogo de departamentos.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public List<DepartamentoDTO> findAll() {
        return departamentoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Departamentos de un país. Es el segundo eslabón de los desplegables en cascada:
     * el usuario elige país y esta consulta llena la lista de departamentos.
     */
    public List<DepartamentoDTO> findByPaisId(Long paisId) {
        return departamentoRepository.findByPaisIdOrderByNombreAsc(paisId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private DepartamentoDTO toDTO(Departamento departamento) {
        return new DepartamentoDTO(departamento.getId(), departamento.getNombre());
    }
}
