package com.fidelidad.programa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    /**
     * Página de inscritos, ordenada según el {@link Pageable} recibido.
     *
     * <p>Redefine el {@code findAll} heredado de {@link JpaRepository} únicamente para
     * añadirle el grafo de carga; el comportamiento es el mismo.
     *
     * <h2>Por qué el @EntityGraph: el problema N+1</h2>
     *
     * <p>Todas las relaciones de {@code Cliente} son {@code LAZY}, así que por defecto una
     * consulta trae solo la fila del cliente y deja las relaciones sin cargar. Al construir el
     * DTO se recorre {@code ciudad → departamento → país}, más el tipo de documento y la marca,
     * y cada acceso dispara su propia consulta.
     *
     * <p>Con un solo cliente eso pasa desapercibido. Con una página entera se convierte en
     * <b>1 consulta para la lista + varias por cada cliente</b>: decenas de viajes a la base
     * para pintar una tabla. Es el clásico problema N+1.
     *
     * <p>{@code @EntityGraph} le dice a Hibernate que traiga esas relaciones <b>en la misma
     * consulta</b>, mediante JOIN. El resultado baja a <b>una sola consulta</b> para los datos,
     * más la del conteo que la paginación necesita aparte.
     *
     * <p>La diferencia está medida en {@code ClienteRepositoryTest.noHayConsultasNMasUno}, que
     * falla si alguien quita esta anotación: para diez clientes en diez ciudades distintas,
     * <b>34 consultas sin el grafo, 2 con él</b>.
     *
     * <p>Nótese que aquí solo se traen relaciones {@code @ManyToOne}. Si alguna fuera una
     * colección, Hibernate no podría paginar en SQL y tendría que hacerlo en memoria.
     */
    @Override
    @EntityGraph(attributePaths = {"tipoIdentificacion", "ciudad", "ciudad.departamento",
            "ciudad.departamento.pais", "marca"})
    Page<Cliente> findAll(Pageable pageable);

    /**
     * Página de inscritos de una marca concreta.
     *
     * <p>{@code findByMarcaId} navega la relación: Spring Data traduce el nombre del método a
     * un filtro por la columna {@code marca_id}, sin necesidad de cargar la marca primero.
     */
    @EntityGraph(attributePaths = {"tipoIdentificacion", "ciudad", "ciudad.departamento",
            "ciudad.departamento.pais", "marca"})
    Page<Cliente> findByMarcaId(Long marcaId, Pageable pageable);
}
