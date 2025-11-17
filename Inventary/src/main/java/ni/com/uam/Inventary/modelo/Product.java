package ni.com.uam.Inventary.modelo;

import javax.persistence.*;

import ni.com.uam.Inventary.run.Inventary;
import org.openxava.annotatations.*;
import lombok.*;

import ni.com.uam.Inventary.modelo.enums.UnidadMedida;

import java.math.BigDecimal;

@Entity
@Getter @Setter
public class Product {
    @Id
    @Column(length = 5)
    @Required
    private String codigo;

    @Column(length = 80)
    @Required
    private String nombre;

    @TextArea
    private String descripcion;

    @Required
    private Category category;

    @Enumerated(EnumType.STRING)
    @Required
    private UnidadMedida unidadMedida;

    @ManyToOne(fetch = FetType.LAZY)
    @DescriptionList
    private Supplier supplier;

    @Money
    private BigDecimal precioCompra;

    @Money
    private BigDecimal precioVenta;

    @Required
    private Integer stockMinimo;

    @Required
    private Integer stockActual;

}
