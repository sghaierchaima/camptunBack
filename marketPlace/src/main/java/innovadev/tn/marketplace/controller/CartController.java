package innovadev.tn.marketplace.controller;

import innovadev.tn.marketplace.entity.Cart;
import innovadev.tn.marketplace.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {
    @Autowired
    private CartService cartService;



    // Ajout d'un produit au panier avec la quantité
    @PreAuthorize("hasRole('NORMAL_USER')")
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addToCart(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {

        try {
            cartService.addProductToCart(userId, productId, quantity);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Produit ajouté au panier !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de l'ajout au panier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PreAuthorize("hasRole('NORMAL_USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {
        // Récupère le panier de l'utilisateur
        Cart cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    // Endpoint pour supprimer un produit du panier
    @PreAuthorize("hasRole('NORMAL_USER')")
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> removeProductFromCart(
            @RequestParam Long userId,
            @RequestParam Long productId) {
        try {
            cartService.removeProductFromCart(userId, productId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Produit supprimé du panier !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la suppression du produit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Endpoint pour vider le panier
    @PreAuthorize("hasRole('NORMAL_USER')")
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart(@RequestParam Long userId) {
        try {
            cartService.clearCart(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le panier a été vidé !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors du vidage du panier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PreAuthorize("hasRole('NORMAL_USER')")
    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateQuantity(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        try {
            cartService.updateQuantity(userId, productId, quantity);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Quantité mise à jour !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la mise à jour de la quantité: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


}
