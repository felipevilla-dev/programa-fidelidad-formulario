package com.fidelidad.programa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tipo de documento de identidad: CC, CE, TI, PA, NIT.
 *
 * <p>Se modela como tabla y no como {@code enum} para que el catálogo se pueda
 * ampliar sin recompilar la aplicación, y para que el desplegable del formulario
 * se alimente de la base de datos como pide el enunciado.
 */
@Entity
@Table(
        name = "tipo_identificacion",
        uniqueConstraints = @UniqueConstraint(name = "uk_tipo_identificacion_codigo", columnNames = "codigo")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoIdentificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código corto y estable: CC, CE, TI, PA, NIT. */
    @Column(name = "codigo", nullable = false, length = 5)
    private String codigo;

    /** Nombre completo que ve el usuario, por ejemplo "Cédula de Ciudadanía". */
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;
}
