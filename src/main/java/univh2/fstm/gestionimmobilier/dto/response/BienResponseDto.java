package univh2.fstm.gestionimmobilier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import univh2.fstm.gestionimmobilier.model.StatutBien;
import univh2.fstm.gestionimmobilier.model.StatutValidation;
import univh2.fstm.gestionimmobilier.model.TypeBien;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienResponseDto {
    private Long id;

    private String reference;

    private TypeBien typeBien;
    private StatutValidation statutValidation;
    private String motifRejet;

    private String adresse;

    private String ville;

    private String codePostal;

    private Double surface;

    private Integer nombrePieces;

    private Integer nombreChambres;

    private Integer nombreSallesBain;

    private String description;

    private BigDecimal loyerMensuel;

    private BigDecimal charges;

    private BigDecimal caution;

    private StatutBien statut;

    private LocalDate dateAcquisition;

    private List<String> photos;

    private Boolean meuble;

    private Boolean balcon;

    private Boolean parking;

    private Boolean ascenseur;

    private Long proprietaireId;
    private String proprietaireNom;
    private String proprietaireEmail;
}
