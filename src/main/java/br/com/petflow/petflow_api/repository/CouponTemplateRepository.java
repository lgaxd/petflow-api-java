package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {
}