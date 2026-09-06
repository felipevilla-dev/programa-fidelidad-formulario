import { useEffect, useState } from 'react'

import { listarClientes, obtenerMarcas } from '../api/fidelidadApi'

const TAMANO_PAGINA = 10

/**
 * Tabla de clientes inscritos, con filtro por marca y paginación.
 *
 * Cierra el ciclo de la aplicación: hasta ahora solo se podía registrar, y la
 * única forma de ver quién estaba inscrito era consultar la base a mano.
 *
 * La paginación la resuelve el backend. Aquí solo se guarda qué página se está
 * pidiendo; los datos de esa página, cuántas hay y cuántos inscritos existen en
 * total vienen en la respuesta.
 */
export default function ListaInscritos() {
  const [pagina, setPagina] = useState(null) // objeto de página del backend
  const [marcas, setMarcas] = useState([])
  const [marcaId, setMarcaId] = useState('')
  const [numeroPagina, setNumeroPagina] = useState(0)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  // Las marcas del filtro no cambian: se piden una sola vez.
  useEffect(() => {
    obtenerMarcas()
      .then(setMarcas)
      .catch(() => {
        // Que falle el filtro no debe impedir ver la tabla.
        setMarcas([])
      })
  }, [])

  /**
   * Consulta la página cada vez que cambia el filtro o el número de página.
   *
   * Ningún `setState` se ejecuta de forma síncrona: todos ocurren cuando la
   * promesa se resuelve. Encender el indicador de carga es trabajo del evento
   * que provoca la consulta, no de este efecto.
   *
   * La bandera `vigente` descarta las respuestas que llegan tarde. Si alguien
   * pulsa «Siguiente» dos veces seguidas, la primera respuesta puede llegar
   * después de la segunda y dejar en pantalla la página equivocada; al
   * desmontarse el efecto anterior, su respuesta se ignora.
   */
  useEffect(() => {
    let vigente = true

    listarClientes({ marcaId, pagina: numeroPagina, tamano: TAMANO_PAGINA })
      .then((respuesta) => {
        if (!vigente) return
        setPagina(respuesta)
        setError('')
      })
      .catch(() => {
        if (!vigente) return
        setError('No se pudo cargar la lista. Comprueba que el servidor esté disponible.')
        setPagina(null)
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })

    return () => {
      vigente = false
    }
  }, [marcaId, numeroPagina])

  /**
   * Al cambiar de marca se vuelve a la primera página.
   *
   * Sin esto, quien estuviera en la página 4 de una marca con muchos inscritos
   * pasaría a la página 4 de otra que quizá solo tiene una: vería una tabla
   * vacía sin entender por qué.
   */
  function manejarCambioDeMarca(evento) {
    setCargando(true)
    setMarcaId(evento.target.value)
    setNumeroPagina(0)
  }

  function irAPagina(numero) {
    setCargando(true)
    setNumeroPagina(numero)
  }

  const inscritos = pagina?.content ?? []
  const totalPaginas = pagina?.totalPages ?? 0
  const totalInscritos = pagina?.totalElements ?? 0

  return (
    <section className="lista">
      <div className="lista__barra">
        <div className="campo">
          <label className="campo__etiqueta" htmlFor="filtro-marca">
            Filtrar por marca
          </label>
          <select
            id="filtro-marca"
            value={marcaId}
            onChange={manejarCambioDeMarca}
            disabled={marcas.length === 0}
          >
            <option value="">Todas las marcas</option>
            {marcas.map((marca) => (
              <option key={marca.id} value={marca.id}>
                {marca.nombre}
              </option>
            ))}
          </select>
        </div>

        <p className="lista__total" aria-live="polite">
          {cargando
            ? 'Consultando…'
            : `${totalInscritos} ${totalInscritos === 1 ? 'inscrito' : 'inscritos'}`}
        </p>
      </div>

      {error && <p className="mensaje mensaje--error">{error}</p>}

      {!error && !cargando && inscritos.length === 0 && (
        <p className="lista__vacia">
          {marcaId
            ? 'Todavía no hay nadie inscrito en esta marca.'
            : 'Todavía no hay inscritos. Registra el primero desde el formulario.'}
        </p>
      )}

      {inscritos.length > 0 && (
        // El contenedor permite desplazar la tabla en pantallas estrechas
        // sin que la página entera se desborde hacia los lados.
        <div className="tabla__contenedor">
          <table className="tabla">
            <caption className="tabla__titulo">
              Clientes inscritos{marcaId ? ' en la marca seleccionada' : ''}
            </caption>
            <thead>
              <tr>
                <th scope="col">Documento</th>
                <th scope="col">Nombre</th>
                <th scope="col">Ciudad</th>
                <th scope="col">Marca</th>
                <th scope="col">Fecha de registro</th>
              </tr>
            </thead>
            <tbody>
              {inscritos.map((cliente) => (
                <tr key={cliente.id}>
                  <td>
                    <span className="tabla__tipo">{cliente.tipoIdentificacion.codigo}</span>{' '}
                    {cliente.numeroIdentificacion}
                  </td>
                  <td>
                    {cliente.nombres} {cliente.apellidos}
                  </td>
                  <td>
                    {cliente.ciudad.nombre}
                    <span className="tabla__secundario">, {cliente.departamento.nombre}</span>
                  </td>
                  <td>{cliente.marca.nombre}</td>
                  <td>{formatearFecha(cliente.fechaRegistro)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {totalPaginas > 1 && (
        <nav className="paginacion" aria-label="Paginación de inscritos">
          <button
            type="button"
            className="boton boton--secundario"
            onClick={() => irAPagina(numeroPagina - 1)}
            disabled={numeroPagina === 0 || cargando}
          >
            Anterior
          </button>

          <span className="paginacion__estado">
            Página {numeroPagina + 1} de {totalPaginas}
          </span>

          <button
            type="button"
            className="boton boton--secundario"
            onClick={() => irAPagina(numeroPagina + 1)}
            disabled={numeroPagina >= totalPaginas - 1 || cargando}
          >
            Siguiente
          </button>
        </nav>
      )}
    </section>
  )
}

/** Convierte "2026-09-06T12:34:56" en "6 sep 2026". */
function formatearFecha(fechaIso) {
  if (!fechaIso) return '—'
  return new Date(fechaIso).toLocaleDateString('es-CO', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}
