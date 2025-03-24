package innovadev.tn.marketplace.service;

import innovadev.tn.marketplace.entity.Cart;
import innovadev.tn.marketplace.entity.CartProduct;
import innovadev.tn.marketplace.entity.Product;
import innovadev.tn.marketplace.repository.CartProductRepository;
import innovadev.tn.marketplace.repository.CartRepository;
import innovadev.tn.marketplace.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartProductRepository cartProductRepository;

    public void addProductToCart(Long userId, Long productId, Integer quantity) {
        // Vérifier si l'utilisateur a déjà un panier
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            // Si aucun panier n'est trouvé, créez un nouveau panier pour l'utilisateur
            Cart newCart = new Cart();
            newCart.setUserId(userId); // Assurez-vous que `userId` est défini
            return cartRepository.save(newCart); // Sauvegardez le nouveau panier dans la base de données
        });

        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Vérifier si le produit est déjà dans le panier
        CartProduct existingCartProduct = cartProductRepository.findByCartAndProduct(cart, product);

        if (existingCartProduct != null) {
            // Si le produit existe, mettre à jour la quantité
            existingCartProduct.setQuantity(existingCartProduct.getQuantity() + quantity);
            cartProductRepository.save(existingCartProduct);
        } else {
            // Sinon, créer un nouvel enregistrement CartProduct
            CartProduct newCartProduct = new CartProduct();
            newCartProduct.setCart(cart);
            newCartProduct.setProduct(product);
            newCartProduct.setQuantity(quantity);
            cartProductRepository.save(newCartProduct);
        }
    }

    // Récupérer le panier d'un utilisateur
    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Panier non trouvé pour cet utilisateur"));
    }

    // Méthode pour supprimer un produit du panier
    public void removeProductFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Panier non trouvé pour cet utilisateur"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        CartProduct cartProduct = cartProductRepository.findByCartAndProduct(cart, product);
        if (cartProduct != null) {
            cartProductRepository.delete(cartProduct); // Supprime le produit du panier
        } else {
            throw new RuntimeException("Le produit n'existe pas dans le panier");
        }
    }
    // Méthode pour vider le panier
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Panier non trouvé pour cet utilisateur"));

        // Supprimer toutes les relations CartProduct associées à ce panier
        cartProductRepository.deleteByCart(cart); // Supprime tous les produits du panier

        // Optionnel : Supprimer le panier après suppression des produits
        cartRepository.delete(cart);
    }


    // Méthode pour mettre à jour la quantité d'un produit dans le panier
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Panier non trouvé pour cet utilisateur"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        CartProduct cartProduct = cartProductRepository.findByCartAndProduct(cart, product);
        if (cartProduct != null) {
            cartProduct.setQuantity(quantity); // Met à jour la quantité
            cartProductRepository.save(cartProduct);
        } else {
            throw new RuntimeException("Le produit n'existe pas dans le panier");
        }
    }
}

