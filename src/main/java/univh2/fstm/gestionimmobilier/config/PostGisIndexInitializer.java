package univh2.fstm.gestionimmobilier.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PostGisIndexInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initSpatialIndex() {
        try {
            log.info("Vérification/Création de l'index spatial GiST sur la table Bien...");
            // Exécution d'une requête native sécurisée (IF NOT EXISTS)
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_bien_localisation ON bien USING GIST (localisation)");
            log.info("✅ Index spatial GiST prêt !");
        } catch (Exception e) {
            log.warn("⚠️ Impossible de créer l'index spatial. Si la table 'bien' n'existe pas encore, Spring le fera, mais l'index devra être créé manuellement plus tard.", e.getMessage());
        }
    }
}
