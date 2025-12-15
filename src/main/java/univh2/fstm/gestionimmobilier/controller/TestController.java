package univh2.fstm.gestionimmobilier.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    // ✅ Accessible SANS token
    @GetMapping("/public")
    public String publicEndpoint() {
        return "{\"message\": \"Public endpoint - accessible sans token\"}";
    }
    
    // 🔒 Nécessite un token
    @GetMapping("/secure")
    @PreAuthorize("hasRole('USER')")  // Nécessite authentication
    public String secureEndpoint() {
        return "{\"message\": \"Secure endpoint - besoin de token\"}";
    }
}