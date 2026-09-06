package com.fidelidad.programa.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelidad.programa.dto.TipoIdentificacionDTO;
import com.fidelidad.programa.model.TipoIdentificacion;
import com.fidelidad.programa.repository.TipoIdentificacionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Lógica de consulta del catálogo de tipos de identificación.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipoIdentificacionService {

    private final TipoIdentificacionRepository tipoIdentificacionRepository;

    /** Ordenados por código (CC, CE, NIT, PA, TI) para el desplegable. */
    public List<TipoIdentificacionDTO> findAll() {
        return tipoIdentificacionRepository.findAllByOrderByCodigoAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private TipoIdentificacionDTO toDTO(TipoIdentificacion tipo) {
        return new TipoIdentificacionDTO(tipo.getId(), tipo.getCodigo(), tipo.getNombre());
    }
}
