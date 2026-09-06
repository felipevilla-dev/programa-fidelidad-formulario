package com.fidelidad.programa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing).
 *
 * <h2>Qué problema resuelve</h2>
 *
 * <p>Por seguridad, el navegador bloquea que una página cargada desde un origen haga
 * peticiones a otro distinto. Un "origen" es la combinación de esquema, host y puerto, así
 * que {@code http://localhost:5173} (React con Vite) y {@code http://localhost:8080} (esta
 * API) son <b>orígenes distintos</b> aunque los dos estén en la misma máquina.
 *
 * <p>Sin esta configuración, el formulario de React recibiría un error de CORS en la consola
 * del navegador y ninguna llamada llegaría a completarse — aunque con {@code curl} todo
 * funcione perfectamente, porque la restricción la impone el navegador, no el servidor.
 *
 * <p>Lo que hace CORS es que el servidor declare, mediante cabeceras de respuesta, qué
 * orígenes tienen permiso. Para las peticiones que no son simples (como un POST con
 * {@code Content-Type: application/json}), el navegador manda primero una petición
 * {@code OPTIONS} de sondeo — el <i>preflight</i> — y solo envía la real si la respuesta lo
 * autoriza.
 *
 * <h2>Por qué aquí y no con @CrossOrigin en cada controlador</h2>
 *
 * <p>{@code @CrossOrigin} sobre cada clase funcionaría, pero habría que acordarse de ponerla
 * en cada controlador nuevo. Una sola clase de configuración define la política para toda la
 * API y no se puede olvidar.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Orígenes autorizados, configurables por entorno.
     *
     * <p>El valor por defecto es el servidor de desarrollo de Vite. Cuando el frontend se
     * despliegue en otra dirección basta definir la variable de entorno, sin recompilar.
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Lista explícita de orígenes en vez del comodín "*": el comodín permitiría
                // a cualquier sitio web llamar a esta API desde el navegador de un usuario.
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Cabeceras de la respuesta que el JavaScript del frontend puede leer.
                // Location no es visible por defecto, y el POST de registro la necesita.
                .exposedHeaders("Location")
                // Cuánto tiempo (en segundos) puede el navegador reutilizar el resultado del
                // preflight sin volver a preguntar.
                .maxAge(3600);
    }
}
