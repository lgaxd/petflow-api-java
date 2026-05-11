package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "REWARD_POINT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "points", "referenceType"})
public class RewardPoint {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotNull(message = "Os pontos são obrigatórios")
    @Column(name = "POINTS", nullable = false)
    private Integer points;
 
    @Size(max = 50, message = "O tipo de referência deve ter no máximo 50 caracteres")
    @Column(name = "REFERENCE_TYPE", length = 50)
    private String referenceType;

    @Column(name = "REFERENCE_ID")
    private Long referenceId;
 
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TUTOR_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_RP_TUTOR"))
    @NotNull(message = "O tutor é obrigatório")
    private Tutor tutor;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REWARD_ACTION_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_RP_ACTION"))
    @NotNull(message = "A ação de recompensa é obrigatória")
    private RewardAction rewardAction;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}