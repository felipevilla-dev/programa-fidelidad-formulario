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
 * Marca del grupo a la que se vincula el cliente: Americanino, American Eagle,
 * Chevignon, Esprit, Naf Naf o Rifle.
 */
@Entity
@Table(
        name = "marca",
        uniqueConstraints = @UniqueConstraint(name = "uk_marca_nombre", columnNames = "nombre")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;
}
