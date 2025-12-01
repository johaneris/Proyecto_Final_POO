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

    // Cliente
    @ManyToOne(optional = false)
    @ReferenceView("Simple")
    @Required
    private Cliente cliente;

    // Contado / Crédito
    @Required
    @Stereotype("ENUMERATION")   // "CONTADO" o "CREDITO"
    @Column(length = 10)
    private String tipoVenta;

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
        String tv = (tipoVenta == null) ? "" : tipoVenta;
        return "CREDITO".equalsIgnoreCase(tv);
    }

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
     * Todo null se trata como 0 para evitar errores.
     */
    public void recalcularTotales() {
        BigDecimal nuevoSubtotal = BigDecimal.ZERO;

        if (detalles != null) {
            for (DetalleFactura d : detalles) {
                BigDecimal imp = d.getImporte();
                if (imp != null) {
                    nuevoSubtotal = nuevoSubtotal.add(imp);
                }
            }
        }

        this.subtotal = nuevoSubtotal.setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal porcentaje =
                (porcentajeIva == null)
                        ? BigDecimal.ZERO
                        : porcentajeIva.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);

        this.iva = subtotal.multiply(porcentaje).setScale(2, BigDecimal.ROUND_HALF_UP);
        this.total = subtotal.add(iva).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // ==== VALIDACIONES / CÁLCULO ANTES DE GRABAR ====

    @PrePersist
    @PreUpdate
    private void validarYRecalcular() {

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un producto");
        }

        if (cliente == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente para la factura");
        }

        // Aseguramos que porcentajeIva nunca sea null
        if (porcentajeIva == null) {
            porcentajeIva = BigDecimal.ZERO;
        }

        recalcularTotales();

        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la factura debe ser mayor que cero");
        }

        // Reglas de crédito
        if (esVentaACredito()) {

            if (!cliente.isPermiteCredito()) {
                throw new IllegalArgumentException(
                        "El cliente " + cliente.getNombre() +
                                " no tiene crédito habilitado");
            }

            BigDecimal limite = (cliente.getLimiteCredito() == null)
                    ? BigDecimal.ZERO
                    : cliente.getLimiteCredito();

            BigDecimal saldoActual = (cliente.getSaldoPendiente() == null)
                    ? BigDecimal.ZERO
                    : cliente.getSaldoPendiente();

            BigDecimal nuevoSaldo = saldoActual.add(total);

            if (nuevoSaldo.compareTo(limite) > 0) {
                throw new IllegalArgumentException(
                        "La factura supera el límite de crédito del cliente. " +
                                "Límite: " + limite + ", saldo actual: " + saldoActual);
            }
        }
    }

    // ==== ACTUALIZAR SALDO DEL CLIENTE AL FACTURAR ====

    @PostPersist
    @PostUpdate
    private void actualizarSaldoCliente() {
        if (!esVentaACredito()) return;
        if (pagada) return;
        if (total == null) return;

        BigDecimal saldoActual = (cliente.getSaldoPendiente() == null)
                ? BigDecimal.ZERO
                : cliente.getSaldoPendiente();

        cliente.setSaldoPendiente(
                saldoActual.add(total).setScale(2, BigDecimal.ROUND_HALF_UP)
        );
    }

    // ==== REGISTRAR PAGO DESDE LÓGICA DE NEGOCIO ====

    public void registrarPago() throws PagoFacturaException {

        if (cliente == null) {
            throw new PagoFacturaException("factura_sin_cliente");
        }

        if (!esVentaACredito()) {
            throw new PagoFacturaException("pago_solo_facturas_credito");
        }

        if (pagada) {
            throw new PagoFacturaException("factura_ya_pagada");
        }

        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PagoFacturaException("total_factura_debe_ser_mayor_cero");
        }

        try {
            BigDecimal saldoActual = (cliente.getSaldoPendiente() == null)
                    ? BigDecimal.ZERO
                    : cliente.getSaldoPendiente();

            BigDecimal nuevoSaldo = saldoActual.subtract(total);
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                nuevoSaldo = BigDecimal.ZERO;
            }

            cliente.setSaldoPendiente(
                    nuevoSaldo.setScale(2, BigDecimal.ROUND_HALF_UP)
            );

            this.pagada = true;
        }
        catch (Exception ex) {
            throw new org.openxava.util.SystemException(
                    "imposible_registrar_pago_factura", ex);
        }
    }

    public void guardar() {
        XPersistence.getManager().persist(this);
    }

    @Override
    public String toString() {
        return numero + " (" + fecha + ") - " + cliente.getNombre();
    }
}
