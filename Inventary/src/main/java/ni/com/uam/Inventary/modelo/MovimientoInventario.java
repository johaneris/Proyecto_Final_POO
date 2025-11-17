package ni.com.uam.Inventary.modelo;

import java.util.Date;
import javax.persistence.*;

import ni.com.uam.Inventary.modelo.Customer;
import ni.com.uam.Inventary.modelo.Supplier;
import ni.com.uam.Inventary.modelo.User;
import ni.com.uam.Inventary.modelo.Product;
import ni.com.uam.Inventary.modelo.enums.TipoMovimiento;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import lombok.*;


@Entity
@Getter @Setter
public class MovimientoInventario {

    @Id
    @Hidden
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    private String oid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @DescriptionsList
    @Required
    private Product producto;

    @Enumerated(EnumType.STRING)
    @Required
    private TipoMovimiento tipoMovimiento;   // ENTRADA o SALIDA

    @Required
    private Integer cantidad;

    @Temporal(TemporalType.DATE)
    @Required
    private Date fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList
    @Required
    private User usuario;                 // quién registra el movimiento

    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList
    private Supplier proveedor;             // se usa en ENTRADA normalmente

    @ManyToOne(fetch = FetchType.LAZY)
    @DescriptionsList
    private Customer cliente;                 // se usa en SALIDA/Venta

    @TextArea
    private String observaciones;
}
