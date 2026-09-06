import FormularioInscripcion from './components/FormularioInscripcion'

/**
 * Contenedor de la aplicación: encabezado y formulario.
 *
 * Toda la lógica vive en FormularioInscripcion; App solo lo monta y le da marco.
 */
export default function App() {
  return (
    <div className="pagina">
      <header className="cabecera">
        <h1 className="cabecera__titulo">Inscríbete al programa de fidelidad</h1>
        <p className="cabecera__texto">
          Un solo registro te da acceso a los beneficios de Americanino, American Eagle,
          Chevignon, Esprit, Naf Naf y Rifle. Elige con cuál quieres empezar.
        </p>
      </header>

      <main>
        <FormularioInscripcion />
      </main>
    </div>
  )
}
