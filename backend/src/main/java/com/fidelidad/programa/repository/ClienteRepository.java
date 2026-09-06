package com.fidelidad.programa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fidelidad.programa.model.Cliente;
import com.fidelidad.programa.model.Marca;
import com.fidelidad.programa.model.TipoIdentificacion;

/**
 * Repositorio de clientes inscritos.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Indica si ese documento ya está inscrito <b>en esa marca</b>.
     *
     * <p>Es la comprobación que respalda la regla de negocio: una misma persona puede
     * inscribirse en varias marcas del grupo, pero no dos veces en la misma. Por eso
     * la marca forma parte de la consulta.
     *
     * <p>El servicio la usa para rechazar el duplicado con un mensaje claro antes de
     * que salte la restricción {@code uk_cliente_identificacion_marca} de la base. Esa
     * restricción sigue siendo necesaria como última defensa ante dos peticiones
     * simultáneas con el mismo documento y la misma marca.
     *
     * <p>{@code existsBy...} genera un {@code EXISTS} en SQL: es más barato que traer
     * el cliente completo solo para saber si está.
     */
    boolean existsByTipoIdentificacionAndNumeroIdentificacionAndMarca(
            TipoIdentificacion tipoIdentificacion, String numeroIdentificacion, Marca marca);

    /**
     * Recupera la inscripción de un documento en una marca concreta.
     *
     * <p>Devuelve {@link Optional} porque la combinación de los tres campos sí es
     * única: como mucho hay una fila.
     */
    Optional<Cliente> findByTipoIdentificacionAndNumeroIdentificacionAndMarca(
            TipoIdentificacion tipoIdentificacion, String numeroIdentificacion, Marca marca);

    /**
     * Todas las inscripciones de un documento, en cualquier marca.
     *
     * <p>Devuelve una {@link List} y no un {@code Optional} justamente porque ahora la
     * misma persona puede tener varias inscripciones, una por marca. Con
     * {@code Optional}, Spring Data lanzaría una excepción en cuanto alguien se
     * inscribiera en su segunda marca.
     */
    List<Cliente> findByTipoIdentificacionAndNumeroIdentificacion(
            TipoIdentificacion tipoIdentificacion, String numeroIdentificacion);
}
