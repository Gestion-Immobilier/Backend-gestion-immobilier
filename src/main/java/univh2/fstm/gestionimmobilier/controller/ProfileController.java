package univh2.fstm.gestionimmobilier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.ChangePasswordRequest;
import univh2.fstm.gestionimmobilier.dto.UpdateProfileRequest;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 🔹 Récupérer le profil du user connecté
    @GetMapping("/me")
    public Personne getProfile(Authentication auth) {
        String email = auth.getName();
        return profileService.getProfile(email);
    }

    // 🔹 Mettre à jour le profil
    @PutMapping("/update")
    public Personne updateProfile(@RequestBody UpdateProfileRequest request,
                                  Authentication auth) {
        String email = auth.getName();
        return profileService.updateProfile(email, request);
    }

    // 🔹 Changer mot de passe
    @PutMapping("/password")
    public String changePassword(@RequestBody ChangePasswordRequest request,
                                 Authentication auth) {
        String email = auth.getName();
        profileService.changePassword(email, request);
        return "Mot de passe changé avec succès";
    }
}
