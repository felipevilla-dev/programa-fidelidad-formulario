package com.fidelidad.programa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de humo: verifica que el contexto de Spring arranca sin errores, es
 * decir, que todos los beans (repositorios, servicios y controladores) se
 * pueden crear y enlazar entre sí.
 *
 * <p>Corre sobre el perfil {@code test}, que sustituye PostgreSQL por H2 en
 * memoria. Antes esta prueba abría una conexión real y fallaba en cualquier
 * máquina sin la base {@code fidelidad_db} configurada, lo que obligaba a
 * compilar con {@code -DskipTests}. Ahora {@code mvn package} funciona en
 * limpio, que es lo que hará quien clone el repositorio.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProgramaFidelidadApplicationTests {

    @Test
    void elContextoDeSpringArranca() {
        // Sin aserciones: el test falla solo si el contexto no puede levantarse.
    }
}
