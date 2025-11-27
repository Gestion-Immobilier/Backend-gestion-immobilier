package univh2.fstm.gestionimmobilier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.dto.*;
import univh2.fstm.gestionimmobilier.mapper.PersonneMapper;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.Type;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;
import univh2.fstm.gestionimmobilier.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PersonneRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PersonneMapper mapper;

    // REGISTER
    public AuthResponse register(RegisterRequest request) {

        Personne p = new Personne();
        p.setFirstName(request.getFirstName());
        p.setLastName(request.getLastName());
        p.setEmail(request.getEmail());
        p.setPhone(request.getPhone());
        p.setAdresse(null);
        p.setType(request.getType() != null ? request.getType() : Type.LOCATAIRE);

        p.setPassword(passwordEncoder.encode(request.getPassword()));
        p.setVerified(false);

        repo.save(p);

        String token = jwtService.generateToken(p);

        return new AuthResponse(token);
    }

    // LOGIN
    public AuthResponse login(AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Personne user = repo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}
