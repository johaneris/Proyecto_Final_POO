package ni.com.uam.Inventary.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
public class User {
    @Id
    @Column(length = 20)
    @Required
    private String nameUser;

    @Column(length = 120)
    @Password
    @Required
    private String password;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @DescriptionsList
    @Required
    private Rol rol;
}
