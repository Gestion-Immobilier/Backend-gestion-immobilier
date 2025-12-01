package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
    @NotBlank
    private String email;

    private String phone;
    private String adresse;
}
