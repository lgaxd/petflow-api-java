package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "SUBSCRIPTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "status"})
public class Subscription {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotNull(message = "A data de início é obrigatória")
    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;
 
    @Column(name = "END_DATE")
    private LocalDate endDate;

    @NotBlank(message = "O status é obrigatório")
    @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
    @Column(name = "STATUS", length = 20, nullable = false)
    private String status;
 
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PET_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_SUB_PET"))
    @NotNull(message = "O pet é obrigatório")
    private Pet pet;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLAN_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_SUB_PLAN"))
    @NotNull(message = "O plano é obrigatório")
    private Plan plan;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ATIVO";
        }
    }
}
 