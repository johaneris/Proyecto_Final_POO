package ni.com.uam.Inventary.modelo;

import java.math.BigDecimal;
import javax.persistence.*;

import ni.com.uam.Inventary.modelo.Category;
import ni.com.uam.Inventary.modelo.Supplier;
import ni.com.uam.Inventary.modelo.enums.ClasificacionProducto;
import ni.com.uam.Inventary.modelo.enums.UnidadMedida;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
public class Product {

    @Id
    @Column(length = 15)
    @Required
    private String codigo;                 // código interno del producto

    @Column(length = 80)
    @Required
    private String nombre;

    @TextArea
    private String descripcion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @DescriptionsList
    @Required
    private Category categoria;

    @Enumerated(EnumType.STRING)
    @Required
    private ClasificacionProducto clasificacion;

    @Enumerated(EnumType.STRING)
    @Required
    private UnidadMedida unidadMedida;

    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList
    private Supplier proveedorPrincipal;

    @Money
    private BigDecimal precioCompra;

    @Money
    private BigDecimal precioVenta;

    @Required
    private Integer stockMinimo;

    @Required
    private Integer stockActual;
}
