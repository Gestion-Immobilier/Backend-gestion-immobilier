package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest {
    @NotBlank(message = "Le refresh token est obligatoire")
    private String refreshToken;
}
