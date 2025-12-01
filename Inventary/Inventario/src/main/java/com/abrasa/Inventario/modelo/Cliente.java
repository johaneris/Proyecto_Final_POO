package com.abrasa.Inventario.modelo;

import lombok.*;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@View(
        name = "Simple",
        members =
                "DatosGenerales[" +
                        "   codigo; nombre; tipoCliente; activo;" +
                        "] " +
                        "Contacto[" +
                        "   telefono; email;" +
                        "] " +
                        "Ubicacion[" +
                        "   direccion; municipio; departamento;" +
                        "] " +
                        "Credito[" +
                        "   permiteCredito; limiteCredito; saldoPendiente;" +
                        "]"
)
@Tab(
        name = "Clientes",
        baseCondition = "activo = true",
        properties = "codigo, nombre, tipoCliente, telefono, email, limiteCredito, saldoPendiente"
)
public class Cliente {

    // Identificador interno que usarás en la agropecuaria: CLI-001, CLI-002, etc.
    @Id
    @Column(length = 15)
    @Required
    private String codigo;

    @Required
    @Column(length = 80)
    private String nombre;          // Nombre del productor, finca o empresa

    @Required
    @Column(length = 30)
    private String tipoCliente;     // Ej: "Productor", "Distribuidor", "Detalle", "Mayorista"

    @Required
    private boolean activo = true;  // Para no borrar clientes, solo desactivarlos


    // Datos de contacto básicos: en la agropecuaria al menos un medio debe quedar registrado
    @Column(length = 30)
    private String telefono;

    @Email
    @Column(length = 80)
    private String email;


    // Ubicación del cliente: útil para rutas de reparto y entregas en finca
    @Stereotype("MEMO")
    @Column(length = 200)
    private String direccion;

    @Column(length = 40)
    private String municipio;

    @Column(length = 40)
    private String departamento;


    // Información de crédito: muy típica en agropecuarias que fían insumos
    @NotNull
    private boolean permiteCredito = false;

    // Límite de crédito aprobado para este cliente
    @Money
    @Digits(integer = 12, fraction = 2)
    @Column(precision = 14, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    // Monto pendiente de pago por facturas anteriores
    @Money
    @Digits(integer = 12, fraction = 2)
    @Column(precision = 14, scale = 2)
    private BigDecimal saldoPendiente = BigDecimal.ZERO;


    // ================= LÓGICA DE NEGOCIO =================

    @PrePersist
    @PreUpdate
    private void validarCliente() {

        // Al menos un medio de contacto
        if ((telefono == null || telefono.trim().isEmpty()) &&
                (email == null || email.trim().isEmpty())) {
            throw new IllegalArgumentException(
                    "El cliente debe tener al menos un teléfono o un correo electrónico");
        }

        // Si no tiene crédito, limpiar datos de crédito
        if (!permiteCredito) {
            limiteCredito = BigDecimal.ZERO;
            saldoPendiente = BigDecimal.ZERO;
            return; // No hace falta seguir validando crédito
        }

        // Si tiene crédito permitido, el límite debe ser > 0
        if (limiteCredito == null || limiteCredito.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Si el cliente tiene crédito, el límite de crédito debe ser mayor que cero");
        }

        if (saldoPendiente == null) {
            saldoPendiente = BigDecimal.ZERO;
        }

        // El saldo pendiente nunca puede superar el límite
        if (saldoPendiente.compareTo(limiteCredito) > 0) {
            throw new IllegalArgumentException(
                    "El saldo pendiente no puede superar el límite de crédito del cliente");
        }
    }

    /**
     * Regla de negocio útil para facturación a futuro:
     * devuelve true si el cliente está bloqueado porque ya alcanzó su límite de crédito.
     */
    public boolean estaBloqueadoPorDeuda() {
        return permiteCredito &&
                limiteCredito != null &&
                saldoPendiente != null &&
                saldoPendiente.compareTo(limiteCredito) >= 0;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
