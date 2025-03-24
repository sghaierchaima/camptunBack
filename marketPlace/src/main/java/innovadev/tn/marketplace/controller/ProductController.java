package innovadev.tn.marketplace.controller;

import innovadev.tn.marketplace.entity.Product;
import innovadev.tn.marketplace.service.ProductServiceImpli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {
    @Autowired
    private ProductServiceImpli productServiceImpli;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    // Créer un product
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<Product> createProgram(
            @RequestParam Long userId,
            @RequestBody Product product,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String jwtToken = authorizationHeader.replace("Bearer ", "");  // Récupérer le token JWT
            Product createdProgram = productServiceImpli.createProduct(userId, jwtToken, product);
            return ResponseEntity.ok(createdProgram);  // Retourner le programme créé avec l'ID généré
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(null);  // Interdit si l'utilisateur n'est pas admin
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);  // Erreur interne du serveur
        }
    }





    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productServiceImpli.getAllProducts());
    }
    @PreAuthorize("hasRole('NORMAL_USER')")
    @PostMapping("/buy/{productId}")
    public ResponseEntity<String> buyProduct(
            @PathVariable Long productId,
            @RequestParam Long userId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String jwtToken = authorizationHeader.replace("Bearer ", "");
            productServiceImpli.buyProduct(userId, jwtToken, productId);
            return ResponseEntity.ok("✅ Achat réussi !");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Erreur interne lors de l'achat.");
        }
    }


    @PostMapping("/{productId}/upload-image")
    public ResponseEntity<?> uploadImage(@PathVariable Long productId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                logger.warn("Tentative d'upload d'un fichier vide.");
                return ResponseEntity.badRequest().body("Le fichier est vide !");
            }

            // 📂 Dossier où enregistrer l’image
            String uploadsDir = System.getProperty("user.dir") + "/marketplace/uploads/";
            Path uploadPath = Paths.get(uploadsDir);

            // ✅ Création du dossier s'il n'existe pas
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Création du répertoire : {}", uploadsDir);
            }

            // 🔹 Génération du nom du fichier
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            logger.info("Fichier téléchargé et sauvegardé à : {}", filePath.toString());

            // ✅ Stocker l’URL dans la BD (chemin public)
            String fileUrl = "/products/uploads/" + fileName;
            productServiceImpli.addImageToProduct(productId, fileUrl);
            logger.info("URL de l'image sauvegardée dans la base de données : {}", fileUrl);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", fileUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Erreur lors de l'upload de l'image : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'upload de l'image.");
        }
    }



    // Rendre les images accessibles via une URL
    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(System.getProperty("user.dir") + "/marketPlace/uploads").resolve(filename).normalize();
            System.out.println("🔍 Tentative de chargement de l'image à : " + filePath.toString()); // Vérifie le chemin dans les logs

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // Ajuste le type MIME si nécessaire
                        .body(resource);
            } else {
                System.err.println("❌ Image non trouvée ou illisible");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de l'image : " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }


    // Récupérer un programme de camping par ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProgramById(@PathVariable Long id) {
        Product program = productServiceImpli.getProgramById(id);
        return program != null ? ResponseEntity.ok(program) : ResponseEntity.notFound().build();
    }


    @GetMapping("/use/{adminId}")
    public ResponseEntity<List<Product>> getCampingProgramsByUserId(@PathVariable Long adminId) {
        System.out.println("🔍 Récupération des produits pour l'admin avec ID : " + adminId);
        List<Product> products = productServiceImpli.getCampingProgramsByUserId(adminId);
        return ResponseEntity.ok(products);
    }

}
