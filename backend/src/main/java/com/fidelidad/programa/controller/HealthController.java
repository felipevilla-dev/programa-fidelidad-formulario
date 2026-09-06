package com.fidelidad.programa.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint de diagnóstico para comprobar que el backend está levantado.
 *
 * <p>{@code @RestController} combina {@code @Controller} y {@code @ResponseBody}:
 * lo que devuelve el método se serializa directamente al cuerpo de la respuesta.
 * Como el tipo de retorno es un {@code Map}, Jackson lo convierte a JSON.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Estado del servicio")
public class HealthController {

    /**
     * @return {@code {"status":"UP"}} con código HTTP 200.
     */
    @Operation(summary = "Comprobar que la API responde",
            description = "Devuelve UP si la aplicación está levantada.")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
