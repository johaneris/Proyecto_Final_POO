package com.abrasa.Inventario.modelo;

import lombok.*;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "factura")
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Factura a la que pertenece la línea
    @ManyToOne(optional = false)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    // Producto vendido
    @ManyToOne(optional = false)
    @ReferenceView("Simple")
    @Required
    private Producto producto;

    // Cantidad vendida
    @Required
    @Min(1)
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    private BigDecimal cantidad = BigDecimal.ONE;

    // Precio unitario aplicado en esta venta
    @Required
    @Money
    @Digits(integer = 10, fraction = 2)
    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    // Importe = cantidad * precioUnitario
    @Money
    @Digits(integer = 12, fraction = 2)
    @Column(precision = 14, scale = 2)
    @ReadOnly
    private BigDecimal importe = BigDecimal.ZERO;

    // Reglas de negocio de la línea
    @PrePersist
    @PreUpdate
    private void recalcularImporteYLógica() {

        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor que cero");
        }

        if (producto != null && producto.getPrecioCompra() != null &&
                precioUnitario.compareTo(producto.getPrecioCompra()) < 0) {
            throw new IllegalArgumentException(
                    "No se puede vender el producto " + producto.getNombre() +
                            " por debajo del precio de compra");
        }

        this.importe = cantidad.multiply(precioUnitario)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
