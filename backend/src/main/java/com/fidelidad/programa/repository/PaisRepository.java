package com.fidelidad.programa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fidelidad.programa.model.Pais;

/**
 * Repositorio de países.
 *
 * <p>Al extender {@link JpaRepository} se heredan gratis {@code findAll()},
 * {@code findById()}, {@code save()}, {@code deleteById()}, paginación y más.
 * Spring Data crea la implementación en tiempo de ejecución: no hay que escribir
 * una clase que implemente esta interfaz.
 *
 * <p>Los dos parámetros genéricos son la entidad y el tipo de su clave primaria.
 */
@Repository
public interface PaisRepository extends JpaRepository<Pais, Long> {

    /**
     * Consulta derivada: Spring Data lee el nombre del método ("findBy" + "Nombre")
     * y genera el SQL {@code select ... from pais where nombre = ?}. No hace falta
     * escribir la consulta.
     */
    Optional<Pais> findByNombre(String nombre);

    /** Países ordenados alfabéticamente, para el desplegable del formulario. */
    List<Pais> findAllByOrderByNombreAsc();
}
