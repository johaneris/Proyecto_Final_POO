package ni.com.uam.Inventary.modelo;

import javax.persistence.*;
import javax.validation.constraints.Email;

import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
public class Supplier {
    @Id
    @Hidden
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid2")
    @Column(length = 36)
    private String oid;

    @Column(length = 80)
    @Required
    private String name;

    @Column(length = 20)
    @Telephone
    private String phone;

    @Column(length = 80)
    @Email
    private String email;

    @TextArea
    private String direction;

    @Column(length = 80)
    private String contacto;
}
