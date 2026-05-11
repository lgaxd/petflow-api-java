package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PET")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
public class Pet {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O nome do pet é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Column(name = "NAME", length = 100, nullable = false)
    private String name;
 
    @Size(max = 50, message = "A raça deve ter no máximo 50 caracteres")
    @Column(name = "BREED", length = 50)
    private String breed;
 
    @Past(message = "A data de nascimento deve ser uma data passada")
    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;
 
    @DecimalMin(value = "0.0", inclusive = false, message = "O peso deve ser positivo")
    @Digits(integer = 3, fraction = 2, message = "Formato de peso inválido (máximo 5 dígitos, 2 decimais)")
    @Column(name = "WEIGHT", precision = 5, scale = 2)
    private BigDecimal weight;
 
    @Column(name = "SPECIES", length = 50)
    private String species;
 
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TUTOR_ID", nullable = false)
    private Tutor tutor;
 
    @JsonIgnore
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HealthEvent> healthEvents = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}