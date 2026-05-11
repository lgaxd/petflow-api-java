package br.com.petflow.petflow_api.entity;

import br.com.petflow.petflow_api.enums.EventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "HEALTH_EVENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "status", "eventDate"})
public class HealthEvent {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
    @Column(name = "DESCRIPTION", length = 200)
    private String description;
 
    @NotNull(message = "A data do evento é obrigatória")
    @Column(name = "EVENT_DATE", nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HealthEventStatus status;

 
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PET_ID", nullable = false)
    private Pet pet;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_TYPE", nullable = false)
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLINIC_ID", foreignKey = @ForeignKey(name = "FK_HE_CLINIC"))
    private Clinic clinic;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "AGENDADO";
        }
    }
}