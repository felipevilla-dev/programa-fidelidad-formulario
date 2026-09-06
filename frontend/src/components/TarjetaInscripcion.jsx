/**
 * Confirmación de la inscripción, con forma de carnet de socio.
 *
 * Un programa de fidelidad entrega una tarjeta: es su objeto real. Por eso la
 * confirmación no es un aviso verde más, sino el carnet en sí, con los datos que
 * devolvió el backend. El número de socio es el id que asignó la base de datos.
 *
 * Sustituye al formulario en lugar de aparecer debajo: al terminar, lo único que
 * importa es lo que acaba de ocurrir.
 */
export default function TarjetaInscripcion({ cliente, alInscribirOtro }) {
  return (
    <section className="confirmacion">
      <div className="carnet">
        <p className="carnet__marca">{cliente.marca.nombre}</p>

        <p className="carnet__nombre">
          {cliente.nombres} {cliente.apellidos}
        </p>

        <dl className="carnet__datos">
          <div>
            <dt>Socio</dt>
            <dd>n.º {cliente.id}</dd>
          </div>
          <div>
            <dt>Ciudad</dt>
            <dd>{cliente.ciudad.nombre}</dd>
          </div>
          <div>
            <dt>Desde</dt>
            <dd>{formatearFecha(cliente.fechaRegistro)}</dd>
          </div>
        </dl>
      </div>

      <p className="confirmacion__texto">
        Ya puedes usar los beneficios de {cliente.marca.nombre}. Guarda tu número de socio.
      </p>

      <button type="button" className="boton" onClick={alInscribirOtro}>
        Inscribir a otra persona
      </button>
    </section>
  )
}

/** "2026-09-06T12:34:56" → "6 de septiembre de 2026" */
function formatearFecha(fechaIso) {
  if (!fechaIso) return '—'
  return new Date(fechaIso).toLocaleDateString('es-CO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}
