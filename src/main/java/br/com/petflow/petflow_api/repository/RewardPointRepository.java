package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.RewardPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RewardPointRepository extends JpaRepository<RewardPoint, Long> {

    Page<RewardPoint> findByTutorId(Long tutorId, Pageable pageable);
 
    @Query("SELECT COALESCE(SUM(rp.points), 0) FROM RewardPoint rp WHERE rp.tutor.id = :tutorId")
    Integer sumPointsByTutorId(@Param("tutorId") Long tutorId);
}
 