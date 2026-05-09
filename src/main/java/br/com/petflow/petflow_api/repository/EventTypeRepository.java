package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {

    List<EventType> findByCategoryIgnoreCase(String category);

    List<EventType> findByNameContainingIgnoreCase(String name);
}