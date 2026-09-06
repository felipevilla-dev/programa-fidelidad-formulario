import { useEffect, useState } from 'react'

import {
  obtenerCiudadesPorDepartamento,
  obtenerDepartamentosPorPais,
  obtenerMarcas,
  obtenerPaises,
  obtenerTiposIdentificacion,
  registrarCliente,
} from '../api/fidelidadApi'
import {
  CODIGO_PASAPORTE,
  filtrarNumeroIdentificacion,
  validarFormulario,
} from '../utils/validacion'
import TarjetaInscripcion from './TarjetaInscripcion'

/**
 * Valores iniciales del formulario.
 *
 * Se declaran fuera del componente para poder reutilizarlos al limpiar el
 * formulario tras un registro exitoso, sin duplicar el objeto.
 *
 * paisId y departamentoId existen SOLO en el frontend, para filtrar los
 * desplegables en cascada: no se envían al backend, que deduce ambos a partir de
 * la ciudad navegando Ciudad -> Departamento -> País.
 */
const FORMULARIO_VACIO = {
  tipoIdentificacionId: '',
  numeroIdentificacion: '',
  nombres: '',
  apellidos: '',
  fechaNacimiento: '',
  direccion: '',
  paisId: '',
  departamentoId: '',
  ciudadId: '',
  marcaId: '',
}

