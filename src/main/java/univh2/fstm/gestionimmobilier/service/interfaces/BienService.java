package univh2.fstm.gestionimmobilier.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.request.BienRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.BienResponseDto;
import univh2.fstm.gestionimmobilier.dto.request.BienValidationDto;
import univh2.fstm.gestionimmobilier.model.StatutBien;
import univh2.fstm.gestionimmobilier.model.StatutValidation;
import univh2.fstm.gestionimmobilier.model.TypeBien;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

public interface BienService {

    BienResponseDto creerBien(BienRequestDto requestDto, List<MultipartFile> photos);
    BienResponseDto updateBien(Long id, BienRequestDto requestDto,List<MultipartFile> photos);
    BienResponseDto getBienById(Long id);
    BienResponseDto getBienByReference(String reference);
    List<BienResponseDto> getAllBiens();


    BienResponseDto changerStatutBien(Long id, StatutBien nouveauStatut);
    void deleteBien(Long id);

    // validation pour l'agent

    List<BienResponseDto> getBiensEnAttente();
    BienResponseDto validerBien(Long id, BienValidationDto validationDto);
    long compterBiensEnAttente();

    List<BienResponseDto> getBiensByProprietaire(Long proprietaireId);


    // Recherche pour les clients

    List<BienResponseDto> getBiensPublics();
    Page<BienResponseDto> getBiensPublicsPagines(Pageable pageable);
    
    List<BienResponseDto> rechercherParVille(String ville);
    Page<BienResponseDto> rechercherParVillePaginee(String ville, Pageable pageable);
    
    List<BienResponseDto> rechercherParType(TypeBien typeBien);
    
    List<BienResponseDto> rechercheAvancee(
            String ville,
            TypeBien typeBien,
            BigDecimal prixMin,
            BigDecimal prixMax
    );
    Page<BienResponseDto> rechercheAvanceePaginee(
            String ville,
            TypeBien typeBien,
            BigDecimal prixMin,
            BigDecimal prixMax,
            Pageable pageable
    );

    long compterBiensParStatutValidation(StatutValidation statutValidation);
}
