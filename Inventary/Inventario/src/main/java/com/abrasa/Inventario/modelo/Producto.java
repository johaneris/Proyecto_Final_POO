package com.abrasa.Inventario.modelo;

import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.Digits;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.*;

/**
 * Entidad JPA + OpenXava que representa un producto del inventario.
 */
@Entity
@Table(name = "producto")
@Getter
@Setter
@View(name = "Simple",
        members =
                "DatosGenerales[" +
                        "   codigo; nombre; tipo; categoria; proveedor; activo; " +
                        "   descripcion;" +
                        "] " +
                        "Inventario[" +
                        "   unidadMedida; stockActual; stockMinimo; " +
                        "] " +
                        "Precios[" +
                        "   precioCompra; precioVenta; iva;" +
                        "]"
)
@Tab(name = "Productos",
        baseCondition = "activo = true",
        properties =
                "codigo, nombre, tipo, " +
                        "categoria.nombre, " +
                        "proveedor.nombreComercial, " +
                        "unidadMedida, stockActual, stockMinimo, precioVenta"
)
public class Producto {

    // 1) Identificación
    @Id
    @Column(length = 15)
    @Required
    @LabelFormat(LabelFormatType.SMALL)
    private String codigo;

    @Column(length = 80)
    @Required
    private String nombre;

    @Column(length = 30)
    @Required
    private String tipo;

    @Column(length = 200)
    @Stereotype("MEMO")
    private String descripcion;

    @Required
    private boolean activo = true;

    // -------- Categoría --------
    @ManyToOne(optional = false)
    @DescriptionsList(descriptionProperties = "nombre")   // Campo de Categoria
    @Required
    private Categoria categoria;

    // -------- Proveedor --------
    @ManyToOne(optional = false)
    @DescriptionsList(descriptionProperties = "nombreComercial, nombreLegal")
    @NoCreate
    @NoModify
    @Required
    private Proveedor proveedor;

    // 3) Inventario
    @Column(length = 20)
    @Required
    private String unidadMedida;

    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    @Required
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    @Required
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    // 4) Precios
    @Money
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    @Required
    private BigDecimal precioCompra = BigDecimal.ZERO;

    @Money
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    @Required
    private BigDecimal precioVenta = BigDecimal.ZERO;

    @Digits(integer = 3, fraction = 2)
    @Column(precision = 5, scale = 2)
    private BigDecimal iva = new BigDecimal("15.00");

    // ===== Reglas de negocio =====
    @PreUpdate
    private void validarPrecios() {
        if (precioCompra != null && precioVenta != null &&
                precioVenta.compareTo(precioCompra) < 0) {
            throw new IllegalArgumentException(
                    "El precio de venta no puede ser menor que el precio de compra para el producto " + codigo
            );
        }
    }


}