export default function FormularioInscripcion() {
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO)

  // Listas que alimentan los desplegables.
  const [tiposIdentificacion, setTiposIdentificacion] = useState([])
  const [paises, setPaises] = useState([])
  const [departamentos, setDepartamentos] = useState([])
  const [ciudades, setCiudades] = useState([])
  const [marcas, setMarcas] = useState([])

  // Mapa { campo: mensaje }. Se llena con la validación del cliente o con los
  // errores por campo que devuelve el backend en un 400.
  const [errores, setErrores] = useState({})

  // idle | enviando | exito | error
  const [estadoEnvio, setEstadoEnvio] = useState('idle')
  const [mensajeGeneral, setMensajeGeneral] = useState('')

  // El cliente que devolvió el backend tras un registro correcto. Mientras exista,
  // se muestra el carnet en lugar del formulario.
  const [clienteInscrito, setClienteInscrito] = useState(null)

  // Una bandera por petición. Se podría deducir el estado mirando si la lista está
  // vacía, pero sería ambiguo: una lista vacía también puede significar que la
  // petición falló. Un booleano explícito distingue "esperando" de "no hay datos".
  const [cargandoCatalogos, setCargandoCatalogos] = useState(true)
  const [cargandoDepartamentos, setCargandoDepartamentos] = useState(false)
  const [cargandoCiudades, setCargandoCiudades] = useState(false)

  // Código (CC, CE, TI, PA, NIT) del tipo elegido. Se deriva durante el render a
  // partir del id que guarda el formulario, en vez de duplicarlo en otro estado que
  // habría que mantener sincronizado.
  const codigoTipoSeleccionado = tiposIdentificacion.find(
    (tipo) => String(tipo.id) === formulario.tipoIdentificacionId,
  )?.codigo
  const esPasaporte = codigoTipoSeleccionado === CODIGO_PASAPORTE

  /**
   * Al montar: se cargan los tres catálogos que no dependen de ninguna selección.
   *
   * Promise.all lanza las tres peticiones a la vez en lugar de una tras otra, así
   * el formulario queda utilizable en el tiempo de la más lenta y no en la suma.
   */
  useEffect(() => {
    async function cargarCatalogosIniciales() {
      try {
        const [tipos, listaPaises, listaMarcas] = await Promise.all([
          obtenerTiposIdentificacion(),
          obtenerPaises(),
          obtenerMarcas(),
        ])
        setTiposIdentificacion(tipos)
        setPaises(listaPaises)
        setMarcas(listaMarcas)
      } catch {
        setEstadoEnvio('error')
        setMensajeGeneral(
          'No se pudieron cargar las listas del formulario. Verifica que el servidor esté disponible.',
        )
      } finally {
        // finally y no al final del try: la bandera debe bajar tanto si la carga
        // funcionó como si falló, o los desplegables quedarían diciendo "Cargando…"
        // para siempre.
        setCargandoCatalogos(false)
      }
    }

    cargarCatalogosIniciales()
  }, [])

  /**
   * Cascada, primer eslabón: al cambiar el país se cargan sus departamentos.
   *
   * El efecto solo se ocupa de traer datos del servidor. Vaciar las listas es
   * consecuencia de una acción del usuario, así que se hace en manejarCambio:
   * llamar a setState dentro de un efecto provoca un render extra innecesario.
   */
  useEffect(() => {
    if (!formulario.paisId) return

    async function cargarDepartamentos() {
      setCargandoDepartamentos(true)
      try {
        setDepartamentos(await obtenerDepartamentosPorPais(formulario.paisId))
      } catch {
        setDepartamentos([])
      } finally {
        setCargandoDepartamentos(false)
      }
    }

    cargarDepartamentos()
  }, [formulario.paisId])

  /** Cascada, segundo eslabón: al cambiar el departamento se cargan sus ciudades. */
  useEffect(() => {
    if (!formulario.departamentoId) return

    async function cargarCiudades() {
      setCargandoCiudades(true)
      try {
        setCiudades(await obtenerCiudadesPorDepartamento(formulario.departamentoId))
      } catch {
        setCiudades([])
      } finally {
        setCargandoCiudades(false)
      }
    }

    cargarCiudades()
  }, [formulario.departamentoId])

  /**
   * Actualiza un campo del formulario.
   *
   * La parte delicada es el RESETEO EN CASCADA: al cambiar el país se limpian
   * departamento y ciudad, y al cambiar el departamento se limpia la ciudad.
   *
   * Sin esto, alguien podría elegir Antioquia y Medellín, cambiar el país, y
   * quedarse con "Medellín" seleccionado en un país donde esa ciudad no existe.
   * El formulario mostraría una cosa y enviaría un ciudadId que no corresponde a
   * lo que el usuario cree haber elegido.
   */
  function manejarCambio(evento) {
    const { name } = evento.target
    let { value } = evento.target

    // El número de identificación se filtra aquí, sobre el valor que llega, para que
    // la restricción valga tanto al teclear como al pegar texto; un atributo pattern
    // del HTML no impediría el pegado, solo marcaría el campo como inválido después.
    // Qué caracteres se admiten depende del tipo: el pasaporte lleva letras.
    if (name === 'numeroIdentificacion') {
      value = filtrarNumeroIdentificacion(value, codigoTipoSeleccionado)
    }

    setFormulario((anterior) => {
      const actualizado = { ...anterior, [name]: value }

      // Al cambiar el tipo de documento hay que volver a filtrar el número ya escrito:
      // si alguien teclea un pasaporte con letras y luego cambia a cédula, esas letras
      // dejan de ser válidas y quedarían en pantalla como un dato que el servidor
      // rechazaría.
      if (name === 'tipoIdentificacionId') {
        const nuevoCodigo = tiposIdentificacion.find((t) => String(t.id) === value)?.codigo
        actualizado.numeroIdentificacion = filtrarNumeroIdentificacion(
          anterior.numeroIdentificacion,
          nuevoCodigo,
        )
      }

      if (name === 'paisId') {
        actualizado.departamentoId = ''
        actualizado.ciudadId = ''
        // Se vacían también las listas, no solo la selección: si no, el
        // desplegable seguiría mostrando los departamentos del país anterior
        // durante el instante que tarda la nueva petición.
        setDepartamentos([])
        setCiudades([])
      } else if (name === 'departamentoId') {
        actualizado.ciudadId = ''
        setCiudades([])
      }

      return actualizado
    })

    // Al corregir un campo se borra su mensaje de error: mantenerlo mientras el
    // usuario ya está escribiendo la corrección resulta confuso.
    setErrores((anteriores) => {
      if (!anteriores[name]) return anteriores
      const { [name]: _, ...resto } = anteriores
      return resto
    })
  }

  async function manejarEnvio(evento) {
    evento.preventDefault()

    const erroresDetectados = validarFormulario(formulario, codigoTipoSeleccionado)

    // Si hay errores no se llama al servidor: se muestran bajo cada campo.
    if (Object.keys(erroresDetectados).length > 0) {
      setErrores(erroresDetectados)
      setEstadoEnvio('error')
      setMensajeGeneral('Revisa los campos marcados antes de continuar.')
      return
    }

    setErrores({})
    setEstadoEnvio('enviando')
    setMensajeGeneral('')

    // El payload lleva SOLO los ocho campos de ClienteRegistroDTO. paisId y
    // departamentoId se quedan fuera: el backend los deduce desde la ciudad, y
    // enviarlos provocaría un error de campo desconocido.
    // Los id se convierten a número porque el value de un <select> siempre es
    // texto, y el DTO espera Long.
    const payload = {
      tipoIdentificacionId: Number(formulario.tipoIdentificacionId),
      numeroIdentificacion: formulario.numeroIdentificacion.trim(),
      nombres: formulario.nombres.trim(),
      apellidos: formulario.apellidos.trim(),
      fechaNacimiento: formulario.fechaNacimiento,
      direccion: formulario.direccion.trim(),
      ciudadId: Number(formulario.ciudadId),
      marcaId: Number(formulario.marcaId),
    }

    try {
      const cliente = await registrarCliente(payload)

      setEstadoEnvio('exito')
      setMensajeGeneral('')
      // Guardar el cliente cambia la vista al carnet; el formulario deja de verse.
      setClienteInscrito(cliente)
      // Se limpia el formulario para permitir una inscripción nueva.
      setFormulario(FORMULARIO_VACIO)
      setDepartamentos([])
      setCiudades([])
    } catch (error) {
      manejarErrorDeRegistro(error)
    }
  }

  /**
   * Traduce el error de axios al mensaje que ve el usuario.
   *
   * `error.response` solo existe cuando el servidor llegó a responder. Si no
   * existe, el fallo es de red: servidor apagado, sin conexión o CORS bloqueado.
   */
  function manejarErrorDeRegistro(error) {
    setEstadoEnvio('error')

    const respuesta = error.response

    if (!respuesta) {
      setMensajeGeneral(
        'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.',
      )
      return
    }

    // El backend devuelve un ErrorResponseDTO con un mapa { campo: mensaje } tanto en
    // el 400 (validación de formato) como en el 409 (documento ya inscrito en esa
    // marca). Como la validación del cliente usa exactamente esa misma forma, el mapa
    // se vuelca tal cual en el estado y cada mensaje aparece bajo su campo, sin
    // ninguna traducción intermedia.
    if (respuesta.data?.errores) {
      setErrores(respuesta.data.errores)
    }

    if (respuesta.status === 400) {
      setMensajeGeneral('Revisa los campos marcados: el servidor rechazó algunos datos.')
      return
    }

    // 409: regla de negocio, no error de formato. El backend manda un mensaje ya
    // legible ("El documento CC 123... ya está registrado en Chevignon"), así que se
    // muestra tal cual, además de bajo el campo del número de identificación.
    if (respuesta.status === 409) {
      setMensajeGeneral(
        respuesta.data?.message ||
          'Ese documento ya está inscrito en la marca seleccionada.',
      )
      return
    }

    setMensajeGeneral('Ocurrió un error al procesar la inscripción. Inténtalo de nuevo más tarde.')
  }

  /** Vuelve al formulario, ya vacío, para inscribir a alguien más. */
  function inscribirOtro() {
    setClienteInscrito(null)
    setEstadoEnvio('idle')
    setErrores({})
  }

  // Tras un registro correcto, el carnet ocupa el lugar del formulario: en ese
  // momento lo único relevante es lo que acaba de pasar.
  if (clienteInscrito) {
    return <TarjetaInscripcion cliente={clienteInscrito} alInscribirOtro={inscribirOtro} />
  }

  return (
    <form className="formulario" onSubmit={manejarEnvio} noValidate>
      <div className="campos">
        <Campo id="tipoIdentificacionId" etiqueta="Tipo de identificación" error={errores.tipoIdentificacionId}>
          <select
            id="tipoIdentificacionId"
            name="tipoIdentificacionId"
            value={formulario.tipoIdentificacionId}
            onChange={manejarCambio}
            disabled={cargandoCatalogos}
          >
            <option value="">{cargandoCatalogos ? 'Cargando…' : 'Selecciona una opción'}</option>
            {tiposIdentificacion.map((tipo) => (
              <option key={tipo.id} value={tipo.id}>
                {tipo.codigo} — {tipo.nombre}
              </option>
            ))}
          </select>
        </Campo>

        <Campo id="numeroIdentificacion" etiqueta="Número de identificación" error={errores.numeroIdentificacion}>
          <input
            id="numeroIdentificacion"
            name="numeroIdentificacion"
            type="text"
            // inputMode numeric abre el teclado numérico en el móvil, pero solo cuando
            // el documento es numérico: para un pasaporte hace falta el teclado normal.
            // Se mantiene type="text" porque type="number" añade flechas de incremento
            // y permite notación como "1e5", que no tiene sentido en un documento.
            inputMode={esPasaporte ? 'text' : 'numeric'}
            maxLength={20}
            value={formulario.numeroIdentificacion}
            onChange={manejarCambio}
          />
        </Campo>

        <Campo id="nombres" etiqueta="Nombres" error={errores.nombres}>
          <input
            id="nombres"
            name="nombres"
            type="text"
            maxLength={100}
            value={formulario.nombres}
            onChange={manejarCambio}
          />
        </Campo>

        <Campo id="apellidos" etiqueta="Apellidos" error={errores.apellidos}>
          <input
            id="apellidos"
            name="apellidos"
            type="text"
            maxLength={100}
            value={formulario.apellidos}
            onChange={manejarCambio}
          />
        </Campo>

        <Campo id="fechaNacimiento" etiqueta="Fecha de nacimiento" error={errores.fechaNacimiento}>
          <input
            id="fechaNacimiento"
            name="fechaNacimiento"
            type="date"
            value={formulario.fechaNacimiento}
            onChange={manejarCambio}
          />
        </Campo>

        <Campo id="marcaId" etiqueta="Marca" error={errores.marcaId}>
          <select
            id="marcaId"
            name="marcaId"
            value={formulario.marcaId}
            onChange={manejarCambio}
            disabled={cargandoCatalogos}
          >
            <option value="">{cargandoCatalogos ? 'Cargando…' : 'Selecciona una marca'}</option>
            {marcas.map((marca) => (
              <option key={marca.id} value={marca.id}>
                {marca.nombre}
              </option>
            ))}
          </select>
        </Campo>

        <Campo id="direccion" etiqueta="Dirección" error={errores.direccion} ancho="completo">
          <input
            id="direccion"
            name="direccion"
            type="text"
            maxLength={200}
            value={formulario.direccion}
            onChange={manejarCambio}
          />
        </Campo>

        <Campo id="paisId" etiqueta="País" error={errores.paisId}>
          <select
            id="paisId"
            name="paisId"
            value={formulario.paisId}
            onChange={manejarCambio}
            disabled={cargandoCatalogos}
          >
            <option value="">{cargandoCatalogos ? 'Cargando…' : 'Selecciona un país'}</option>
            {paises.map((pais) => (
              <option key={pais.id} value={pais.id}>
                {pais.nombre}
              </option>
            ))}
          </select>
        </Campo>

        {/* Deshabilitado hasta que haya país: sin él la lista está vacía. */}
        <Campo id="departamentoId" etiqueta="Departamento" error={errores.departamentoId}>
          <select
            id="departamentoId"
            name="departamentoId"
            value={formulario.departamentoId}
            onChange={manejarCambio}
            disabled={!formulario.paisId || cargandoDepartamentos}
          >
            <option value="">
              {!formulario.paisId
                ? 'Elige primero un país'
                : cargandoDepartamentos
                  ? 'Cargando…'
                  : 'Selecciona un departamento'}
            </option>
            {departamentos.map((departamento) => (
              <option key={departamento.id} value={departamento.id}>
                {departamento.nombre}
              </option>
            ))}
          </select>
        </Campo>

        <Campo id="ciudadId" etiqueta="Ciudad" error={errores.ciudadId}>
          <select
            id="ciudadId"
            name="ciudadId"
            value={formulario.ciudadId}
            onChange={manejarCambio}
            disabled={!formulario.departamentoId || cargandoCiudades}
          >
            <option value="">
              {!formulario.departamentoId
                ? 'Elige primero un departamento'
                : cargandoCiudades
                  ? 'Cargando…'
                  : 'Selecciona una ciudad'}
            </option>
            {ciudades.map((ciudad) => (
              <option key={ciudad.id} value={ciudad.id}>
                {ciudad.nombre}
              </option>
            ))}
          </select>
        </Campo>
      </div>

      {mensajeGeneral && (
        <p className={`mensaje mensaje--${estadoEnvio}`} role="status">
          {mensajeGeneral}
        </p>
      )}

      {/* aria-live hace que un lector de pantalla anuncie el aviso cuando aparece,
          sin que el usuario tenga que ir a buscarlo. */}
      {cargandoCatalogos && (
        <p className="cargando" role="status" aria-live="polite">
          Cargando las listas del formulario…
        </p>
      )}

      {/* Deshabilitado también mientras cargan los catálogos: enviar en ese momento
          solo produciría errores de campos obligatorios que el usuario todavía no ha
          podido rellenar. */}
      <button
        className="boton"
        type="submit"
        disabled={estadoEnvio === 'enviando' || cargandoCatalogos}
      >
        {estadoEnvio === 'enviando' ? 'Enviando…' : 'Inscribirme'}
      </button>
    </form>
  )
}

/**
 * Envoltorio de un campo: etiqueta, control y mensaje de error.
 *
 * Evita repetir la misma estructura diez veces. `aria-describedby` conecta el
 * mensaje de error con el control para que un lector de pantalla lo anuncie.
 */
function Campo({ id, etiqueta, error, ancho, children }) {
  return (
    <div className={`campo${ancho === 'completo' ? ' campo--completo' : ''}${error ? ' campo--error' : ''}`}>
      <label className="campo__etiqueta" htmlFor={id}>
        {etiqueta}
      </label>
      {children}
      {error && (
        <p className="campo__error" id={`${id}-error`} role="alert">
          {error}
        </p>
      )}
    </div>
  )
}
