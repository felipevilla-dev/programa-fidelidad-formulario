package com.fidelidad.programa.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelidad.programa.dto.MarcaDTO;
import com.fidelidad.programa.model.Marca;
import com.fidelidad.programa.repository.MarcaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Lógica de consulta del catálogo de marcas del programa de fidelidad.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public List<MarcaDTO> findAll() {
        return marcaRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private MarcaDTO toDTO(Marca marca) {
        return new MarcaDTO(marca.getId(), marca.getNombre());
    }
}
