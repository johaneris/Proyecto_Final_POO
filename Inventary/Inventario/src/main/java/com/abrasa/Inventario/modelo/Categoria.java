package com.abrasa.Inventario.modelo;

import lombok.*;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Categoría de productos (fertilizantes, agroquímicos, semillas, etc.).
 */
@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@View(
        name = "Simple",
        members =
                "DatosGenerales[" +
                        "   nombre; activa;" +
                        "] " +
                        "Descripcion[" +
                        "   descripcion;" +
                        "]"
)
@Tab(
        name = "Categorias",
        baseCondition = "activa = true",
        properties = "id, nombre, descripcion"
)
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden   // el usuario no necesita ver el id interno
    private Long id;

    @Required
    @Size(min = 3, max = 60)
    @Column(length = 60, nullable = false, unique = true)
    private String nombre;

    @Stereotype("MEMO")
    @Column(length = 200)
    private String descripcion;

    @Required
    private boolean activa = true;

    // ============== Reglas de negocio ==============

    @PrePersist
    @PreUpdate
    private void validarCategoria() {

        if (nombre != null) {
            nombre = nombre.trim();
        }

        // Nombre obligatorio y con longitud razonable
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio");
        }
        if (nombre.length() < 3) {
            throw new IllegalArgumentException(
                    "El nombre de la categoría debe tener al menos 3 caracteres");
        }

        // Descripción opcional pero, si se pone, que no sea solo espacios
        if (descripcion != null) {
            descripcion = descripcion.trim();
            if (descripcion.isEmpty()) {
                descripcion = null;
            }
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}

