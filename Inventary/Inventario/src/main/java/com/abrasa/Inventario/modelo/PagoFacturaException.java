package com.abrasa.Inventario.modelo;

import org.openxava.util.XavaResources;

/**
 * Excepción de aplicación para problemas al registrar el pago de una factura.
 */
public class PagoFacturaException extends Exception {

    public PagoFacturaException(String messageKey) {
        // messageKey se resuelve en el archivo i18n
        super(XavaResources.getString(messageKey));
    }
}
