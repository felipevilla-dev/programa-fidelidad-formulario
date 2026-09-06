import axiosClient from './axiosClient'

/**
 * Funciones que consumen la API del programa de fidelidad.
 *
 * Cada una devuelve directamente `response.data`, es decir, el cuerpo de la
 * respuesta ya desempaquetado. Así el componente trabaja con listas y objetos
 * normales y no necesita saber cómo estructura axios sus respuestas: si mañana se
 * cambiara axios por fetch, solo habría que tocar este archivo.
 *
 * Los errores NO se capturan aquí a propósito: se dejan subir para que el
 * componente decida qué mensaje mostrar según el código HTTP (400, 409, otro).
 */

/** GET /api/tipos-identificacion -> [{ id, codigo, nombre }] */
export async function obtenerTiposIdentificacion() {
  const { data } = await axiosClient.get('/tipos-identificacion')
  return data
}

/** GET /api/paises -> [{ id, nombre }] */
export async function obtenerPaises() {
  const { data } = await axiosClient.get('/paises')
  return data
}

/** GET /api/marcas -> [{ id, nombre }] */
export async function obtenerMarcas() {
  const { data } = await axiosClient.get('/marcas')
  return data
}

/**
 * GET /api/departamentos?paisId=... -> [{ id, nombre }]
 *
 * Segundo eslabón de los desplegables en cascada.
 */
export async function obtenerDepartamentosPorPais(paisId) {
  const { data } = await axiosClient.get('/departamentos', {
    // axios se encarga de construir la query string y de codificar los valores.
    params: { paisId },
  })
  return data
}

/**
 * GET /api/ciudades?departamentoId=... -> [{ id, nombre }]
 *
 * Tercer y último eslabón de la cascada. La ciudad elegida aquí es el único dato
 * geográfico que se envía al registrar: el backend deduce departamento y país.
 */
export async function obtenerCiudadesPorDepartamento(departamentoId) {
  const { data } = await axiosClient.get('/ciudades', {
    params: { departamentoId },
  })
  return data
}

/**
 * POST /api/clientes -> ClienteResponseDTO
 *
 * Respuestas posibles del backend:
 *   201 -> cliente registrado
 *   400 -> alguna validación falló; el cuerpo trae un mapa `errores` por campo
 *   409 -> ya existe un cliente con ese tipo y número de identificación
 */
export async function registrarCliente(datos) {
  const { data } = await axiosClient.post('/clientes', datos)
  return data
}

/**
 * GET /api/clientes?marcaId=...&page=...&size=... -> página de inscritos
 *
 * La respuesta no es una lista sino un objeto de página de Spring Data:
 *
 *   {
 *     content: [ClienteResponseDTO, ...],  // los registros de ESTA página
 *     number: 0,                           // página actual, empezando en 0
 *     totalPages: 3,
 *     totalElements: 47                    // cuántos hay en total, no en la página
 *   }
 *
 * `marcaId` se omite cuando viene vacío para que el backend devuelva todas las
 * marcas: axios descarta los parámetros `undefined` al construir la URL.
 */
export async function listarClientes({ marcaId, pagina = 0, tamano = 20 } = {}) {
  const { data } = await axiosClient.get('/clientes', {
    params: {
      marcaId: marcaId || undefined,
      page: pagina,
      size: tamano,
    },
  })
  return data
}
