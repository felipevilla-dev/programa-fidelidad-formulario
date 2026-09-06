/** Código del pasaporte en el catálogo de tipos de identificación. */
export const CODIGO_PASAPORTE = 'PA'

/**
 * Deja en el texto solo los caracteres válidos para ese tipo de documento.
 *
 * El pasaporte admite letras y números (AR123456); el resto de documentos son
 * numéricos. Se usa al escribir y al pegar, de modo que el usuario nunca llega a ver
 * un carácter que el formulario no acepta.
 */
export function filtrarNumeroIdentificacion(valor, codigoTipo) {
  return codigoTipo === CODIGO_PASAPORTE
    ? valor.replace(/[^A-Za-z0-9]/g, '')
    : valor.replace(/\D/g, '')
}

/**
 * Valida el formulario y devuelve un objeto { campo: mensaje }.
 *
 * Devuelve la MISMA forma que el mapa `errores` del ErrorResponseDTO del backend.
 * Gracias a eso, el componente pinta igual los errores detectados aquí y los que
 * llegan en un 400, sin ninguna traducción intermedia.
 *
 * Estas reglas son un espejo de las validaciones del servidor. Validar aquí le da al
 * usuario respuesta inmediata sin gastar una petición, pero NO sustituye a la
 * validación del backend: cualquiera puede saltarse el JavaScript, así que el
 * servidor sigue siendo la autoridad.
 *
 * @param datos       estado actual del formulario
 * @param codigoTipo  código del tipo de documento elegido (CC, CE, TI, PA, NIT);
 *                    hace falta porque el formato del número depende de él
 */
export function validarFormulario(datos, codigoTipo) {
  const errores = {}

  if (!datos.tipoIdentificacionId) {
    errores.tipoIdentificacionId = 'El tipo de identificación es obligatorio'
  }

  const numero = datos.numeroIdentificacion.trim()
  // El formato depende del tipo de documento: el pasaporte lleva letras (AR123456),
  // los demás son solo numéricos. Es la misma regla que aplica el backend.
  const patron = codigoTipo === CODIGO_PASAPORTE ? /^[A-Za-z0-9]+$/ : /^\d+$/

  if (!numero) {
    errores.numeroIdentificacion = 'El número de identificación es obligatorio'
  } else if (numero.length > 20) {
    errores.numeroIdentificacion = 'No puede superar 20 caracteres'
  } else if (!patron.test(numero)) {
    // El campo ya filtra lo que se teclea o se pega, así que esta comprobación es
    // una red de seguridad para cualquier vía por la que el estado llegue con
    // caracteres que no corresponden.
    errores.numeroIdentificacion =
      codigoTipo === CODIGO_PASAPORTE
        ? 'Solo puede contener letras y números'
        : 'Solo puede contener números'
  }

  if (!datos.nombres.trim()) {
    errores.nombres = 'Los nombres son obligatorios'
  } else if (datos.nombres.trim().length > 100) {
    errores.nombres = 'No pueden superar 100 caracteres'
  }

  if (!datos.apellidos.trim()) {
    errores.apellidos = 'Los apellidos son obligatorios'
  } else if (datos.apellidos.trim().length > 100) {
    errores.apellidos = 'No pueden superar 100 caracteres'
  }

  if (!datos.fechaNacimiento) {
    errores.fechaNacimiento = 'La fecha de nacimiento es obligatoria'
  } else {
    // Se comparan solo las fechas, sin hora, para que "hoy" no dependa de la hora
    // a la que el usuario abra el formulario. El backend usa @Past, que exige
    // estrictamente una fecha anterior a hoy.
    const hoy = new Date()
    hoy.setHours(0, 0, 0, 0)
    const nacimiento = new Date(`${datos.fechaNacimiento}T00:00:00`)

    if (Number.isNaN(nacimiento.getTime())) {
      errores.fechaNacimiento = 'La fecha de nacimiento no es válida'
    } else if (nacimiento >= hoy) {
      errores.fechaNacimiento = 'La fecha de nacimiento debe ser anterior a hoy'
    }
  }

  if (!datos.direccion.trim()) {
    errores.direccion = 'La dirección es obligatoria'
  } else if (datos.direccion.trim().length > 200) {
    errores.direccion = 'No puede superar 200 caracteres'
  }

  // País y departamento no viajan al backend, pero sí son obligatorios en la
  // interfaz: sin ellos el usuario no puede llegar a elegir una ciudad.
  if (!datos.paisId) errores.paisId = 'El país es obligatorio'
  if (!datos.departamentoId) errores.departamentoId = 'El departamento es obligatorio'
  if (!datos.ciudadId) errores.ciudadId = 'La ciudad es obligatoria'
  if (!datos.marcaId) errores.marcaId = 'La marca es obligatoria'

  return errores
}
