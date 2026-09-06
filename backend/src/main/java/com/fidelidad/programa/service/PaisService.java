package com.fidelidad.programa.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelidad.programa.dto.PaisDTO;
import com.fidelidad.programa.model.Pais;
import com.fidelidad.programa.repository.PaisRepository;

import lombok.RequiredArgsConstructor;

/**
 * Lógica de consulta del catálogo de países.
 *
 * <p>{@code @RequiredArgsConstructor} de Lombok genera el constructor con los campos
 * {@code final}, y Spring lo usa para inyectar el repositorio. Se prefiere la inyección
 * por constructor sobre {@code @Autowired} en el campo porque deja la dependencia
 * inmutable, hace explícito qué necesita la clase y permite crearla en un test sin
 * levantar el contexto de Spring.
 *
 * <p>{@code readOnly = true} le indica a Hibernate que no hace falta rastrear cambios en
 * las entidades cargadas, lo que ahorra trabajo en consultas puras.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaisService {

    private final PaisRepository paisRepository;

    /** Países ordenados alfabéticamente, para el desplegable del formulario. */
    public List<PaisDTO> findAll() {
        return paisRepository.findAllByOrderByNombreAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Convierte la entidad en DTO.
     *
     * <p>El mapeo es manual y a propósito: deja a la vista qué campos salen de la API y
     * evita exponer la entidad, que arrastra las relaciones perezosas de JPA.
     */
    private PaisDTO toDTO(Pais pais) {
        return new PaisDTO(pais.getId(), pais.getNombre());
    }
}
