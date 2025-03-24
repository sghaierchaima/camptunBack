package innovadev.tn.marketplace.controller;

import innovadev.tn.marketplace.dto.User;
import innovadev.tn.marketplace.entity.Order;
import innovadev.tn.marketplace.enums.OrderStatus;
import innovadev.tn.marketplace.repository.OrderRepository;
import innovadev.tn.marketplace.service.EmailService;
import innovadev.tn.marketplace.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RestTemplate restTemplate;
    private static final String USER_SERVICE_URL = "http://localhost:8082/camptn/users/getuser/";

    @PreAuthorize("hasRole('NORMAL_USER')")
    @PostMapping("/place")
    public ResponseEntity<Map<String, String>> placeOrder(
            @RequestParam Long userId,
            @RequestParam String paymentMethod,
            @RequestParam String deliveryMethod) {
        try {
            orderService.placeOrder(userId, paymentMethod, deliveryMethod);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Commande passée avec succès !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors du passage de la commande: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PreAuthorize("hasRole('NORMAL_USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }



    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Map<String, String>> updateOrderStatus(@PathVariable Long orderId,
                                                                 @RequestParam String newStatus,
                                                                 @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Vérifier si le token est présent dans les en-têtes
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Token JWT manquant ou invalide");
            }

            // Extraire le token JWT
            String token = authorizationHeader.substring(7); // Retirer le "Bearer " du début du token

            // Charger la commande
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

            // Vérifier si le statut est valide
            OrderStatus status = OrderStatus.valueOf(newStatus);
            order.setStatus(status);
            orderRepository.save(order);

            // Récupérer l'utilisateur via l'API REST de UserService
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token); // Ajouter le token JWT dans les en-têtes
            HttpEntity<String> entity = new HttpEntity<>(headers);
            User user = restTemplate.exchange(USER_SERVICE_URL + "/" + order.getUserId(), HttpMethod.GET, entity, User.class).getBody();

            if (user == null) {
                throw new RuntimeException("Utilisateur non trouvé");
            }

            // Envoi de l'email à l'utilisateur
            String userEmail = user.getEmail();
            String subject = "Mise à jour du statut de votre commande";
            String text = "Bonjour,\n\nVotre commande (ID: " + order.getId() + ") a été mise à jour avec le statut suivant : " + status + ".\n\nMerci pour votre confiance.";
            emailService.sendEmail(userEmail, subject, text);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Statut de la commande mis à jour avec succès et un email a été envoyé à l'utilisateur.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la mise à jour du statut : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll(); // Récupérer toutes les commandes
            return ResponseEntity.ok(orders); // Retourne les commandes avec un code 200
        } catch (Exception e) {
            // Retourne un message d'erreur sous forme de Map
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des commandes : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse); // Code 500 pour l'erreur
        }
    }


}
