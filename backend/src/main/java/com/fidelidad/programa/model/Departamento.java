package com.fidelidad.programa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Departamento, nivel intermedio de la jerarquía geográfica.
 *
 * <p>Muchos departamentos pertenecen a un país, de ahí {@code @ManyToOne}.
 */
@Entity
@Table(
        name = "departamento",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_departamento_nombre_pais",
                // Dos departamentos pueden llamarse igual en países distintos,
                // pero no dentro del mismo país.
                columnNames = {"nombre", "pais_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * País al que pertenece el departamento.
     *
     * <p>{@code LAZY}: el país no se consulta hasta que alguien llama a
     * {@code getPais()}. El valor por defecto de {@code @ManyToOne} es EAGER, que
     * traería el país en cada consulta aunque no se use y genera el problema N+1.
     *
     * <p>{@code optional = false} + {@code nullable = false}: un departamento
     * siempre tiene país, tanto para JPA como para la restricción de la tabla.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pais_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_departamento_pais"))
    private Pais pais;
}
