package ni.com.uam.Inventary.modelo;

import java.security.Provider;
import java.util.Date;
import javax.management.DescriptorKey;
import javax.persistence.*;

import com.sun.security.ntlm.Client;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import lombok.*;

import ni.com.uam.Inventary.modelo.enums.TipoMovimiento;
import sun.jvm.hotspot.debugger.cdbg.EnumType;

@Entity
@Getter @Setter
public class MovimientoInventario {
    @Id
    @Hidden
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @Column(length = 36)
    private String oid;

    @ManyToOne(optional = false, fetch = fetchType.LAZY)
    @DescriptonList
    @Required
    private Product product;

    @Enumerated(EnumType.STRING)
    @Required
    private TipoMovimiento tipoMovimiento;

    @Required
    private Integer cantidad;

    @Temporal(twmporalType.DATE)
    @Required
    private Date date;

    @ManyToOne(fetch = fetchType.LAZY)
    @DescriptonList
    @Required
    private User user;

    @ManyToOne(fetch = fetchType.LAZY)
    @DescriptonList
    private Provider provider;

    @ManyToOne(fetch = fetchType.LAZY)
    @DescriptonList
    private Clientt client;

    @TextArea
    private String obervaciones;

}
