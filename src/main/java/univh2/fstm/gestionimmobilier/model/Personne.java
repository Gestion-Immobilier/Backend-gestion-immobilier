package univh2.fstm.gestionimmobilier.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Personne extends AuditEntity {

    @Enumerated(EnumType.STRING)
    private Type type;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    private Boolean verified;

    private String adresse;

    private String password;
}
