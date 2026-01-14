package univh2.fstm.gestionimmobilier.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratLightDto {
    private Long id;
    private String reference;
    private String bienAdresse;
    private String locataireNom;
    private String locataireEmail;
}