package com.abrasa.Inventario.modelo;

import lombok.*;
import org.openxava.annotations.*;
import org.openxava.jpa.XPersistence;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@View(
        name = "Simple",
        members =
                "DatosFactura[" +
                        "   numero; fecha; cliente; tipoVenta; pagada;" +
                        "] " +
                        "Totales[" +
                        "   porcentajeIva; subtotal; iva; total;" +
                        "] " +
                        "Detalles{ detalles }"
)
@Tab(
        name = "Facturas",
        properties = "numero, fecha, cliente.nombre, tipoVenta, total, pagada"
)
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Número visible para el usuario
    @Required
    @Column(length = 20, unique = true)
    private String numero;

    @Required
    private LocalDate fecha = LocalDate.now();

    // AHORA: referencia a Cliente (en lugar de String)
    @ManyToOne(optional = false)
    @ReferenceView("Simple")           // utiliza la vista Simple de Cliente
    @Required
    private Cliente cliente;

    // Contado / Crédito
    @Required
    @Stereotype("ENUMERATION")        // se mostrará como combo en la UI
    @Column(length = 10)
    private String tipoVenta;         // "CONTADO" o "CREDITO"

    @Required
    private boolean pagada = false;

    // % IVA aplicado a toda la factura
    @NotNull
    @Digits(integer = 3, fraction = 2)
    @Column(name = "porcentaje_iva", precision = 5, scale = 2)
    private BigDecimal porcentajeIva = new BigDecimal("15.00");

    // Totales calculados
    @Money
    @Digits(integer = 12, fraction = 2)
    @Column(precision = 14, scale = 2)
    @ReadOnly
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Money
    @Digits(integer = 12, fraction = 2)
    @Column(precision = 14, scale = 2)
    @ReadOnly
    private BigDecimal iva = BigDecimal.ZERO;

    @Money
    @Digits(integer = 12, fraction = 2)
    @Column(precision = 14, scale = 2)
    @ReadOnly
    private BigDecimal total = BigDecimal.ZERO;

    // Líneas de la factura
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("producto.codigo, producto.nombre, cantidad, precioUnitario, importe")
    private Collection<DetalleFactura> detalles = new ArrayList<>();

    // ================= LÓGICA DE NEGOCIO =================

    public boolean esVentaACredito() {
        return "CREDITO".equalsIgnoreCase(tipoVenta);
    }

    /**
     * Añade una línea desde código (útil para acciones futuras).
     */
    public void agregarLinea(Producto producto,
                             BigDecimal cantidad,
                             BigDecimal precioUnitario) {
        DetalleFactura linea = new DetalleFactura();
        linea.setFactura(this);
        linea.setProducto(producto);
        linea.setCantidad(cantidad);
        linea.setPrecioUnitario(precioUnitario);
        detalles.add(linea);
        recalcularTotales();
    }

    /**
     * Recalcula subtotal, IVA y total en base a las líneas.
     */
    public void recalcularTotales() {
        BigDecimal nuevoSubtotal = BigDecimal.ZERO;

        if (detalles != null) {
            for (DetalleFactura d : detalles) {
                if (d.getImporte() != null) {
                    nuevoSubtotal = nuevoSubtotal.add(d.getImporte());
                }
            }
        }

        this.subtotal = nuevoSubtotal.setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal porcentaje = porcentajeIva == null
                ? BigDecimal.ZERO
                : porcentajeIva.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);

        this.iva = subtotal.multiply(porcentaje).setScale(2, BigDecimal.ROUND_HALF_UP);
        this.total = subtotal.add(iva).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Validaciones y recálculo antes de grabar/actualizar.
     */

    @PreUpdate
    private void validarYRecalcular() {

        // Debe tener al menos una línea
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un producto");
        }

        // Cliente obligatorio siempre
        if (cliente == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente para la factura");
        }

        // Recalcular totales
        recalcularTotales();

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la factura debe ser mayor que cero");
        }

        // Reglas de crédito
        if (esVentaACredito()) {

            // El cliente debe tener crédito habilitado
            if (!cliente.isPermiteCredito()) {
                throw new IllegalArgumentException(
                        "El cliente " + cliente.getNombre() +
                                " no tiene crédito habilitado");
            }

            BigDecimal limite = cliente.getLimiteCredito() == null
                    ? BigDecimal.ZERO : cliente.getLimiteCredito();

            BigDecimal saldoActual = cliente.getSaldoPendiente() == null
                    ? BigDecimal.ZERO : cliente.getSaldoPendiente();

            BigDecimal nuevoSaldo = saldoActual.add(total);

            if (nuevoSaldo.compareTo(limite) > 0) {
                throw new IllegalArgumentException(
                        "La factura supera el límite de crédito del cliente. " +
                                "Límite: " + limite + ", saldo actual: " + saldoActual);
            }
        }
    }

    /**
     * Después de grabar/actualizar, actualizar saldo del cliente si la venta es a crédito
     * y la factura queda pendiente (no pagada).
     */
    @PostPersist
    @PostUpdate
    private void actualizarSaldoCliente() {
        if (!esVentaACredito()) return;
        if (pagada) return;

        // Si el total aún no está calculado, no hacemos nada
        if (total == null) return;

        BigDecimal saldoActual = cliente.getSaldoPendiente() == null
                ? BigDecimal.ZERO
                : cliente.getSaldoPendiente();

        BigDecimal totalFactura = total == null
                ? BigDecimal.ZERO
                : total;

        cliente.setSaldoPendiente(
                saldoActual.add(totalFactura).setScale(2, BigDecimal.ROUND_HALF_UP)
        );
    }

    public void registrarPago() throws PagoFacturaException {

        if (cliente == null) {
            throw new PagoFacturaException("factura_sin_cliente");
        }

        if (!esVentaACredito()) {
            throw new PagoFacturaException("pago_solo_facturas_credito");
        }

        if (isPagada()) {
            throw new PagoFacturaException("factura_ya_pagada");
        }

        try {
            BigDecimal saldoActual = cliente.getSaldoPendiente() == null
                    ? BigDecimal.ZERO
                    : cliente.getSaldoPendiente();

            BigDecimal nuevoSaldo = saldoActual.subtract(getTotal());
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                nuevoSaldo = BigDecimal.ZERO;
            }

            cliente.setSaldoPendiente(
                    nuevoSaldo.setScale(2, BigDecimal.ROUND_HALF_UP)
            );

            setPagada(true); // marcamos la factura como pagada
        }
        catch (Exception ex) {
            // Cualquier problema inesperado
            throw new org.openxava.util.SystemException(
                    "imposible_registrar_pago_factura", ex);
        }
    }

    /**
     * Grabar desde código si en algún momento lo necesitas.
     */
    public void guardar() {
        XPersistence.getManager().persist(this);
    }

    @Override
    public String toString() {
        return numero + " (" + fecha + ") - " + cliente.getNombre();
    }
}
