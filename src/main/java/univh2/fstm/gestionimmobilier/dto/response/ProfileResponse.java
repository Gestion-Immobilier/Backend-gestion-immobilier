package univh2.fstm.gestionimmobilier.dto.response;

import lombok.Builder;
import lombok.Data;
import univh2.fstm.gestionimmobilier.model.Type;

@Data
@Builder
public class ProfileResponse {
    private Type type;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean verified;
    private String adresse;
}
