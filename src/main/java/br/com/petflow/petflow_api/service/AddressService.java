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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço", id));
        return toResponseDTO(address);
    }

    public List<AddressResponseDTO> findAll() {
        return addressRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AddressResponseDTO> findByCity(String city) {
        return addressRepository.findByCityIgnoreCase(city).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AddressResponseDTO> findByState(String state) {
        return addressRepository.findByStateIgnoreCase(state).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AddressResponseDTO> findByTutorId(Long tutorId) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new EntityNotFoundException("Tutor", tutorId);
        }
        return addressRepository.findByTutorId(tutorId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
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