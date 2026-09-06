package com.fidelidad.programa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fidelidad.programa.model.Departamento;

/**
 * Repositorio de departamentos.
 */
@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    /**
     * Departamentos de un país.
     *
     * <p>{@code findByPaisId} navega la relación: "Pais" es el campo {@code pais} de
     * la entidad e "Id" es el campo {@code id} dentro de ese país. Spring Data lo
     * traduce a {@code where d.pais.id = ?}, así que basta con el id y no hace falta
     * cargar el objeto Pais completo.
     */
    List<Departamento> findByPaisId(Long paisId);

    /** Igual que el anterior, pero ordenado para mostrarlo en el desplegable. */
    List<Departamento> findByPaisIdOrderByNombreAsc(Long paisId);
}
