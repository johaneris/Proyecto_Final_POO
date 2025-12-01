package com.abrasa.Inventario.modelo;

import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.Digits;
import org.openxava.annotations.*;

/**
 * Entidad JPA + OpenXava que representa un producto del inventario.
 */
@Entity
@Table(name = "producto")
@View(name = "Simple",
        members =
                "DatosGenerales[" +
                        "   codigo; nombre; tipo; activo; " +
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
        properties = "codigo, nombre, tipo, unidadMedida, stockActual, stockMinimo, precioVenta"
)
public class Producto {

    // 1) Identificación
    @Id
    @Column(length = 15)
    @Required
    @LabelFormat(LabelFormatType.SMALL)
    private String codigo;           // Ej: "HERB-001", "FERT-15KG"

    @Column(length = 80)
    @Required
    private String nombre;           // Ej: "Glifosato 480 SL", "Urea 46%"

    @Column(length = 30)
    @Required
    private String tipo;             // Ej: "Agroquímico", "Fertilizante", "Veterinario"

    @Column(length = 200)
    @Stereotype("MEMO")
    private String descripcion;      // Detalle del producto, dosis, observaciones, etc.

    @Required
    private boolean activo = true;   // Para desactivar productos sin borrarlos

    // 2) Información de inventario
    @Column(length = 20)
    @Required
    private String unidadMedida;     // Ej: "Litro", "Kg", "Frasco 250 ml"

    // Stock actual en bodega
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    @Required
    private BigDecimal stockActual = BigDecimal.ZERO;

    // Cantidad mínima antes de lanzar alerta
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    @Required
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    // 3) Precios
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

    // Porcentaje de IVA, por ejemplo 15 = 15%
    @Digits(integer = 3, fraction = 2)
    @Column(precision = 5, scale = 2)
    private BigDecimal iva = new BigDecimal("15.00");



    // ===== Reglas de negocio =====

    @PreUpdate
    private void validarPrecios() {
        if (precioCompra != null && precioVenta != null) {
            if (precioVenta.compareTo(precioCompra) < 0) {
                throw new IllegalArgumentException(
                        "El precio de venta no puede ser menor que el precio de compra para el producto " + codigo
                );
            }
        }
    }

    // ===== Getters y Setters =====

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getStockActual() {
        return stockActual;
    }

    public void setStockActual(BigDecimal stockActual) {
        this.stockActual = stockActual;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }
}
