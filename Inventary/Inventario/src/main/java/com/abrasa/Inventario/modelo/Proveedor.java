package com.abrasa.Inventario.modelo;

import lombok.*;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@View(
        name = "Simple",
        members =
                "Identificacion[" +
                        "   codigo; nombreLegal; nombreComercial; tipoProveedor; activo;" +
                        "] " +
                        "Contacto[" +
                        "   telefono; celular; email; sitioWeb;" +
                        "] " +
                        "Ubicacion[" +
                        "   direccion; municipio; departamento; pais;" +
                        "] " +
                        "CondicionesPago[" +
                        "   manejaCredito; plazoCreditoDias; limiteCredito; saldoPendiente;" +
                        "]"
)
@Tab(
        name = "Proveedores",
        baseCondition = "activo = true",
        properties = "codigo, nombreComercial, tipoProveedor, telefono, email, plazoCreditoDias, limiteCredito, saldoPendiente"
)
public class Proveedor {

    // Identificador interno: PROV-001, PROV-002, etc.
    @Id
    @Column(length = 15)
    @Required
    private String codigo;

    // Razón social / nombre legal de la empresa
    @Required
    @Column(length = 120)
    private String nombreLegal;

    // Nombre comercial con el que se conoce al proveedor
    @Column(length = 120)
    private String nombreComercial;

    // Tipo de proveedor: Agroquímicos, semillas, veterinaria, transporte, etc.
    @Required
    @Column(length = 40)
    private String tipoProveedor;

    @Required
    private boolean activo = true;


    // ----------------- Contacto -----------------

    @Column(length = 30)
    private String telefono;

    @Column(length = 30)
    private String celular;

    @Email
    @Column(length = 80)
    private String email;

    @Column(length = 100)
    private String sitioWeb;


    // ----------------- Ubicación -----------------

    @Stereotype("MEMO")
    @Column(length = 200)
    private String direccion;

    @Column(length = 40)
    private String municipio;

    @Column(length = 40)
    private String departamento;

    @Column(length = 40)
    private String pais = "Nicaragua";


    // ---------- Condiciones de pago / crédito ----------

    // Si el proveedor nos otorga crédito (30 días, 45 días, etc.)
    @NotNull
    private boolean manejaCredito = false;

    // Plazo de crédito en días (ej. 30, 45). Solo tiene sentido si manejaCredito = true
    @Digits(integer = 5, fraction = 0)
    @Column(name = "plazo_credito_dias")
    private Integer plazoCreditoDias = 0;

    // Monto máximo que el proveedor nos permite deberle
    @Money
    @Digits(integer = 12, fraction = 2)
    @Column(precision = 14, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    // Lo que actualmente le debemos al proveedor
    @Money
    @Digits(integer = 12, fraction = 2)
    @Column(precision = 14, scale = 2)
    private BigDecimal saldoPendiente = BigDecimal.ZERO;


    // ================= LÓGICA DE NEGOCIO =================

    @PrePersist
    @PreUpdate
    private void validarProveedor() {

        // Al menos un medio de contacto (teléfono, celular o correo)
        boolean sinTelefono = (telefono == null || telefono.trim().isEmpty());
        boolean sinCelular  = (celular  == null || celular.trim().isEmpty());
        boolean sinEmail    = (email    == null || email.trim().isEmpty());

        if (sinTelefono && sinCelular && sinEmail) {
            throw new IllegalArgumentException(
                    "El proveedor debe tener al menos un teléfono, celular o correo electrónico");
        }

        // Si no maneja crédito, limpiar datos de crédito
        if (!manejaCredito) {
            plazoCreditoDias = 0;
            limiteCredito = BigDecimal.ZERO;
            if (saldoPendiente == null) {
                saldoPendiente = BigDecimal.ZERO;
            }
            // No seguimos validando reglas de crédito
            return;
        }

        // Si maneja crédito, el plazo debe ser > 0 y el límite > 0
        if (plazoCreditoDias == null || plazoCreditoDias <= 0) {
            throw new IllegalArgumentException(
                    "Si el proveedor maneja crédito, el plazo de crédito en días debe ser mayor que cero");
        }

        if (limiteCredito == null || limiteCredito.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Si el proveedor maneja crédito, el límite de crédito debe ser mayor que cero");
        }

        if (saldoPendiente == null) {
            saldoPendiente = BigDecimal.ZERO;
        }

        // No podemos deberle al proveedor más de lo que nos autoriza
        if (saldoPendiente.compareTo(limiteCredito) > 0) {
            throw new IllegalArgumentException(
                    "El saldo pendiente con el proveedor no puede superar su límite de crédito");
        }
    }

    @Override
    public String toString() {
        String nombre = (nombreComercial != null && !nombreComercial.trim().isEmpty())
                ? nombreComercial
                : nombreLegal;
        return codigo + " - " + nombre;
    }
}

