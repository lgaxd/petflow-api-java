package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.AddressRequestDTO;
import br.com.petflow.petflow_api.dto.AddressResponseDTO;
import br.com.petflow.petflow_api.entity.Address;
import br.com.petflow.petflow_api.entity.Tutor;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.AddressRepository;
import br.com.petflow.petflow_api.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final TutorRepository tutorRepository;

    @Transactional
    @CacheEvict(value = "addresses", allEntries = true)
    public AddressResponseDTO create(AddressRequestDTO request) {
        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));

        Address address = Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .tutor(tutor)
                .build();

        address = addressRepository.save(address);
        return toResponseDTO(address);
    }

    @Cacheable(value = "addresses", key = "#id")
    public AddressResponseDTO findById(Long id) {
        return addressRepository.findProjectedById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço", id));
    }

    public Page<AddressResponseDTO> findAll(Pageable pageable) {
        return addressRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<AddressResponseDTO> findByCity(String city, Pageable pageable) {
        return addressRepository.findByCityIgnoreCase(city, pageable);
    }

    public Page<AddressResponseDTO> findByState(String state, Pageable pageable) {
        return addressRepository.findByStateIgnoreCase(state, pageable);
    }

    public Page<AddressResponseDTO> findByTutorId(Long tutorId, Pageable pageable) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new EntityNotFoundException("Tutor", tutorId);
        }
        return addressRepository.findByTutorId(tutorId, pageable);
    }

    @Transactional
    @CacheEvict(value = "addresses", key = "#id")
    public AddressResponseDTO update(Long id, AddressRequestDTO request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço", id));

        if (!address.getTutor().getId().equals(request.getTutorId())) {
            Tutor tutor = tutorRepository.findById(request.getTutorId())
                    .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));
            address.setTutor(tutor);
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());

        address = addressRepository.save(address);
        return toResponseDTO(address);
    }

    @Transactional
    @CacheEvict(value = "addresses", key = "#id")
    public void delete(Long id) {
        if (!addressRepository.existsById(id)) {
            throw new EntityNotFoundException("Endereço", id);
        }
        addressRepository.deleteById(id);
    }

    private AddressResponseDTO toResponseDTO(Address address) {
        return AddressResponseDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .tutorId(address.getTutor().getId())
                .build();
    }
}