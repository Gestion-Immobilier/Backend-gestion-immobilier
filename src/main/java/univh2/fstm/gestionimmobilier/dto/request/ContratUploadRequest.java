package univh2.fstm.gestionimmobilier.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Wrapper pour création de contrat avec fichier")
public class ContratUploadRequest {

    @Schema(description = "Données du contrat au format JSON", required = true)
    private String contratJson;

    @Schema(description = "Document PDF", required = true)
    private MultipartFile document;
}