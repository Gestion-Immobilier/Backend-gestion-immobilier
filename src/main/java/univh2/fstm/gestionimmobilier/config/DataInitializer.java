package univh2.fstm.gestionimmobilier.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.Type;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PersonneRepository personneRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        // Vérifier si un admin existe déjà
        boolean adminExists = personneRepository.existsByTypeAndEmail(Type.ADMIN, "admin@gestion-immobilier.com");

        if (!adminExists) {
            log.info("🔧 Aucun admin trouvé. Création de l'admin par défaut...");

            Personne admin = new Personne();
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setEmail("admin@gestion-immobilier.com");
            admin.setPhone("0600000045");
            admin.setType(Type.ADMIN);
            admin.setAdresse("Système");
            admin.setPassword(passwordEncoder.encode("Admin@123"));  // Mot de passe par défaut

            personneRepository.save(admin);

            log.info("✅ Admin créé avec succès !");
            log.info("📧 Email: admin@gestion-immobilier.com");
            log.info("🔑 Mot de passe: Admin@123");
        } else {
            log.info("ℹ️ Admin déjà existant dans la base de données");
        }
    }
}