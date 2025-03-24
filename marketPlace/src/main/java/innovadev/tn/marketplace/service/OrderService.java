package innovadev.tn.marketplace.service;

import innovadev.tn.marketplace.entity.*;
import innovadev.tn.marketplace.enums.OrderStatus;
import innovadev.tn.marketplace.exception.InsufficientStockException;
import innovadev.tn.marketplace.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartProductRepository cartProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order placeOrder(Long userId, String paymentMethod, String deliveryMethod) {
        // Vérifier si l'utilisateur a un panier
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Panier non trouvé pour l'utilisateur ID : " + userId));

        // Récupérer les produits du panier
        List<CartProduct> cartProducts = cartProductRepository.findByCart(cart);
        if (cartProducts.isEmpty()) {
            throw new RuntimeException("Le panier est vide !");
        }

        // Vérifier la disponibilité des produits en stock
        for (CartProduct cartProduct : cartProducts) {
            Product product = cartProduct.getProduct();
            if (product.getStockQuantity() < cartProduct.getQuantity()) { // ✅ Correction : getStock()
                throw new InsufficientStockException("Stock insuffisant pour le produit : " + product.getName());
            }
        }

        // Créer une nouvelle commande
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.EN_COURS);
        order.setPaymentMethod(paymentMethod);
        order.setDeliveryMethod(deliveryMethod);

        // Décrémenter les stocks avant d'ajouter les items
        for (CartProduct cartProduct : cartProducts) {
            Product product = cartProduct.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartProduct.getQuantity()); // ✅ Correction : getStock()
            productRepository.save(product);
        }

        // Créer les items de commande
        List<OrderItem> orderItems = cartProducts.stream().map(cartProduct ->
                        new OrderItem(null, order, cartProduct.getProduct().getId(),
                                cartProduct.getProduct().getName(),
                                cartProduct.getProduct().getPrice(),
                                cartProduct.getQuantity()))
                .collect(Collectors.toList());

        // Calculer le total
        double totalPrice = orderItems.stream().mapToDouble(item -> item.getProductPrice() * item.getQuantity()).sum();
        order.setTotalPrice(totalPrice);

        // Sauvegarder la commande et les items
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // Supprimer le panier après passage de la commande
        cartProductRepository.deleteByCart(cart);
        cartRepository.delete(cart);

        return order;
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        // Vérifier si la commande existe
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID : " + orderId));

        // Mettre à jour le statut
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}


