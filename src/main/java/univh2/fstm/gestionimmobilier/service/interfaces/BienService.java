package univh2.fstm.gestionimmobilier.service.interfaces;

import univh2.fstm.gestionimmobilier.dto.request.BienRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.BienResponseDto;
import univh2.fstm.gestionimmobilier.dto.request.BienValidationDto;
import univh2.fstm.gestionimmobilier.model.StatutBien;
import univh2.fstm.gestionimmobilier.model.StatutValidation;
import univh2.fstm.gestionimmobilier.model.TypeBien;

import java.math.BigDecimal;
import java.util.List;

public interface BienService {

    BienResponseDto creerBien(BienRequestDto requestDto);
    BienResponseDto getBienById(Long id);
    BienResponseDto getBienByReference(String reference);
    List<BienResponseDto> getAllBiens();

    BienResponseDto updateBien(Long id, BienRequestDto requestDto);
    BienResponseDto changerStatutBien(Long id, StatutBien nouveauStatut);
    void deleteBien(Long id);

    // validation pour l'agent

    List<BienResponseDto> getBiensEnAttente();
    BienResponseDto validerBien(Long id, BienValidationDto validationDto);
    long compterBiensEnAttente();

    // Recherche pour les clients

    List<BienResponseDto> getBiensPublics();
    List<BienResponseDto> rechercherParVille(String ville);
    List<BienResponseDto> rechercherParType(TypeBien typeBien);
    List<BienResponseDto> rechercheAvancee(
            String ville,
            TypeBien typeBien,
            BigDecimal prixMin,
            BigDecimal prixMax
    );

    long compterBiensParStatutValidation(StatutValidation statutValidation);
}
