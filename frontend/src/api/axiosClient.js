import axios from 'axios'

/**
 * Instancia de axios configurada para hablar con el backend.
 *
 * La URL base sale de la variable de entorno VITE_API_URL (definida en el .env).
 * El valor de respaldo evita que la aplicación quede inservible si alguien clona
 * el repositorio y arranca sin crear su .env: como el .env no se versiona, ese
 * caso es de esperar.
 *
 * Tener una única instancia —en vez de llamar a axios.get con la URL completa en
 * cada sitio— permite cambiar la dirección del backend, las cabeceras o el tiempo
 * de espera en un solo punto.
 */
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  // Si el backend no responde en 10 segundos, se falla con un error de red en vez
  // de dejar al usuario esperando indefinidamente.
  timeout: 10000,
})

export default axiosClient
