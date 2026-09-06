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
 * Ciudad, nivel más bajo de la jerarquía geográfica.
 *
 * <p>Es la única referencia geográfica que guarda {@link Cliente}: desde la ciudad
 * se llega al departamento y al país navegando las relaciones.
 */
@Entity
@Table(
        name = "ciudad",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ciudad_nombre_departamento",
                columnNames = {"nombre", "departamento_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ciudad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /** Departamento al que pertenece la ciudad. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departamento_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ciudad_departamento"))
    private Departamento departamento;
}
