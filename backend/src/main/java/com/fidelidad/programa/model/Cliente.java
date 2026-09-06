package com.fidelidad.programa.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
 * Cliente inscrito en el programa de fidelidad.
 *
 * <h2>Por qué solo se guarda la ciudad y no el departamento ni el país</h2>
 *
 * <p>El formulario pide ciudad, departamento y país, pero esos tres datos no son
 * independientes: cada ciudad pertenece a un único departamento y cada departamento
 * a un único país. Esa jerarquía ya está modelada en
 * {@link Ciudad} → {@link Departamento} → {@link Pais}.
 *
 * <p>Si además se guardaran aquí las columnas {@code departamento_id} y
 * {@code pais_id}, la base podría terminar con datos contradictorios: por ejemplo un
 * cliente con ciudad = Medellín pero departamento = Valle del Cauca. No habría forma
 * de saber cuál de los dos es el correcto, y cada actualización tendría que mantener
 * las tres columnas sincronizadas a mano.
 *
 * <p>Guardando únicamente {@code ciudad}, ese estado inconsistente es imposible de
 * representar: el departamento y el país se derivan con
 * {@code cliente.getCiudad().getDepartamento().getPais()}. Es la tercera forma normal
 * aplicada — un dato no debe depender de otro dato no clave de la misma tabla, sino
 * vivir en la tabla a la que realmente pertenece.
 */
@Entity
@Table(
        name = "cliente",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cliente_identificacion_marca",
                // La unicidad es POR MARCA: una misma persona puede inscribirse en
                // Chevignon y también en Esprit, pero no dos veces en la misma marca.
                //
                // El tipo entra en la combinación porque un mismo número puede repetirse
                // entre tipos distintos: una CC y un NIT pueden coincidir en dígitos y
                // pertenecer a titulares diferentes.
                columnNames = {"tipo_identificacion_id", "numero_identificacion", "marca_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tipo de documento: CC, CE, TI, PA o NIT. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_identificacion_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cliente_tipo_identificacion"))
    private TipoIdentificacion tipoIdentificacion;

    /** Se guarda como texto: puede llevar ceros a la izquierda y no se opera con él. */
    @Column(name = "numero_identificacion", nullable = false, length = 20)
    private String numeroIdentificacion;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    /** {@code LocalDate}: fecha sin hora ni zona horaria. */
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "direccion", nullable = false, length = 200)
    private String direccion;

    /** Única referencia geográfica del cliente (ver explicación de la clase). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciudad_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cliente_ciudad"))
    private Ciudad ciudad;

    /** Marca del grupo a la que el cliente decide vincularse. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marca_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cliente_marca"))
    private Marca marca;

    /**
     * Momento de la inscripción.
     *
     * <p>{@code @CreationTimestamp} hace que Hibernate la rellene al insertar, así que
     * el frontend no necesita enviarla. {@code updatable = false} impide que se
     * modifique en actualizaciones posteriores: es la fecha de registro, no la de la
     * última edición.
     */
    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;
}
