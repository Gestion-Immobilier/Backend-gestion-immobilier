package univh2.fstm.gestionimmobilier.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.request.DemandeResiliationRequestDto;
import univh2.fstm.gestionimmobilier.dto.request.TraiterDemandeResiliationDto;
import univh2.fstm.gestionimmobilier.dto.response.DemandeResiliationResponseDto;
import univh2.fstm.gestionimmobilier.model.StatutDemandeResiliation;

import java.util.List;

public interface DemandeResiliationService {
    DemandeResiliationResponseDto creerDemande(DemandeResiliationRequestDto requestDto);

    List<DemandeResiliationResponseDto> getMesDemandesResiliation();

    DemandeResiliationResponseDto getDemandeById(Long id);

    void annulerDemande(Long id);

    // Admin
    List<DemandeResiliationResponseDto> getAllDemandes();

    List<DemandeResiliationResponseDto> getDemandesByStatut(StatutDemandeResiliation statut);

    DemandeResiliationResponseDto traiterDemande(Long id, TraiterDemandeResiliationDto traiterDto);

    long countDemandesEnAttente();
}


