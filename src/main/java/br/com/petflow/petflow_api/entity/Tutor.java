package br.com.petflow.petflow_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TUTOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"pets", "redeems"})
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @NotBlank @Email @Size(max = 100)
    @Column(unique = true, nullable = false)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank
    @Column(name = "PASSWORD_HASH", nullable = false)
    private String passwordHash;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pet> pets = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Redeem> redeems = new ArrayList<>();
}