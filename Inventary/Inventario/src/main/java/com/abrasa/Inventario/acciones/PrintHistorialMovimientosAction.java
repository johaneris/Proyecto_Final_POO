package com.abrasa.Inventario.acciones;

import com.abrasa.Inventario.modelo.Movimiento;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.openxava.actions.JasperReportBaseAction;
import org.openxava.jpa.XPersistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintHistorialMovimientosAction extends JasperReportBaseAction {

    @Override
    protected JRDataSource getDataSource() throws Exception {
        // Traemos todos los movimientos ordenados por fecha y nombre de producto
        List<Movimiento> movimientos = XPersistence.getManager()
                .createQuery(
                        "from Movimiento m order by m.fecha, m.producto.nombre",
                        Movimiento.class
                )
                .getResultList();

        return new JRBeanCollectionDataSource(movimientos);
    }

    @Override
    protected String getJRXML() throws Exception {
        // Nombre del archivo ubicado en src/main/resources/reports
        return "HistorialMovimientos.jrxml";
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected Map getParameters() throws Exception {
        // Por ahora sin parámetros adicionales
        return new HashMap<>();
    }
}
