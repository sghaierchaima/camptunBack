package innovadev.tn.marketplace.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class User {
    private Long id;
    private String username;
    private String role;
    private String email;
    private List<Long> adminProductIds = new ArrayList<>();
    // Ajouter cette ligne
}
