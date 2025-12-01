package com.abrasa.Inventario.acciones;

import com.abrasa.Inventario.modelo.Cliente;
import com.abrasa.Inventario.modelo.Factura;
import org.openxava.actions.ViewBaseAction;
import org.openxava.jpa.XPersistence;

import java.math.BigDecimal;

public class RegistrarPagoFacturaAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {

        // 1) Validar que la factura esté grabada
        Object idValor = getView().getValue("id");
        if (idValor == null) {
            addError("imposible_registrar_pago_factura_no_existe");
            return;
        }

        Long id = (Long) idValor;

        // 2) Buscar la factura en BD de forma segura
        Factura factura = XPersistence.getManager().find(Factura.class, id);
        if (factura == null) {
            addError("factura_no_encontrada_para_pago", id);
            return;
        }

        // 3) Validar reglas de negocio básicas
        if (!factura.esVentaACredito()) {
            addError("solo_pagos_facturas_credito");
            return;
        }

        if (factura.isPagada()) {
            addError("factura_ya_pagada");
            return;
        }

        // 4) Registrar el pago
        Cliente cliente = factura.getCliente();
        BigDecimal total = factura.getTotal() == null
                ? BigDecimal.ZERO
                : factura.getTotal();

        // Descontar del saldo pendiente del cliente
        if (cliente != null && total.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal saldoActual = cliente.getSaldoPendiente() == null
                    ? BigDecimal.ZERO
                    : cliente.getSaldoPendiente();

            BigDecimal nuevoSaldo = saldoActual.subtract(total);
            if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
                nuevoSaldo = BigDecimal.ZERO;
            }
            cliente.setSaldoPendiente(nuevoSaldo.setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        // Marcar factura como pagada
        factura.setPagada(true);

        // Grabar cambios
        XPersistence.getManager().merge(factura);

        // Refrescar la vista y mostrar mensaje
        getView().refresh();
        addMessage("pago_registrado_correctamente", factura.getNumero());
    }
}
