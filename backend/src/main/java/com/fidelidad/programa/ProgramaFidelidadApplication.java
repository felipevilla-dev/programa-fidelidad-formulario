package com.fidelidad.programa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>{@code @SpringBootApplication} agrupa tres anotaciones: habilita la
 * configuración automática de Spring Boot, marca esta clase como clase de
 * configuración y activa el escaneo de componentes a partir de este paquete.
 * Por eso todas las capas (controller, service, repository...) viven debajo de
 * {@code com.fidelidad.programa}: así Spring las detecta sin configuración extra.
 */
@SpringBootApplication
public class ProgramaFidelidadApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgramaFidelidadApplication.class, args);
    }
}
