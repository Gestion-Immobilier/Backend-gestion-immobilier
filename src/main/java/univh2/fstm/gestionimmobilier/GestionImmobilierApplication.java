package univh2.fstm.gestionimmobilier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class GestionImmobilierApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionImmobilierApplication.class, args);
    }

}
