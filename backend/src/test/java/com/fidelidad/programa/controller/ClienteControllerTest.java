package com.fidelidad.programa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fidelidad.programa.dto.CiudadDTO;
import com.fidelidad.programa.dto.ClienteRegistroDTO;
import com.fidelidad.programa.dto.ClienteResponseDTO;
import com.fidelidad.programa.dto.DepartamentoDTO;
import com.fidelidad.programa.dto.MarcaDTO;
import com.fidelidad.programa.dto.PaisDTO;
import com.fidelidad.programa.dto.TipoIdentificacionDTO;
import com.fidelidad.programa.service.ClienteService;
import com.fidelidad.programa.validation.ClienteDuplicadoException;
import com.fidelidad.programa.validation.RecursoNoEncontradoException;

/**
 * Pruebas de la capa web de {@link ClienteController}.
 *
 * <p>{@code @WebMvcTest} levanta solo lo necesario para atender peticiones HTTP
 * —el controlador, la conversión JSON y el {@code GlobalExceptionHandler}—, sin
 * base de datos ni el resto de la aplicación. El servicio se sustituye por un
 * doble, porque aquí no se prueba la lógica de negocio (eso lo hace
 * {@code ClienteServiceTest}) sino la traducción entre HTTP y esa lógica:
 * qué código de estado sale, y qué forma tiene el cuerpo de la respuesta.
 */
@WebMvcTest(ClienteController.class)
@DisplayName("ClienteController")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteService clienteService;

    private ClienteRegistroDTO registroValido() {
        return new ClienteRegistroDTO(
                1L,
                "1088123456",
                "Ana María",
                "Gómez Ruiz",
                LocalDate.of(1998, 4, 12),
                "Calle 10 #5-20",
                14L,
                3L);
    }

    private ClienteResponseDTO respuestaDeEjemplo() {
        return new ClienteResponseDTO(
                99L,
                new TipoIdentificacionDTO(1L, "CC", "Cédula de Ciudadanía"),
                "1088123456",
                "Ana María",
                "Gómez Ruiz",
                LocalDate.of(1998, 4, 12),
                "Calle 10 #5-20",
                new CiudadDTO(14L, "Bogotá D.C."),
                new DepartamentoDTO(14L, "Cundinamarca"),
                new PaisDTO(1L, "Colombia"),
                new MarcaDTO(3L, "Chevignon"),
                LocalDateTime.of(2026, 9, 6, 12, 0));
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    // ---------------------------------------------------------------------

    @Test
    @DisplayName("POST con datos válidos devuelve 201 y la cabecera Location")
    void postValidoDevuelve201() throws Exception {
        when(clienteService.registrarCliente(any())).thenReturn(respuestaDeEjemplo());

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registroValido())))
                .andExpect(status().isCreated())
                // 201 debe indicar dónde quedó el recurso creado.
                .andExpect(header().string("Location", "/api/clientes/99"))
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.marca.nombre").value("Chevignon"))
                .andExpect(jsonPath("$.pais.nombre").value("Colombia"));
    }

    @Test
    @DisplayName("POST con campos vacíos devuelve 400 y un mensaje por campo")
    void postInvalidoDevuelve400() throws Exception {
        ClienteRegistroDTO invalido = new ClienteRegistroDTO(
                null, "", "", "Gómez", LocalDate.of(1998, 4, 12), "", 14L, null);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalido)))
                .andExpect(status().isBadRequest())
                // El frontend pinta el error debajo de cada campo usando este mapa.
                .andExpect(jsonPath("$.errores.numeroIdentificacion").exists())
                .andExpect(jsonPath("$.errores.nombres").exists())
                .andExpect(jsonPath("$.errores.tipoIdentificacionId").exists())
                .andExpect(jsonPath("$.errores.marcaId").exists());

        // Bean Validation corta antes: el servicio no llega a ejecutarse.
        verify(clienteService, never()).registrarCliente(any());
    }

    @Test
    @DisplayName("POST de un documento ya inscrito en esa marca devuelve 409")
    void postDuplicadoDevuelve409() throws Exception {
        when(clienteService.registrarCliente(any()))
                .thenThrow(new ClienteDuplicadoException("CC", "1088123456", "Chevignon"));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registroValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.numeroIdentificacion").exists());
    }

    @Test
    @DisplayName("POST con JSON mal formado devuelve 400, no 500")
    void postConJsonRotoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroIdentificacion\": "))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET de un id existente devuelve 200")
    void getExistenteDevuelve200() throws Exception {
        when(clienteService.buscarPorId(99L)).thenReturn(respuestaDeEjemplo());

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroIdentificacion").value("1088123456"));
    }

    @Test
    @DisplayName("GET de un id inexistente devuelve 404")
    void getInexistenteDevuelve404() throws Exception {
        when(clienteService.buscarPorId(404L))
                .thenThrow(new RecursoNoEncontradoException("Cliente", 404L));

        mockMvc.perform(get("/api/clientes/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
