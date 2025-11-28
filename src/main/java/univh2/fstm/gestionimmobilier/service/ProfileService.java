package univh2.fstm.gestionimmobilier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.dto.ChangePasswordRequest;
import univh2.fstm.gestionimmobilier.dto.UpdateProfileRequest;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final PersonneRepository repo;
    private final PasswordEncoder passwordEncoder;

    // Récupérer le profil de l'utilisateur connecté
    public Personne getProfile(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // Mettre à jour le profil
    public Personne updateProfile(String email, UpdateProfileRequest request) {
        Personne user = getProfile(email);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAdresse(request.getAdresse());

        return repo.save(user);
    }

    // Changer mot de passe
    public void changePassword(String email, ChangePasswordRequest request) {

        Personne user = getProfile(email);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        repo.save(user);
    }
}
