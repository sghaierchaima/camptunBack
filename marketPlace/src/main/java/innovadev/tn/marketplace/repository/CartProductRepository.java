package innovadev.tn.marketplace.repository;

import innovadev.tn.marketplace.entity.Cart;
import innovadev.tn.marketplace.entity.CartProduct;
import innovadev.tn.marketplace.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
    CartProduct findByCartAndProduct(Cart cart, Product product);

    void deleteByCart(Cart cart);

    List<CartProduct> findByCart(Cart cart);
}
