package com.fidelidad.programa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fidelidad.programa.model.Marca;

/**
 * Repositorio de marcas del programa de fidelidad.
 */
@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    Optional<Marca> findByNombre(String nombre);

    /** Marcas ordenadas alfabéticamente, para el desplegable del formulario. */
    List<Marca> findAllByOrderByNombreAsc();
}
