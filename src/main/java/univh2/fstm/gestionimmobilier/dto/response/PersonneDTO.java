package univh2.fstm.gestionimmobilier.dto.response;

import lombok.Data;
import univh2.fstm.gestionimmobilier.model.Type;

@Data
public class PersonneDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Type type;
    private String adresse;
    private String password;
}
