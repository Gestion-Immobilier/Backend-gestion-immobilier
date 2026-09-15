package univh2.fstm.gestionimmobilier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.response.AuthResponse;
import univh2.fstm.gestionimmobilier.dto.request.AuthRequest;
import univh2.fstm.gestionimmobilier.dto.request.RegisterRequest;
import univh2.fstm.gestionimmobilier.dto.request.TokenRefreshRequest;
import univh2.fstm.gestionimmobilier.service.impl.AuthService;
import univh2.fstm.gestionimmobilier.service.impl.RefreshTokenService;
import univh2.fstm.gestionimmobilier.model.RefreshToken;
import univh2.fstm.gestionimmobilier.security.JwtService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refreshtoken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getPersonne)
                .map(personne -> {
                    String token = jwtService.generateToken(personne);
                    return new AuthResponse(token, requestRefreshToken);
                })
                .orElseThrow(() -> new RuntimeException(
                        "Le refresh token n'est pas en base de données!"));
    }
}
