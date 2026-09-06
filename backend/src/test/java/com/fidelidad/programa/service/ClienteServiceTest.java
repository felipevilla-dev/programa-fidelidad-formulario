package com.fidelidad.programa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fidelidad.programa.dto.ClienteRegistroDTO;
import com.fidelidad.programa.dto.ClienteResponseDTO;
import com.fidelidad.programa.model.Ciudad;
import com.fidelidad.programa.model.Cliente;
import com.fidelidad.programa.model.Departamento;
import com.fidelidad.programa.model.Marca;
import com.fidelidad.programa.model.Pais;
import com.fidelidad.programa.model.TipoIdentificacion;
import com.fidelidad.programa.repository.CiudadRepository;
import com.fidelidad.programa.repository.ClienteRepository;
import com.fidelidad.programa.repository.MarcaRepository;
import com.fidelidad.programa.repository.TipoIdentificacionRepository;
import com.fidelidad.programa.validation.ClienteDuplicadoException;
import com.fidelidad.programa.validation.DatoInvalidoException;
import com.fidelidad.programa.validation.RecursoNoEncontradoException;

/**
 * Pruebas de las reglas de negocio de {@link ClienteService}.
 *
 * <p>Son pruebas <strong>unitarias</strong>: los repositorios se sustituyen por
 * dobles de Mockito, así que no hay base de datos de por medio y cada caso corre
 * en milisegundos. Lo que se verifica aquí no es que el SQL funcione, sino que
 * las decisiones del servicio sean las correctas.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    @Mock
    private CiudadRepository ciudadRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @InjectMocks
    private ClienteService clienteService;

    // ---------------------------------------------------------------------
    // Datos de apoyo
    // ---------------------------------------------------------------------

    private static final Long ID_TIPO = 1L;
    private static final Long ID_CIUDAD = 14L;
    private static final Long ID_MARCA = 3L;
    private static final String DOCUMENTO = "1088123456";

    private TipoIdentificacion tipo(String codigo, String nombre) {
        return new TipoIdentificacion(ID_TIPO, codigo, nombre);
    }

    /** Ciudad con toda su cadena hacia arriba: el DTO de respuesta la recorre entera. */
    private Ciudad ciudadBogota() {
        Pais colombia = new Pais(1L, "Colombia");
        Departamento cundinamarca = new Departamento(14L, "Cundinamarca", colombia);
        return new Ciudad(ID_CIUDAD, "Bogotá D.C.", cundinamarca);
    }

    private Marca marca(String nombre) {
        return new Marca(ID_MARCA, nombre);
    }

    private ClienteRegistroDTO registroCon(String numeroIdentificacion) {
        return new ClienteRegistroDTO(
                ID_TIPO,
                numeroIdentificacion,
                "Ana María",
                "Gómez Ruiz",
                LocalDate.of(1998, 4, 12),
                "Calle 10 #5-20",
                ID_CIUDAD,
                ID_MARCA);
    }

    /** Deja los tres catálogos resueltos, que es el punto de partida habitual. */
    private void dadosLosCatalogos(TipoIdentificacion tipo, Marca marca) {
        when(tipoIdentificacionRepository.findById(ID_TIPO)).thenReturn(Optional.of(tipo));
        when(ciudadRepository.findById(ID_CIUDAD)).thenReturn(Optional.of(ciudadBogota()));
        when(marcaRepository.findById(ID_MARCA)).thenReturn(Optional.of(marca));
    }

    /** Simula el guardado: la base asigna el id y la marca de tiempo. */
    private void dadoQueGuardaCorrectamente() {
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacion -> {
            Cliente aGuardar = invocacion.getArgument(0);
            aGuardar.setId(99L);
            aGuardar.setFechaRegistro(LocalDateTime.now());
            return aGuardar;
        });
    }

    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Registro correcto")
    class RegistroCorrecto {

        @Test
        @DisplayName("guarda el cliente y devuelve el DTO con país y departamento resueltos")
        void registraYDevuelveElDTOCompleto() {
            dadosLosCatalogos(tipo("CC", "Cédula de Ciudadanía"), marca("Chevignon"));
            when(clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacionAndMarca(
                    any(), eq(DOCUMENTO), any())).thenReturn(false);
            dadoQueGuardaCorrectamente();

            ClienteResponseDTO respuesta = clienteService.registrarCliente(registroCon(DOCUMENTO));

            assertThat(respuesta.id()).isEqualTo(99L);
            assertThat(respuesta.numeroIdentificacion()).isEqualTo(DOCUMENTO);
            assertThat(respuesta.marca().nombre()).isEqualTo("Chevignon");
            // El cliente solo guarda ciudad_id: departamento y país se derivan navegando.
            assertThat(respuesta.ciudad().nombre()).isEqualTo("Bogotá D.C.");
            assertThat(respuesta.departamento().nombre()).isEqualTo("Cundinamarca");
            assertThat(respuesta.pais().nombre()).isEqualTo("Colombia");
        }
    }

    @Nested
    @DisplayName("Documento único por marca")
    class DocumentoUnicoPorMarca {

        @Test
        @DisplayName("rechaza el mismo documento en la misma marca")
        void rechazaDuplicadoEnLaMismaMarca() {
            dadosLosCatalogos(tipo("CC", "Cédula de Ciudadanía"), marca("Chevignon"));
            when(clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacionAndMarca(
                    any(), eq(DOCUMENTO), any())).thenReturn(true);

            assertThatThrownBy(() -> clienteService.registrarCliente(registroCon(DOCUMENTO)))
                    .isInstanceOf(ClienteDuplicadoException.class)
                    .hasMessageContaining(DOCUMENTO)
                    .hasMessageContaining("Chevignon");

            // La regla debe cortar antes de tocar la base.
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("permite el mismo documento en otra marca")
        void permiteElMismoDocumentoEnOtraMarca() {
            // Esta es la razón de ser de la restricción por (tipo, número, marca):
            // una persona puede estar inscrita en varias marcas del grupo.
            dadosLosCatalogos(tipo("CC", "Cédula de Ciudadanía"), marca("Americanino"));
            when(clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacionAndMarca(
                    any(), eq(DOCUMENTO), any())).thenReturn(false);
            dadoQueGuardaCorrectamente();

            ClienteResponseDTO respuesta = clienteService.registrarCliente(registroCon(DOCUMENTO));

            assertThat(respuesta.marca().nombre()).isEqualTo("Americanino");
            verify(clienteRepository).save(any(Cliente.class));
        }
    }

    @Nested
    @DisplayName("Formato del número según el tipo de documento")
    class FormatoDelNumero {

        @Test
        @DisplayName("la cédula no admite letras")
        void laCedulaNoAdmiteLetras() {
            when(tipoIdentificacionRepository.findById(ID_TIPO))
                    .thenReturn(Optional.of(tipo("CC", "Cédula de Ciudadanía")));
            when(ciudadRepository.findById(ID_CIUDAD)).thenReturn(Optional.of(ciudadBogota()));
            when(marcaRepository.findById(ID_MARCA)).thenReturn(Optional.of(marca("Esprit")));

            assertThatThrownBy(() -> clienteService.registrarCliente(registroCon("12AB34")))
                    .isInstanceOf(DatoInvalidoException.class)
                    .hasMessageContaining("CC")
                    .hasFieldOrPropertyWithValue("campo", "numeroIdentificacion");

            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("el pasaporte sí admite letras")
        void elPasaporteAdmiteLetras() {
            dadosLosCatalogos(tipo("PA", "Pasaporte"), marca("Rifle"));
            when(clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacionAndMarca(
                    any(), eq("AR123456"), any())).thenReturn(false);
            dadoQueGuardaCorrectamente();

            ClienteResponseDTO respuesta = clienteService.registrarCliente(registroCon("AR123456"));

            assertThat(respuesta.numeroIdentificacion()).isEqualTo("AR123456");
        }

        @Test
        @DisplayName("el NIT tampoco admite letras, y el mensaje nombra el código")
        void elNitNoAdmiteLetras() {
            when(tipoIdentificacionRepository.findById(ID_TIPO))
                    .thenReturn(Optional.of(tipo("NIT", "Número de Identificación Tributaria")));
            when(ciudadRepository.findById(ID_CIUDAD)).thenReturn(Optional.of(ciudadBogota()));
            when(marcaRepository.findById(ID_MARCA)).thenReturn(Optional.of(marca("Naf Naf")));

            assertThatThrownBy(() -> clienteService.registrarCliente(registroCon("900A123")))
                    .isInstanceOf(DatoInvalidoException.class)
                    // Usa el código corto, no el nombre largo: "El número de NIT ...".
                    .hasMessageContaining("NIT")
                    .hasMessageNotContaining("Número de Identificación Tributaria");
        }
    }

    @Nested
    @DisplayName("Referencias inexistentes")
    class ReferenciasInexistentes {

        @Test
        @DisplayName("falla si el tipo de identificación no existe")
        void fallaSiNoExisteElTipo() {
            when(tipoIdentificacionRepository.findById(ID_TIPO)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.registrarCliente(registroCon(DOCUMENTO)))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("Tipo de identificación");
        }

        @Test
        @DisplayName("falla si la ciudad no existe")
        void fallaSiNoExisteLaCiudad() {
            when(tipoIdentificacionRepository.findById(ID_TIPO))
                    .thenReturn(Optional.of(tipo("CC", "Cédula de Ciudadanía")));
            when(ciudadRepository.findById(ID_CIUDAD)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.registrarCliente(registroCon(DOCUMENTO)))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("Ciudad");
        }

        @Test
        @DisplayName("falla si la marca no existe")
        void fallaSiNoExisteLaMarca() {
            when(tipoIdentificacionRepository.findById(ID_TIPO))
                    .thenReturn(Optional.of(tipo("CC", "Cédula de Ciudadanía")));
            when(ciudadRepository.findById(ID_CIUDAD)).thenReturn(Optional.of(ciudadBogota()));
            when(marcaRepository.findById(ID_MARCA)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.registrarCliente(registroCon(DOCUMENTO)))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("Marca");
        }
    }

    @Nested
    @DisplayName("Consulta por id")
    class ConsultaPorId {

        @Test
        @DisplayName("devuelve el cliente cuando existe")
        void devuelveElClienteCuandoExiste() {
            Cliente cliente = new Cliente();
            cliente.setId(7L);
            cliente.setTipoIdentificacion(tipo("CC", "Cédula de Ciudadanía"));
            cliente.setNumeroIdentificacion(DOCUMENTO);
            cliente.setNombres("Ana María");
            cliente.setApellidos("Gómez Ruiz");
            cliente.setFechaNacimiento(LocalDate.of(1998, 4, 12));
            cliente.setDireccion("Calle 10 #5-20");
            cliente.setCiudad(ciudadBogota());
            cliente.setMarca(marca("Chevignon"));
            when(clienteRepository.findById(7L)).thenReturn(Optional.of(cliente));

            ClienteResponseDTO respuesta = clienteService.buscarPorId(7L);

            assertThat(respuesta.id()).isEqualTo(7L);
            assertThat(respuesta.pais().nombre()).isEqualTo("Colombia");
        }

        @Test
        @DisplayName("falla cuando el cliente no existe")
        void fallaCuandoNoExiste() {
            when(clienteRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clienteService.buscarPorId(404L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("Cliente");
        }
    }
}
