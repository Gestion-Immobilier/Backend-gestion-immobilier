package univh2.fstm.gestionimmobilier.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import univh2.fstm.gestionimmobilier.model.Reclamation;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class ReclamationRepositoryIntegrationTest {

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
    private ReclamationRepository reclamationRepository;

    @Test
    void testFindReclamationsEnRetard() {
        // Tests the custom JPQL query for delayed reclamations
        List<Reclamation> reclamations = reclamationRepository.findReclamationsEnRetard(LocalDateTime.now());
        
        assertNotNull(reclamations);
        assertTrue(reclamations.isEmpty(), "La base devrait être vide");
    }
}
