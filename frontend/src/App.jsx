import { useState } from 'react'

import BotonTema from './components/BotonTema'
import FormularioInscripcion from './components/FormularioInscripcion'
import ListaInscritos from './components/ListaInscritos'

const VISTA_FORMULARIO = 'formulario'
const VISTA_INSCRITOS = 'inscritos'

/**
 * Contenedor de la aplicación: encabezado y las dos vistas.
 *
 * Se alterna entre inscribirse y consultar los inscritos con un estado local, sin
 * router: con dos vistas, añadir react-router traería una dependencia y un
 * concepto nuevos sin resolver ningún problema que hoy exista.
 *
 * Cada vista se monta y desmonta al cambiar de pestaña. Eso hace que la lista
 * vuelva a consultar al backend cada vez que se entra, así que un registro
 * recién hecho aparece sin tener que recargar la página.
 */
export default function App() {
  const [vista, setVista] = useState(VISTA_FORMULARIO)

  const esFormulario = vista === VISTA_FORMULARIO

  return (
    <div className="pagina">
      <header className="cabecera">
        <div className="cabecera__fila">
          <h1 className="cabecera__titulo">
            {esFormulario ? 'Inscríbete al programa de fidelidad' : 'Clientes inscritos'}
          </h1>
          <BotonTema />
        </div>
        <p className="cabecera__texto">
          {esFormulario
            ? 'Un solo registro te da acceso a los beneficios de Americanino, American Eagle, Chevignon, Esprit, Naf Naf y Rifle. Elige con cuál quieres empezar.'
            : 'Consulta quién se ha inscrito al programa. Puedes filtrar por marca.'}
        </p>
      </header>

      {/* role="tablist" y aria-selected permiten que un lector de pantalla
          anuncie esto como un grupo de pestañas y diga cuál está activa. */}
      <nav className="pestanas" role="tablist" aria-label="Secciones">
        <button
          type="button"
          role="tab"
          className={`pestana ${esFormulario ? 'pestana--activa' : ''}`}
          aria-selected={esFormulario}
          onClick={() => setVista(VISTA_FORMULARIO)}
        >
          Inscripción
        </button>
        <button
          type="button"
          role="tab"
          className={`pestana ${!esFormulario ? 'pestana--activa' : ''}`}
          aria-selected={!esFormulario}
          onClick={() => setVista(VISTA_INSCRITOS)}
        >
          Inscritos
        </button>
      </nav>

      <main>{esFormulario ? <FormularioInscripcion /> : <ListaInscritos />}</main>
    </div>
  )
}
