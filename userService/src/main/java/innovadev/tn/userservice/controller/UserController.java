package innovadev.tn.userservice.controller;

import innovadev.tn.userservice.entity.User;
import innovadev.tn.userservice.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/users")
@Tag(name = "User API", description = "Gestion des utilisateurs")
@RequiredArgsConstructor
public class UserController {

    private final IUserService iuserService;

    @Operation(summary = "Créer un utilisateur", description = "Ajoute un nouvel utilisateur à la base de données.")
    @PostMapping("/add")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(iuserService.createUser(user));
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/getuser/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        System.out.println("Tentative d'accès à /getuser/" + id);
        return iuserService.getUserById(id)
                .map(user -> {
                    System.out.println("Utilisateur trouvé : " + user.getUsername());
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    System.out.println("Utilisateur non trouvé pour l'ID : " + id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(iuserService.getAllUsers());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = iuserService.updateUser(id, updatedUser);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }
    @PutMapping("/updatee/{id}")
    public ResponseEntity<User> updateAdmin(@PathVariable Long id, @RequestBody User updatedAdmin) {
        User admin = iuserService.updateAdmin(id, updatedAdmin);
        return admin != null ? ResponseEntity.ok(admin) : ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        iuserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
