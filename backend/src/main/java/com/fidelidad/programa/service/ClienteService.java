package com.fidelidad.programa.service;

import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelidad.programa.dto.CiudadDTO;
import com.fidelidad.programa.dto.ClienteRegistroDTO;
import com.fidelidad.programa.dto.ClienteResponseDTO;
import com.fidelidad.programa.dto.DepartamentoDTO;
import com.fidelidad.programa.dto.MarcaDTO;
import com.fidelidad.programa.dto.PaisDTO;
import com.fidelidad.programa.dto.TipoIdentificacionDTO;
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

import lombok.RequiredArgsConstructor;

/**
 * Lógica de negocio de la inscripción al programa de fidelidad.
 *
 * <p>Aquí viven las reglas que las anotaciones de Bean Validation no pueden comprobar.
 * La diferencia es importante:
 *
 * <ul>
 *   <li><b>Validación de formato</b> ({@code @NotBlank}, {@code @Past}): se decide mirando
 *       solo el dato que llegó. La hace el DTO y produce un 400.</li>
 *   <li><b>Validación de negocio</b> (¿existe esa ciudad?, ¿ya está inscrito?): necesita
 *       consultar la base de datos. La hace este servicio y produce un 404 o un 409.</li>
 *   <li><b>Validación de formato que depende de otro campo</b> (¿este número corresponde
 *       al tipo de documento elegido?): una anotación solo ve el campo que decora, así que
 *       también vive aquí, pero produce un 400 como cualquier error de validación.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    /**
     * Código del pasaporte en la tabla {@code tipo_identificacion}.
     *
     * <p>Es el único tipo del catálogo cuyo número admite letras.
     */
    private static final String CODIGO_PASAPORTE = "PA";

    /** Se compila una sola vez, en vez de en cada llamada a {@code String.matches}. */
    private static final Pattern SOLO_DIGITOS = Pattern.compile("^\\d+$");

    private final ClienteRepository clienteRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final CiudadRepository ciudadRepository;
    private final MarcaRepository marcaRepository;

    /**
     * Registra un cliente en el programa de fidelidad.
     *
     * <p>{@code @Transactional} (de escritura) hace que los pasos formen una sola unidad:
     * si algo falla a mitad de camino, no queda nada guardado a medias. Además mantiene
     * abierta la sesión de Hibernate, que es lo que permite navegar
     * Ciudad -&gt; Departamento -&gt; País al construir la respuesta; fuera de la transacción
     * eso lanzaría LazyInitializationException, porque la aplicación tiene
     * open-in-view=false.
     *
     * @throws RecursoNoEncontradoException si algún id del catálogo no existe (404)
     * @throws ClienteDuplicadoException    si el documento ya está inscrito (409)
     */
    @Transactional
    public ClienteResponseDTO registrarCliente(ClienteRegistroDTO dto) {

        // 1. Resolver las referencias. Se hace primero porque el resto del método necesita
        //    las entidades reales, y porque un id inexistente es un 404 que no tiene
        //    sentido seguir procesando.
        TipoIdentificacion tipoIdentificacion = tipoIdentificacionRepository
                .findById(dto.tipoIdentificacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Tipo de identificación", dto.tipoIdentificacionId()));

        Ciudad ciudad = ciudadRepository.findById(dto.ciudadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Ciudad", dto.ciudadId()));

        Marca marca = marcaRepository.findById(dto.marcaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Marca", dto.marcaId()));

        // 2. El formato del número depende del tipo, así que se comprueba aquí, donde
        //    ya se conoce el código del tipo.
        validarFormatoDelNumero(tipoIdentificacion, dto.numeroIdentificacion());

        // 3. Regla de negocio: un mismo documento no puede inscribirse dos veces EN LA
        //    MISMA MARCA. Sí puede estar en varias marcas del grupo, así que la marca
        //    forma parte de la comprobación.
        //    Se usa existsBy... (un EXISTS en SQL) en vez de traer el cliente completo,
        //    porque solo interesa saber si está.
        //    La restricción uk_cliente_identificacion_marca de la base sigue siendo
        //    necesaria como última defensa ante dos peticiones simultáneas; esta
        //    comprobación existe para devolver un mensaje claro en vez de un error de
        //    base de datos.
        if (clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacionAndMarca(
                tipoIdentificacion, dto.numeroIdentificacion(), marca)) {
            throw new ClienteDuplicadoException(
                    tipoIdentificacion.getCodigo(), dto.numeroIdentificacion(), marca.getNombre());
        }

        // 4. Mapear el DTO a la entidad y guardar.
        //    fechaRegistro no se asigna aquí a propósito: la entidad Cliente la marca con
        //    @CreationTimestamp, así que Hibernate la rellena al insertar y sobrescribiría
        //    cualquier valor puesto a mano. Mantenerlo en un solo sitio evita que las dos
        //    fuentes se contradigan.
        Cliente cliente = new Cliente();
        cliente.setTipoIdentificacion(tipoIdentificacion);
        cliente.setNumeroIdentificacion(dto.numeroIdentificacion());
        cliente.setNombres(dto.nombres());
        cliente.setApellidos(dto.apellidos());
        cliente.setFechaNacimiento(dto.fechaNacimiento());
        cliente.setDireccion(dto.direccion());
        cliente.setCiudad(ciudad);
        cliente.setMarca(marca);

        Cliente guardado = clienteRepository.save(cliente);

        return toResponseDTO(guardado);
    }

    /**
     * Comprueba que el número de identificación tenga el formato que corresponde a su
     * tipo de documento.
     *
     * <p>Casi todos los documentos colombianos son numéricos: cédula de ciudadanía, de
     * extranjería, tarjeta de identidad y NIT. El pasaporte es la excepción — uno
     * colombiano tiene la forma {@code AR123456}, con letras—, así que para ese tipo
     * basta con el formato alfanumérico que ya garantiza la anotación del DTO.
     *
     * <p>Esta regla no puede ser una anotación en {@code ClienteRegistroDTO} porque
     * depende de OTRO campo, y las anotaciones de Bean Validation solo ven el campo que
     * decoran. Además el DTO recibe el id del tipo, no su código: hay que resolverlo
     * contra la base de datos, y eso es trabajo de esta capa.
     */
    private void validarFormatoDelNumero(TipoIdentificacion tipo, String numeroIdentificacion) {
        if (CODIGO_PASAPORTE.equals(tipo.getCodigo())) {
            return;
        }

        if (!SOLO_DIGITOS.matcher(numeroIdentificacion).matches()) {
            // Se usa el código y no el nombre completo: "El número de Número de
            // Identificación Tributaria..." se lee mal, y el código es justo la etiqueta
            // corta que el usuario acaba de elegir en el desplegable.
            throw new DatoInvalidoException(
                    "numeroIdentificacion",
                    "El número de " + tipo.getCodigo() + " solo puede contener dígitos");
        }
    }

    /**
     * Consulta un cliente por su id.
     *
     * <p>{@code readOnly = true} porque solo lee; la transacción se mantiene abierta para
     * poder navegar la jerarquía geográfica al armar la respuesta.
     */
    /**
     * Devuelve una página de inscritos, opcionalmente filtrada por marca.
     *
     * <h3>Por qué se pagina y no se devuelve la lista entera</h3>
     *
     * <p>Un {@code List<Cliente>} sin límite funciona mientras haya veinte registros y tumba el
     * servidor cuando haya doscientos mil: la consulta los trae todos a memoria y la respuesta
     * JSON crece sin tope. Con {@link Pageable}, quien llama pide un tramo concreto
     * ({@code page}, {@code size}, {@code sort}) y la respuesta incluye además cuántos hay en
     * total, que es lo que el frontend necesita para dibujar la paginación.
     *
     * <p>El {@code map} se hace <b>dentro</b> de esta transacción, y no en el controlador,
     * porque el proyecto usa {@code spring.jpa.open-in-view=false}: fuera de aquí la sesión de
     * Hibernate ya está cerrada y recorrer las relaciones fallaría.
     *
     * @param marcaId  filtro opcional; si es {@code null} devuelve todas las marcas
     * @param pageable página, tamaño y orden solicitados
     */
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(Long marcaId, Pageable pageable) {
        Page<Cliente> pagina = (marcaId == null)
                ? clienteRepository.findAll(pageable)
                : clienteRepository.findByMarcaId(marcaId, pageable);

        return pagina.map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));

        return toResponseDTO(cliente);
    }

    /**
     * Convierte la entidad en la respuesta de la API.
     *
     * <p>Aquí está el punto clave del diseño del modelo: {@code Cliente} solo guarda la
     * ciudad, así que el departamento y el país se obtienen subiendo por la jerarquía.
     * De esa forma no hay datos duplicados en la tabla y aun así el frontend recibe los
     * tres niveles resueltos, sin tener que hacer consultas extra.
     *
     * <p>Este método se llama siempre dentro de una transacción: cada
     * {@code getDepartamento()} o {@code getPais()} dispara la carga perezosa de la
     * relación, y eso solo funciona con la sesión de Hibernate abierta.
     */
    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        Ciudad ciudad = cliente.getCiudad();
        Departamento departamento = ciudad.getDepartamento();
        Pais pais = departamento.getPais();

        TipoIdentificacion tipo = cliente.getTipoIdentificacion();
        Marca marca = cliente.getMarca();

        return new ClienteResponseDTO(
                cliente.getId(),
                new TipoIdentificacionDTO(tipo.getId(), tipo.getCodigo(), tipo.getNombre()),
                cliente.getNumeroIdentificacion(),
                cliente.getNombres(),
                cliente.getApellidos(),
                cliente.getFechaNacimiento(),
                cliente.getDireccion(),
                new CiudadDTO(ciudad.getId(), ciudad.getNombre()),
                new DepartamentoDTO(departamento.getId(), departamento.getNombre()),
                new PaisDTO(pais.getId(), pais.getNombre()),
                new MarcaDTO(marca.getId(), marca.getNombre()),
                cliente.getFechaRegistro());
    }
}
