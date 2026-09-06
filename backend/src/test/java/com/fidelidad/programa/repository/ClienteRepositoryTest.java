package com.fidelidad.programa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.fidelidad.programa.model.Ciudad;
import com.fidelidad.programa.model.Cliente;
import com.fidelidad.programa.model.Departamento;
import com.fidelidad.programa.model.Marca;
import com.fidelidad.programa.model.Pais;
import com.fidelidad.programa.model.TipoIdentificacion;

/**
 * Pruebas de {@link ClienteRepository} contra una base H2 real.
 *
 * <p>A diferencia de las pruebas del servicio, aquí sí hay SQL de por medio:
 * es lo único que permite comprobar dos cosas que Mockito no puede. La primera,
 * que Spring Data deriva correctamente la consulta a partir del nombre del
 * método. La segunda, que la restricción de unicidad existe de verdad en el
 * esquema y no solo en el código del servicio.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DisplayName("ClienteRepository")
class ClienteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClienteRepository clienteRepository;

    private TipoIdentificacion cedula;
    private Ciudad bogota;
    private Marca chevignon;
    private Marca americanino;

    private static final String DOCUMENTO = "1088123456";

    @BeforeEach
    void prepararCatalogos() {
        Pais colombia = entityManager.persist(new Pais(null, "Colombia"));
        Departamento cundinamarca =
                entityManager.persist(new Departamento(null, "Cundinamarca", colombia));
        bogota = entityManager.persist(new Ciudad(null, "Bogotá D.C.", cundinamarca));
        cedula = entityManager.persist(
                new TipoIdentificacion(null, "CC", "Cédula de Ciudadanía"));
        chevignon = entityManager.persist(new Marca(null, "Chevignon"));
        americanino = entityManager.persist(new Marca(null, "Americanino"));
    }

    private Cliente clienteEn(Marca marca) {
        Cliente cliente = new Cliente();
        cliente.setTipoIdentificacion(cedula);
        cliente.setNumeroIdentificacion(DOCUMENTO);
        cliente.setNombres("Ana María");
        cliente.setApellidos("Gómez Ruiz");
        cliente.setFechaNacimiento(LocalDate.of(1998, 4, 12));
        cliente.setDireccion("Calle 10 #5-20");
        cliente.setCiudad(bogota);
        cliente.setMarca(marca);
        return cliente;
    }

    // ---------------------------------------------------------------------

    @Test
    @DisplayName("detecta que el documento ya está inscrito en esa marca")
    void detectaElDuplicadoEnLaMismaMarca() {
        entityManager.persistAndFlush(clienteEn(chevignon));

        boolean existe = clienteRepository
                .existsByTipoIdentificacionAndNumeroIdentificacionAndMarca(
                        cedula, DOCUMENTO, chevignon);

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("no lo considera duplicado si la marca es distinta")
    void noEsDuplicadoEnOtraMarca() {
        entityManager.persistAndFlush(clienteEn(chevignon));

        boolean existe = clienteRepository
                .existsByTipoIdentificacionAndNumeroIdentificacionAndMarca(
                        cedula, DOCUMENTO, americanino);

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("una misma persona puede aparecer en varias marcas")
    void devuelveTodasLasInscripcionesDeUnaPersona() {
        entityManager.persistAndFlush(clienteEn(chevignon));
        entityManager.persistAndFlush(clienteEn(americanino));

        // Este método devuelve List y no Optional justamente por este caso: con
        // Optional, Spring Data lanzaría IncorrectResultSizeDataAccessException
        // en cuanto alguien se inscribiera en una segunda marca.
        List<Cliente> inscripciones = clienteRepository
                .findByTipoIdentificacionAndNumeroIdentificacion(cedula, DOCUMENTO);

        assertThat(inscripciones).hasSize(2);
        assertThat(inscripciones)
                .extracting(cliente -> cliente.getMarca().getNombre())
                .containsExactlyInAnyOrder("Chevignon", "Americanino");
    }

    @Test
    @DisplayName("la base rechaza el duplicado aunque el servicio no lo valide")
    void laRestriccionDeUnicidadExisteEnLaBase() {
        entityManager.persistAndFlush(clienteEn(chevignon));

        // Última línea de defensa: si dos peticiones simultáneas pasaran a la vez
        // la comprobación del servicio, la restricción uk_cliente_identificacion_marca
        // impide que se guarden las dos.
        assertThatThrownBy(() -> clienteRepository.saveAndFlush(clienteEn(chevignon)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("guarda la fecha de registro automáticamente")
    void asignaLaFechaDeRegistro() {
        Cliente guardado = clienteRepository.saveAndFlush(clienteEn(chevignon));

        // La pone @CreationTimestamp, no el servicio.
        assertThat(guardado.getFechaRegistro()).isNotNull();
    }
}
