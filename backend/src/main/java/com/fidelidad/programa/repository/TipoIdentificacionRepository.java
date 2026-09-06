package com.fidelidad.programa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fidelidad.programa.model.TipoIdentificacion;

/**
 * Repositorio de tipos de identificación (CC, CE, TI, PA, NIT).
 */
@Repository
public interface TipoIdentificacionRepository extends JpaRepository<TipoIdentificacion, Long> {

    /**
     * Busca por el código corto. Devuelve {@link Optional} porque el código puede no
     * existir; obliga a quien llama a decidir qué hacer en ese caso, en vez de
     * arriesgar un {@code null}.
     */
    Optional<TipoIdentificacion> findByCodigo(String codigo);

    /** Catálogo ordenado por código, para el desplegable del formulario. */
    List<TipoIdentificacion> findAllByOrderByCodigoAsc();
}
