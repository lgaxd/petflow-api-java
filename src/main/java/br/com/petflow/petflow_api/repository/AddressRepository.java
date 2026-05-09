package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCityIgnoreCase(String city);

    List<Address> findByStateIgnoreCase(String state);

    List<Address> findByTutorId(Long tutorId);
}