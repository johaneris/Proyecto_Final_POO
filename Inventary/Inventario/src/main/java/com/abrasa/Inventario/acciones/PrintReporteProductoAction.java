package com.abrasa.Inventario.acciones;

import com.abrasa.Inventario.modelo.Producto;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.openxava.actions.JasperReportBaseAction;
import org.openxava.jpa.XPersistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintReporteProductoAction extends JasperReportBaseAction {

    @Override
    protected JRDataSource getDataSource() throws Exception {
        // Traemos todos los productos activos ordenados por nombre
        List<Producto> productos = XPersistence.getManager()
                .createQuery(
                        "from Producto p where p.activo = true order by p.nombre",
                        Producto.class
                )
                .getResultList();

        return new JRBeanCollectionDataSource(productos);
    }

    @Override
    protected String getJRXML() throws Exception {
        // Nombre del archivo ubicado en src/main/resources/reports
        return "ProductoDetalle.jrxml";
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected Map getParameters() throws Exception {
        // Por ahora no enviamos parámetros adicionales
        return new HashMap<>();
    }
}
