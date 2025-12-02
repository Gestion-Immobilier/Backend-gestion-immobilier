package univh2.fstm.gestionimmobilier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
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

    @OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Bien> biens = new ArrayList<>();

    @PrePersist
    public void initDefaults() {
        if (type == null) {
            type = Type.LOCATAIRE; // Par défaut
        }
    }
}
