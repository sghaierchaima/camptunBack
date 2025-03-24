package innovadev.tn.userservice.controller;

import innovadev.tn.userservice.dto.AuthRequest;
import innovadev.tn.userservice.entity.User;
import innovadev.tn.userservice.service.IUserService;
import innovadev.tn.userservice.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final IUserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthController(IUserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthRequest authRequest) {
        System.out.println("Requête de connexion reçue pour : " + authRequest.getUsername());

        // Charger les détails de l'utilisateur
        UserDetails validUser = userService.loadUserByUsername(authRequest.getUsername());
        if (validUser == null) {
            System.out.println("Aucun utilisateur trouvé avec le nom d'utilisateur : " + authRequest.getUsername());
            return ResponseEntity.status(401).build();  // Non autorisé si l'utilisateur est introuvable
        }

        // Vérifier le mot de passe
        if (passwordEncoder.matches(authRequest.getPassword(), validUser.getPassword())) {
            // Extraire le rôle de l'utilisateur à partir des autorités
            String role = validUser.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority())
                    .orElse("NORMAL_USER");  // Rôle par défaut

            System.out.println("Rôle de l'utilisateur : " + role);

            // Générer le token JWT avec le rôle
            String token = jwtUtil.generateToken(authRequest.getUsername(), role);

            // Récupérer l'ID de l'utilisateur depuis la base de données
            Optional<User> userOptional = userService.findByUsername(authRequest.getUsername());

            if (userOptional.isEmpty()) {
                System.out.println("Erreur : L'utilisateur existe en mémoire mais pas en base de données !");
                return ResponseEntity.status(500).build();
            }

            User user = userOptional.get();

            // Construire la réponse avec le token + l'ID utilisateur
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("role", role);// Ajout de l'ID utilisateur

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).build();  // Non autorisé si le mot de passe ne correspond pas
    }
}
