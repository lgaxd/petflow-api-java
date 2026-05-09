package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.RewardPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardPointRepository extends JpaRepository<RewardPoint, Long> {

    List<RewardPoint> findByTutorId(Long tutorId);
}