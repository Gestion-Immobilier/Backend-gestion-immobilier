package univh2.fstm.gestionimmobilier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Personne extends AuditEntity {

    @Enumerated(EnumType.STRING)
    private Type type;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    private Boolean verified = false; // PROPRIETAIRE → admin doit le valider

    private String adresse;

    private String password;

    // Un locataire peut demander à devenir propriétaire
    private Boolean demandeProprietaire = false;

    @PrePersist
    public void initDefaults() {
        if (type == null) {
            type = Type.LOCATAIRE; // Par défaut
        }
    }
}
