package univh2.fstm.gestionimmobilier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Entity
public class Reclamation extends FileEntity{

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id", nullable = false)
    @JsonIgnore
    @NotNull(message = "Le contrat est obligatoire")
    private Contrat contrat;




    @Enumerated(EnumType.STRING)
    @Column(name = "type_reclamation", nullable = false, length = 20)
    @NotNull(message = "Le type de reclamation est obligatoire")
    private TypeReclamation typeReclamation;

    @Column(nullable = false, length = 150)
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "La description est obligatoire")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "La priorité est obligatoire")
    private PrioriteReclamation priorite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Le statut est obligatoire")
    private StatutReclamation statut;


    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;


    @ElementCollection
    @CollectionTable(
            name = "reclamation_photos",
            joinColumns = @JoinColumn(name = "reclamation_id")
    )
    @Column(name = "photo_url", columnDefinition = "TEXT", length = 5000)
    @Builder.Default
    private List<String> photos = new ArrayList<>();



    @OneToMany(
            mappedBy = "reclamation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<MaintenanceTicket> maintenanceTickets = new ArrayList<>();

}
