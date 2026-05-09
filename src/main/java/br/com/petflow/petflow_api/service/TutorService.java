package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.TutorRequestDTO;
import br.com.petflow.petflow_api.dto.TutorResponseDTO;
import br.com.petflow.petflow_api.entity.Tutor;
import br.com.petflow.petflow_api.exception.DuplicateResourceException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
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
public class TutorService {

    private final TutorRepository tutorRepository;

    @Transactional
    @CacheEvict(value = "tutors", allEntries = true)
    public TutorResponseDTO create(TutorRequestDTO request) {
        if (tutorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Tutor", "email", request.getEmail());
        }

        Tutor tutor = Tutor.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(encodePassword(request.getPassword()))
                .build();

        tutor = tutorRepository.save(tutor);
        return toResponseDTO(tutor);
    }

    @Cacheable(value = "tutors", key = "#id")
    public TutorResponseDTO findById(Long id) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tutor", id));
        return toResponseDTO(tutor);
    }

    public Page<TutorResponseDTO> findAll(Pageable pageable) {
        return tutorRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<TutorResponseDTO> findByName(String name, Pageable pageable) {
        return tutorRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toResponseDTO);
    }

    public TutorResponseDTO findByEmail(String email) {
        Tutor tutor = tutorRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Tutor", "email", email));
        return toResponseDTO(tutor);
    }

    @Transactional
    @CacheEvict(value = "tutors", key = "#id")
    public TutorResponseDTO update(Long id, TutorRequestDTO request) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tutor", id));

        if (!tutor.getEmail().equals(request.getEmail()) &&
                tutorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Tutor", "email", request.getEmail());
        }

        tutor.setName(request.getName());
        tutor.setEmail(request.getEmail());
        tutor.setPhone(request.getPhone());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            tutor.setPasswordHash(encodePassword(request.getPassword()));
        }

        tutor = tutorRepository.save(tutor);
        return toResponseDTO(tutor);
    }

    @Transactional
    @CacheEvict(value = "tutors", key = "#id")
    public void delete(Long id) {
        if (!tutorRepository.existsById(id)) {
            throw new EntityNotFoundException("Tutor", id);
        }
        tutorRepository.deleteById(id);
    }

    private TutorResponseDTO toResponseDTO(Tutor tutor) {
        return TutorResponseDTO.builder()
                .id(tutor.getId())
                .name(tutor.getName())
                .email(tutor.getEmail())
                .phone(tutor.getPhone())
                .createdAt(tutor.getCreatedAt())
                .build();
    }

    private String encodePassword(String rawPassword) {
        // Simplificado - em produção usar BCrypt ou similar
        return "{noop}" + rawPassword;
    }
}