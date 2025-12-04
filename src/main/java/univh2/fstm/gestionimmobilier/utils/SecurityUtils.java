package univh2.fstm.gestionimmobilier.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final PersonneRepository personneRepository;

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        String username = authentication.getName(); // Email ou username

        Personne user = personneRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return user.getId();
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

}
