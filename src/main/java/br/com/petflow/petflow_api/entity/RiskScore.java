package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "RISK_SCORE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "score", "calculatedAt"})
public class RiskScore {
 
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "risk_score_seq")
    @SequenceGenerator(name = "risk_score_seq", sequenceName = "RISK_SCORE_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;
 
    @NotNull(message = "O score é obrigatório")
    @PositiveOrZero(message = "O score não pode ser negativo")
    @Column(name = "SCORE", nullable = false)
    private Integer score;
 
    @Column(name = "CALCULATED_AT", updatable = false)
    private LocalDateTime calculatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PET_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_RS_PET"))
    @NotNull(message = "O pet é obrigatório")
    private Pet pet;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RISK_LEVEL_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_RS_LEVEL"))
    @NotNull(message = "O nível de risco é obrigatório")
    private RiskLevel riskLevel;

    @PrePersist
    private void prePersist() {
        this.calculatedAt = LocalDateTime.now();
    }
}
 