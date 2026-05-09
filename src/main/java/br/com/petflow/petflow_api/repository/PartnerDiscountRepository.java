package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.PartnerDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnerDiscountRepository extends JpaRepository<PartnerDiscount, Long> {

    List<PartnerDiscount> findByCategoryIgnoreCase(String category);
}