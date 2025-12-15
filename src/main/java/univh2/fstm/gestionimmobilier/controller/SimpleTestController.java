package univh2.fstm.gestionimmobilier.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/simple-test")
public class SimpleTestController {

    @GetMapping("/hello")
    public String hello() {
        return "{\"message\": \"Backend fonctionne!\"}";
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String text) {
        return "{\"echo\": \"" + text + "\"}";
    }
}