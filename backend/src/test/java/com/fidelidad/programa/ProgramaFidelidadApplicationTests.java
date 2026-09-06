package com.fidelidad.programa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de humo: verifica que el contexto de Spring arranca sin errores.
 *
 * <p>Como el proyecto usa Spring Data JPA, este test abre una conexión real a
 * PostgreSQL. Si la base {@code fidelidad_db} no existe o las credenciales no son
 * correctas, fallará; en ese caso se puede compilar con {@code mvn package -DskipTests}.
 */
@SpringBootTest
class ProgramaFidelidadApplicationTests {

    @Test
    void contextLoads() {
        // Sin aserciones: el test falla solo si el contexto no puede levantarse.
    }
}
