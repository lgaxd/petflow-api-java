package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.RewardPointResponseDTO;
import br.com.petflow.petflow_api.entity.RewardPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardPointRepository extends JpaRepository<RewardPoint, Long> {

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.RewardPointResponseDTO(
                rp.id, rp.points, rp.referenceType, rp.referenceId, rp.createdAt,
                rp.tutor.id, rp.tutor.name,
                rp.rewardAction.id, rp.rewardAction.name
            )
            FROM RewardPoint rp
            WHERE rp.tutor.id = :tutorId
            """)
    Page<RewardPointResponseDTO> findByTutorId(@Param("tutorId") Long tutorId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(rp.points), 0) FROM RewardPoint rp WHERE rp.tutor.id = :tutorId")
    Integer sumPointsByTutorId(@Param("tutorId") Long tutorId);
}