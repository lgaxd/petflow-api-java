package br.com.petflow.petflow_api.entity;

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

    @NotBlank(message = "O status é obrigatório")
    @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
    @Column(name = "STATUS", length = 20, nullable = false)
    private String status;
 
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PET_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_HE_PET"))
    @NotNull(message = "O pet é obrigatório")
    private Pet pet;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_TYPE_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_HE_EVENT_TYPE"))
    @NotNull(message = "O tipo de evento é obrigatório")
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