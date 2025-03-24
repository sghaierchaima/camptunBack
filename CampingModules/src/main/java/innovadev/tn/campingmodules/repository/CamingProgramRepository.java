package innovadev.tn.campingmodules.repository;

import innovadev.tn.campingmodules.entity.CamingProgram;
import innovadev.tn.campingmodules.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CamingProgramRepository extends JpaRepository<CamingProgram, Long> {
    List<CamingProgram> findByOrganizerId(Long organizerId);
    List<CamingProgram> findByCategory(Category category);

}
