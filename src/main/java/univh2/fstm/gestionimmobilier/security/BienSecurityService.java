package univh2.fstm.gestionimmobilier.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import univh2.fstm.gestionimmobilier.repository.BienRepository;

@Service("bienSecurityService")
@RequiredArgsConstructor
@Slf4j
public class BienSecurityService {

    private final BienRepository bienRepository;
    private final JwtService jwtService;


    // on recupere l'id du user depuis token
    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtService.extractUserId(token);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'ID utilisateur", e);
        }
        return null;
    }


    /**
     * Vérifie si l'utilisateur connecté est le propriétaire du bien
     */
    public boolean isProprietaireDuBien(Long bienId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Long userIdConnecte = getCurrentUserId();
        if (userIdConnecte == null) {
            return false;
        }

        return bienRepository.findById(bienId)
                .map(bien -> bien.getProprietaire().getId().equals(userIdConnecte))
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
        Long userIdConnecte = getCurrentUserId();

        return proprietaireId.equals(userIdConnecte);
    }

}