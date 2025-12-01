package com.abrasa.Inventario.modelo;

import lombok.*;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "movimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@View(
        name = "Simple",
        members =
                "DatosMovimiento[" +
                        "   producto; tipoMovimiento; fecha; cantidad;" +
                        "] " +
                        "Relacion[" +
                        "   proveedor; observaciones;" +
                        "]"
)
@Tab(
        name = "Movimientos",
        properties = "fecha, tipoMovimiento, producto.codigo, producto.nombre, cantidad, proveedor.nombreComercial"
)
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Producto afectado por el movimiento
    @ManyToOne(optional = false)
    @ReferenceView("Simple")
    @Required
    private Producto producto;

    // Proveedor (solo tiene sentido para ENTRADA, pero lo dejamos opcional)
    @ManyToOne(optional = true)
    @ReferenceView("Simple")
    private Proveedor proveedor;

    @Required
    @Stereotype("ENUMERATION")
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;   // ENTRADA o SALIDA

    @Required
    private LocalDate fecha = LocalDate.now();

    @Required
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    private BigDecimal cantidad = BigDecimal.ONE;

    @Stereotype("MEMO")
    @Column(length = 200)
    private String observaciones;


    // ================= LÓGICA DE NEGOCIO =================

    @PrePersist
    private void aplicarMovimientoSobreStock() {

        if (producto == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto para el movimiento");
        }

        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser mayor que cero");
        }

        BigDecimal stockActual = producto.getStockActual() == null
                ? BigDecimal.ZERO
                : producto.getStockActual();

        switch (tipoMovimiento) {

            case ENTRADA:
                // Aumenta el stock
                producto.setStockActual(
                        stockActual.add(cantidad).setScale(2, BigDecimal.ROUND_HALF_UP)
                );
                break;

            case SALIDA:
                // Valida que haya stock suficiente
                if (stockActual.compareTo(cantidad) < 0) {
                    throw new IllegalArgumentException(
                            "No hay stock suficiente del producto "
                                    + producto.getNombre()
                                    + " para realizar la salida"
                    );
                }
                producto.setStockActual(
                        stockActual.subtract(cantidad).setScale(2, BigDecimal.ROUND_HALF_UP)
                );
                break;

            default:
                throw new IllegalArgumentException("Tipo de movimiento no soportado");
        }
    }

    @Override
    public String toString() {
        return fecha + " - " + tipoMovimiento + " - "
                + producto.getCodigo() + " (" + cantidad + " " + producto.getUnidadMedida() + ")";
    }
}
