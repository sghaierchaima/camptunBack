package innovadev.tn.campingmodules.service;

import innovadev.tn.campingmodules.dto.User;
import innovadev.tn.campingmodules.entity.CamingProgram;
import innovadev.tn.campingmodules.entity.CampingProgramRequest;
import innovadev.tn.campingmodules.enums.Category;
import innovadev.tn.campingmodules.repository.CamingProgramRepository;
import innovadev.tn.campingmodules.repository.CampingProgramRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Optional;

@Service
public class CampingProgramService {

    @Autowired
    private CamingProgramRepository camingProgramRepository;
    @Autowired
    private CampingProgramRequestRepository requestRepository;

    @Autowired
    private RestTemplate restTemplate;

    private String userServiceUrl = "http://localhost:8082/camptn/users";  // URL de UserService

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

                return user != null && user.getRole() != null && user.getRole().contains("NORMAL_USER");

            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }

        System.out.println("🚫 Utilisateur non trouvé ou rôle incorrect.");
        return false;
    }


    // Crée un programme de camping si l'utilisateur est administrateur
    // Crée un programme de camping si l'utilisateur est administrateur
    public CamingProgram createProgram(Long userId, String jwtToken, CamingProgram program) {
        if (!isUserAdmin(userId, jwtToken)) {
            throw new IllegalArgumentException("L'utilisateur n'a pas le rôle d'administrateur");
        }

        // Attribuer l'organisateur au programme
        program.setOrganizerId(userId);

        // Enregistrer le programme dans la base de données
        CamingProgram savedProgram = camingProgramRepository.save(program);

        // Ajouter l'ID du programme à la liste organizedProgramIds de l'utilisateur
        String url = userServiceUrl + "/getuser/" + userId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<User> response = restTemplate.exchange(url, HttpMethod.GET, entity, User.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                User user = response.getBody();

                // Ajouter l'ID du programme créé à la liste des programmes organisés par l'utilisateur
                user.getOrganizedProgramIds().add(savedProgram.getId());

                // Mettre à jour l'utilisateur en envoyant une requête PUT au service User
                HttpEntity<User> updateEntity = new HttpEntity<>(user, headers);
                restTemplate.exchange(userServiceUrl + "/update/" + userId, HttpMethod.PUT, updateEntity, User.class);

                System.out.println("✅ Programme ajouté avec succès à la liste de l'utilisateur.");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }

        return savedProgram;
    }

    // Récupérer tous les programmes de camping
    public List<CamingProgram> getAllPrograms() {
        return camingProgramRepository.findAll();
    }

    // Récupérer un programme de camping par ID
    public CamingProgram getProgramById(Long id) {
        return camingProgramRepository.findById(id).orElse(null);
    }

    // Mettre à jour un programme de camping
    public CamingProgram updateProgram(Long id, CamingProgram updatedProgram) {
        if (camingProgramRepository.existsById(id)) {
            updatedProgram.setId(id);
            return camingProgramRepository.save(updatedProgram);
        }
        return null;
    }

    // Supprimer un programme de camping
    public void deleteProgram(Long id) {
        camingProgramRepository.deleteById(id);
    }

    // Ajouter une image à un programme
    @Transactional
    public CamingProgram addImageToProgram(Long programId, String imageUrl) {
        CamingProgram program = camingProgramRepository.findById(programId).orElse(null);
        if (program == null) {
            throw new IllegalArgumentException("Programme introuvable avec l'ID : " + programId);
        }

        // Mettre à jour l'URL de l'image
        program.setImageUrl(imageUrl);

        // Enregistrer les modifications dans la base de données
        return camingProgramRepository.save(program);
    }

    public List<CamingProgram> getCampingProgramsByUserId(Long userId) {
        return camingProgramRepository.findByOrganizerId(userId);
    }

    public List<CamingProgram> getAllCampings() {
        return camingProgramRepository.findAll();
    }


    public CampingProgramRequest requestToJoinProgram(Long userId, Long programId) {
        // Vérifier si la demande existe déjà
        if (requestRepository.findByUserIdAndProgramId(userId, programId).isPresent()) {
            throw new IllegalArgumentException("Vous avez déjà envoyé une demande pour rejoindre ce programme.");
        }

        // Créer une nouvelle demande
        CampingProgramRequest request = new CampingProgramRequest();
        request.setUserId(userId);
        request.setProgramId(programId);
        request.setStatus(CampingProgramRequest.RequestStatus.PENDING);

        return requestRepository.save(request);
    }
    // Accepter une demande
  /*  public void acceptRequest(Long requestId) {
        CampingProgramRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        request.setStatus(CampingProgramRequest.RequestStatus.ACCEPTED);
        requestRepository.save(request);
    }*/

   /* // Accepter une demande et incrémenter le nombre de participants
    public void acceptRequest(Long requestId) {
        CampingProgramRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        CamingProgram program = camingProgramRepository.findById(request.getProgramId())
                .orElseThrow(() -> new IllegalArgumentException("Programme introuvable"));

        // Vérifier si le programme a atteint son nombre maximal de participants
        if (program.getNParticipants() >= program.getMaxParticipants()) {
            throw new IllegalStateException("Le nombre maximal de participants a été atteint.");
        }

        // Changer le statut à ACCEPTED et enregistrer
        request.setStatus(CampingProgramRequest.RequestStatus.ACCEPTED);
        requestRepository.save(request);

        // Incrémenter le nombre de participants et enregistrer le programme mis à jour
        program.setNParticipants(program.getNParticipants() + 1);
        camingProgramRepository.save(program);
    }
*/
   // Accepter une demande de participation
   public void acceptRequest(Long requestId) {
       CampingProgramRequest request = requestRepository.findById(requestId)
               .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

       if (request.getStatus() == CampingProgramRequest.RequestStatus.ACCEPTED) {
           throw new IllegalStateException("Cette demande a déjà été acceptée.");
       }

       // Récupérer le programme concerné
       CamingProgram program = camingProgramRepository.findById(request.getProgramId())
               .orElseThrow(() -> new IllegalArgumentException("Programme introuvable"));

       if (program.getNParticipants() >= program.getMaxParticipants()) {
           throw new IllegalStateException("Le nombre maximal de participants a été atteint.");
       }

       // Accepter la demande et incrémenter le nombre de participants
       request.setStatus(CampingProgramRequest.RequestStatus.ACCEPTED);
       requestRepository.save(request);

       program.setNParticipants(program.getNParticipants() + 1);
       camingProgramRepository.save(program);
   }






    // Refuser une demande
    public void rejectRequest(Long requestId) {
        CampingProgramRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));

        request.setStatus(CampingProgramRequest.RequestStatus.REJECTED);
        requestRepository.save(request);
    }
    // Récupérer les demandes pour un programme spécifique
    public List<CampingProgramRequest> getRequestsForProgram(Long programId) {
        return requestRepository.findByProgramId(programId);
    }



    // Vérifie si l'utilisateur est propriétaire du programme
    public boolean isUserOwnerOfProgram(Long userId, Long programId) {
        Optional<CamingProgram> optionalProgram = camingProgramRepository.findById(programId);

        if (optionalProgram.isPresent()) {
            CamingProgram program = optionalProgram.get();
            return program.getOrganizerId().equals(userId);  // Vérifie si l'utilisateur est bien le propriétaire
        }

        return false; // Programme non trouvé, donc pas propriétaire
    }

    public List<Category> getAllCategories() {
        // Récupérer toutes les catégories de programmes
        return List.of(Category.values());
    }

    public List<CamingProgram> getProgramsByCategory(Category category) {
        return camingProgramRepository.findByCategory(category);
    }





}
