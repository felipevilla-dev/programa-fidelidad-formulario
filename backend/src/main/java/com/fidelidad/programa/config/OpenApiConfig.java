package com.fidelidad.programa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * Portada de la documentación de la API.
 *
 * <p>springdoc genera la especificación OpenAPI leyendo los controladores y los
 * DTO al arrancar, así que no hay que mantener la documentación a mano: si
 * cambia un endpoint o un campo, la página cambia con él. Esta clase solo aporta
 * los datos que el código no puede deducir, como el título o la descripción.
 *
 * <p>La documentación queda disponible en dos formatos:
 * <ul>
 *   <li><code>/swagger-ui.html</code> — página navegable, para probar la API</li>
 *   <li><code>/v3/api-docs</code> — la especificación en JSON, para herramientas</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiDelProgramaDeFidelidad() {
        return new OpenAPI().info(new Info()
                .title("API del Programa de Fidelidad")
                .version("1.0.0")
                .description("""
                        API de inscripción al programa de fidelidad de las seis marcas del \
                        grupo comercial: Americanino, American Eagle, Chevignon, Esprit, \
                        Naf Naf y Rifle.

                        Los endpoints de catálogo alimentan los desplegables del formulario. \
                        El de clientes registra la inscripción.

                        **Reglas de negocio**

                        - Una persona puede inscribirse en varias marcas, pero solo una vez \
                        en cada una. El documento es único por la combinación \
                        (tipo, número, marca).
                        - El número de documento solo admite dígitos, salvo en el pasaporte, \
                        que también acepta letras.
                        - El cliente guarda únicamente la ciudad: el departamento y el país \
                        se derivan navegando las relaciones.""")
                .contact(new Contact().name("Andrés Villa")));
    }
}
