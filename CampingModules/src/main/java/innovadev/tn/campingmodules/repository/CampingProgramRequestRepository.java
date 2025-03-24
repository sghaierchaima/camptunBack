package innovadev.tn.campingmodules.repository;

import innovadev.tn.campingmodules.entity.CampingProgramRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CampingProgramRequestRepository extends JpaRepository<CampingProgramRequest, Long> {
    List<CampingProgramRequest> findByProgramId(Long programId);  // Récupérer toutes les demandes pour un programme
    Optional<CampingProgramRequest> findByUserIdAndProgramId(Long userId, Long programId);  // Récupérer une demande par utilisateur et programme
}