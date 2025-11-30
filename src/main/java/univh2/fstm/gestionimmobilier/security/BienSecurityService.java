package univh2.fstm.gestionimmobilier.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.model.Bien;
import univh2.fstm.gestionimmobilier.repository.BienRepository;

@Service("bienSecurityService")
@RequiredArgsConstructor
public class BienSecurityService {

    private final BienRepository bienRepository;

    /**
     * Vérifie si l'utilisateur connecté est le propriétaire du bien
     */
    public boolean isProprietaireDuBien(Long bienId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String emailConnecte = auth.getName();  // Email de l'utilisateur connecté

        return bienRepository.findById(bienId)
                .map(bien -> bien.getProprietaire().getEmail().equals(emailConnecte))
                .orElse(false);
    }

    /**
     * Vérifie si l'utilisateur connecté a cet ID de propriétaire
     */
    public boolean isProprietaire(Long proprietaireId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // Récupère l'ID de l'utilisateur depuis le token (ton binôme doit avoir ça)
        // Adapte selon comment elle stocke l'ID dans le token JWT
        Long userIdConnecte = getUserIdFromAuth(auth);

        return proprietaireId.equals(userIdConnecte);
    }

    private Long getUserIdFromAuth(Authentication auth) {
        // TODO: Demande à ton binôme comment récupérer l'ID depuis le JWT
        // Exemple possible :
        // return ((UserDetailsImpl) auth.getPrincipal()).getId();
        return null;  // À adapter
    }
}