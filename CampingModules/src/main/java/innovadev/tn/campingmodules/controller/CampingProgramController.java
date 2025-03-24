package innovadev.tn.campingmodules.controller;

import innovadev.tn.campingmodules.entity.CamingProgram;
import innovadev.tn.campingmodules.entity.CampingProgramRequest;
import innovadev.tn.campingmodules.enums.Category;
import innovadev.tn.campingmodules.service.CampingProgramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/campingprograms")
@CrossOrigin(origins = "http://localhost:4200")
public class CampingProgramController {
    @Autowired
    private CampingProgramService campingProgramService;
    private static final Logger logger = LoggerFactory.getLogger(CampingProgramController.class);

    // Créer un programme de camping
    @PreAuthorize("hasRole('NORMAL_USER')")
    @PostMapping("/add")
    public ResponseEntity<CamingProgram> createProgram(
            @RequestParam Long userId,
            @RequestBody CamingProgram program,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String jwtToken = authorizationHeader.replace("Bearer ", "");  // Récupérer le token JWT
            CamingProgram createdProgram = campingProgramService.createProgram(userId, jwtToken, program);
            return ResponseEntity.ok(createdProgram);  // Retourner le programme créé avec l'ID généré
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(null);  // Interdit si l'utilisateur n'est pas admin
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);  // Erreur interne du serveur
        }
    }


    // Récupérer tous les programmes de camping
    @GetMapping
    public ResponseEntity<List<CamingProgram>> getAllPrograms() {
        List<CamingProgram> programs = campingProgramService.getAllPrograms();
        return ResponseEntity.ok(programs);
    }

    // Récupérer un programme de camping par ID
    @GetMapping("/{id}")
    public ResponseEntity<CamingProgram> getProgramById(@PathVariable Long id) {
        CamingProgram program = campingProgramService.getProgramById(id);
        return program != null ? ResponseEntity.ok(program) : ResponseEntity.notFound().build();
    }

    // Mettre à jour un programme de camping
    @PutMapping("/{id}")
    public ResponseEntity<CamingProgram> updateProgram(@PathVariable Long id, @RequestBody CamingProgram updatedProgram) {
        CamingProgram program = campingProgramService.updateProgram(id, updatedProgram);
        return program != null ? ResponseEntity.ok(program) : ResponseEntity.notFound().build();
    }

    // Supprimer un programme de camping
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id) {
        campingProgramService.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }




    @PostMapping("/{campingProgramId}/upload-image")
    public ResponseEntity<?> uploadImage(@PathVariable Long campingProgramId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                logger.warn("Tentative d'upload d'un fichier vide.");
                return ResponseEntity.badRequest().body("Le fichier est vide !");
            }

            // 📂 Dossier où enregistrer l’image
            String uploadsDir = System.getProperty("user.dir") + "/campingmodules/uploads/";
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
            String fileUrl = "/campingprograms/uploads/" + fileName;
            campingProgramService.addImageToProgram(campingProgramId, fileUrl);
            logger.info("URL de l'image sauvegardée dans la base de données : {}", fileUrl);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", fileUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Erreur lors de l'upload de l'image : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'upload de l'image.");
        }
    }


    @GetMapping("/use/{userId}")
    public ResponseEntity<List<CamingProgram>> getCampingProgramsByUserId(@PathVariable Long userId) {
        List<CamingProgram> campingPrograms = campingProgramService.getCampingProgramsByUserId(userId);
        return ResponseEntity.ok(campingPrograms);
    }

    // Rendre les images accessibles via une URL
    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(System.getProperty("user.dir") + "/campingModules/uploads").resolve(filename).normalize();
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
    @GetMapping("/all")
    public ResponseEntity<?> getAllCampings() {
        try {
            List<CamingProgram> campings = campingProgramService.getAllCampings();
            return ResponseEntity.ok(campings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : " + e.getMessage());
        }
    }

    // Envoyer une demande pour rejoindre un programme
    @PostMapping("/{programId}/request")
    public ResponseEntity<?> requestToJoinProgram(@PathVariable Long programId,
                                                  @RequestParam Long userId,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String jwtToken = authorizationHeader.replace("Bearer ", "");

            // Vérifier si l'utilisateur est l'organisateur du programme
            CamingProgram program = campingProgramService.getProgramById(programId);
            if (program.getOrganizerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Vous ne pouvez pas envoyer une demande pour rejoindre votre propre programme.");
            }

            // Lancer la demande d'adhésion
            CampingProgramRequest request = campingProgramService.requestToJoinProgram(userId, programId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // Accepter une demande
    @PutMapping("/{programId}/request/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long programId, @PathVariable Long requestId,
                                           @RequestParam Long userId, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Vérifier si l'utilisateur est l'organisateur du programme
            CamingProgram program = campingProgramService.getProgramById(programId);
            if (!program.getOrganizerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'êtes pas autorisé à accepter cette demande.");
            }

            // Accepter la demande
            campingProgramService.acceptRequest(requestId);
            return ResponseEntity.ok("Demande acceptée avec succès.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Refuser une demande
    @PutMapping("/{programId}/request/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long programId, @PathVariable Long requestId,
                                           @RequestParam Long userId, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Vérifier si l'utilisateur est l'organisateur du programme
            CamingProgram program = campingProgramService.getProgramById(programId);
            if (!program.getOrganizerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'êtes pas autorisé à refuser cette demande.");
            }

            // Refuser la demande
            campingProgramService.rejectRequest(requestId);
            return ResponseEntity.ok("Demande refusée avec succès.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // Récupérer toutes les demandes pour un programme
    @GetMapping("/{programId}/requests")
    public ResponseEntity<List<CampingProgramRequest>> getRequestsForProgram(@PathVariable Long programId) {
        List<CampingProgramRequest> requests = campingProgramService.getRequestsForProgram(programId);
        return ResponseEntity.ok(requests);
    }
    @GetMapping("/categories")
    public List<Category> getAllCategories(@RequestHeader("Authorization") String token) {
        return campingProgramService.getAllCategories();
    }

    @GetMapping("/programs/category/{category}")
    public ResponseEntity<?> getProgramsByCategory(@PathVariable String category) {
        try {
            Category categoryEnum = Category.fromString(category);
            return ResponseEntity.ok(campingProgramService.getProgramsByCategory(categoryEnum));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Catégorie invalide : " + category);
        }
    }


}








