package innovadev.tn.userservice.entity;



import innovadev.tn.userservice.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Détails de l'utilisateur pour l'authentification")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String name;
    private String prename;
    private String phoneNumber;
    private LocalDate birthDate;
    private LocalDate signUpDate;
    @ElementCollection
    @CollectionTable(name = "user_camping_programs", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "camping_program_id")
    private List<Long> organizedProgramIds;

    @ElementCollection
    @CollectionTable(name = "admin_product", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "product_id")
    private List<Long> adminProductIds;
}
