package innovadev.tn.marketplace.service;

import innovadev.tn.marketplace.dto.User;
import innovadev.tn.marketplace.entity.Product;
import innovadev.tn.marketplace.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpli {

    @Autowired
    private ProductRepository productRepository;
    private String userServiceUrl = "http://localhost:8082/camptn/users";
    @Autowired
    private RestTemplate restTemplate;

    // Vérifie si l'utilisateur est un administrateur
    public boolean isUserAdmin(Long userId, String jwtToken) {
        String url = userServiceUrl + "/getuser/" + userId;
        System.out.println("🔍 Vérification du rôle de l'utilisateur via : " + url);
        System.out.println("🔑 Token utilisé : " + jwtToken);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<User> response = restTemplate.exchange(url, HttpMethod.GET, entity, User.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User user = response.getBody();
                System.out.println("✅ Utilisateur trouvé : " + user.getUsername() + ", Rôle : " + user.getRole());

                return user != null && user.getRole() != null && user.getRole().contains("ADMIN");

            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }

        System.out.println("🚫 Utilisateur non trouvé ou rôle incorrect.");
        return false;
    }





    // Crée un programme de camping si l'utilisateur est administrateur
    public Product createProduct(Long userId, String jwtToken, Product product) {
        if (!isUserAdmin(userId, jwtToken)) {
            throw new IllegalArgumentException("L'utilisateur n'a pas le rôle d'administrateur");
        }

        // Attribuer l'admin au produit
        product.setAdminId(userId);

        // Enregistrer le produit dans la base de données
        Product savedProduct = productRepository.save(product);

        // Ajouter l'ID du produit à la liste adminProductIds de l'admin
        String url = userServiceUrl + "/getuser/" + userId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Récupérer l'utilisateur pour lier le produit
            ResponseEntity<User> response = restTemplate.exchange(url, HttpMethod.GET, entity, User.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User user = response.getBody();
                if (user.getAdminProductIds() == null) {
                    user.setAdminProductIds(new ArrayList<>());
                }

                // Ajouter l'ID du produit créé à la liste adminProductIds de l'utilisateur
                user.getAdminProductIds().add(savedProduct.getId());

                // Mettre à jour l'utilisateur en envoyant une requête PUT au service User
                HttpEntity<User> updateEntity = new HttpEntity<>(user, headers);
                restTemplate.exchange(userServiceUrl + "/updatee/" + userId, HttpMethod.PUT, updateEntity, User.class);

                System.out.println("✅ Product ajouté avec succès à la liste de l'administrateur. ID produit : " + savedProduct.getId());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }

        return savedProduct;
    }





    public Product addProduct(Product product) {
        return productRepository.save(product);
    }


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }




    public void buyProduct(Long userId, String jwtToken, Long productId) {
        // Vérifier si l'utilisateur est valide
        String url = userServiceUrl + "/getuser/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<User> response = restTemplate.exchange(url, HttpMethod.GET, entity, User.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User user = response.getBody();
                System.out.println("✅ Utilisateur trouvé : " + user.getUsername());

                // Vérifier si le produit existe et est disponible
                Optional<Product> productOpt = productRepository.findById(productId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();

                    if (product.getStockQuantity() > 0) {
                        // Décrémenter le stock du produit
                        product.setStockQuantity(product.getStockQuantity() - 1);
                        productRepository.save(product);
                        System.out.println("🛒 Achat réussi pour le produit : " + product.getName());
                    } else {
                        throw new IllegalArgumentException("🚫 Stock insuffisant !");
                    }
                } else {
                    throw new IllegalArgumentException("❌ Produit introuvable !");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'achat : " + e.getMessage());
        }
    }



    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }


    @Transactional
    public Product addImageToProduct(Long productId, String imageUrl) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new IllegalArgumentException("Programme introuvable avec l'ID : " + productId);
        }

        // Mettre à jour l'URL de l'image
        product.setImageUrl(imageUrl);

        // Enregistrer les modifications dans la base de données
        return productRepository.save(product);
    }
    // Récupérer un programme de camping par ID
    public Product getProgramById(Long id) {

        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getCampingProgramsByUserId(Long adminId) {
        return productRepository.findByAdminId(adminId);
    }

}
