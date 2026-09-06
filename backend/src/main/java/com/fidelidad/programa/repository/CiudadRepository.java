package com.fidelidad.programa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fidelidad.programa.model.Ciudad;

/**
 * Repositorio de ciudades.
 */
@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Long> {

    /**
     * Ciudades de un departamento. Es la consulta que alimenta el desplegable de
     * ciudad una vez el usuario elige departamento (desplegables en cascada).
     */
    List<Ciudad> findByDepartamentoId(Long departamentoId);

    /** Igual que el anterior, ordenado alfabéticamente. */
    List<Ciudad> findByDepartamentoIdOrderByNombreAsc(Long departamentoId);
}
