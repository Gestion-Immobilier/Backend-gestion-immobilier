package univh2.fstm.gestionimmobilier.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import univh2.fstm.gestionimmobilier.model.Bien;
import univh2.fstm.gestionimmobilier.model.StatutBien;
import univh2.fstm.gestionimmobilier.model.StatutValidation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class BienRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Avoid Minio config issues in tests
        registry.add("minio.url", () -> "http://localhost:9000");
        registry.add("minio.access.name", () -> "minioadmin");
        registry.add("minio.access.secret", () -> "minioadmin");
        registry.add("minio.bucket-bien", () -> "biens");
        registry.add("minio.bucket-contrat", () -> "contrats");
        registry.add("minio.bucket-reclamation", () -> "reclamations");
    }

    @Autowired
    private BienRepository bienRepository;

    @Test
    void testContextLoadsAndQueriesAreValid() {
        // This acts as a smoke test for JPQL queries. 
        // If the context loads and this method executes without exception,
        // it means all custom @Query definitions in BienRepository are valid.
        List<Bien> biens = bienRepository.findByStatutValidationAndStatut(
                StatutValidation.VALIDE, StatutBien.DISPONIBLE);
        
        assertNotNull(biens);
        assertTrue(biens.isEmpty(), "La base devrait être vide");
    }
}
