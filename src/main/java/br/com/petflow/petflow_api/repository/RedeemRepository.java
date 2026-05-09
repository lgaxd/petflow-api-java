package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.Redeem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedeemRepository extends JpaRepository<Redeem, Long> {

    List<Redeem> findByTutorId(Long tutorId);
}