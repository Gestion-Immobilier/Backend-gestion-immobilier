package univh2.fstm.gestionimmobilier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.response.AuthResponse;
import univh2.fstm.gestionimmobilier.dto.request.AuthRequest;
import univh2.fstm.gestionimmobilier.dto.request.RegisterRequest;
import univh2.fstm.gestionimmobilier.service.impl.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }
}
