import { useState } from 'react'

/**
 * Alterna entre el tema claro y el oscuro, y recuerda la elección.
 *
 * El tema vive en el atributo `data-tema` del elemento raíz, que es lo que lee la
 * hoja de estilos para decidir qué paleta aplica. El valor inicial ya lo dejó
 * puesto el script de `index.html` antes del primer pintado; aquí solo se lee.
 *
 * La preferencia se guarda en `localStorage` para que sobreviva a recargar la
 * página. Si el navegador lo tiene bloqueado —navegación privada, por ejemplo—,
 * el cambio sigue funcionando durante la sesión: solo se pierde al recargar.
 */
export default function BotonTema() {
  const [tema, setTema] = useState(() => document.documentElement.dataset.tema || 'claro')

  const esOscuro = tema === 'oscuro'

  function alternar() {
    const nuevo = esOscuro ? 'claro' : 'oscuro'

    document.documentElement.dataset.tema = nuevo
    setTema(nuevo)

    try {
      localStorage.setItem('tema', nuevo)
    } catch {
      // Sin almacenamiento el tema no se recuerda, pero la página no debe romperse.
    }
  }

  return (
    <button
      type="button"
      className="boton-tema"
      onClick={alternar}
      // El botón no lleva texto, así que necesita un nombre accesible. Dice lo que
      // hará al pulsarlo, no el estado actual, que es lo que espera quien lo usa.
      aria-label={esOscuro ? 'Cambiar al tema claro' : 'Cambiar al tema oscuro'}
      title={esOscuro ? 'Tema claro' : 'Tema oscuro'}
    >
      {/* aria-hidden porque el icono es decorativo: el nombre ya está en el aria-label. */}
      {esOscuro ? <IconoSol /> : <IconoLuna />}
    </button>
  )
}

function IconoSol() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
      <circle cx="12" cy="12" r="4.2" fill="currentColor" />
      <g stroke="currentColor" strokeWidth="1.6" strokeLinecap="round">
        <line x1="12" y1="2.5" x2="12" y2="5" />
        <line x1="12" y1="19" x2="12" y2="21.5" />
        <line x1="2.5" y1="12" x2="5" y2="12" />
        <line x1="19" y1="12" x2="21.5" y2="12" />
        <line x1="5.3" y1="5.3" x2="7" y2="7" />
        <line x1="17" y1="17" x2="18.7" y2="18.7" />
        <line x1="5.3" y1="18.7" x2="7" y2="17" />
        <line x1="17" y1="7" x2="18.7" y2="5.3" />
      </g>
    </svg>
  )
}

function IconoLuna() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
      <path
        d="M20 14.4A8.5 8.5 0 0 1 9.6 4a8.5 8.5 0 1 0 10.4 10.4Z"
        fill="currentColor"
      />
    </svg>
  )
}
