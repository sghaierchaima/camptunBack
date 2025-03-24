package innovadev.tn.marketplace.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import innovadev.tn.marketplace.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDateTime orderDate;

    private Double totalPrice;

    @Enumerated(EnumType.STRING) // Utilisation d'une énumération pour plus de robustesse
    private OrderStatus status; // "EN_COURS", "VALIDEE", "EXPEDIEE"

    private String paymentMethod;  // Ex: "Carte Bancaire", "PayPal"
    private String deliveryMethod; // "En cours", "Validée", "Expédiée"

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> items;
}
