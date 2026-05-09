package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
@Entity
@Table(name = "RISK_LEVEL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "minScore", "maxScore"})
public class RiskLevel {
 
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "risk_level_seq")
    @SequenceGenerator(name = "risk_level_seq", sequenceName = "RISK_LEVEL_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O nome do nível de risco é obrigatório")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    @Column(name = "NAME", length = 50, nullable = false, unique = true)
    private String name;
 
    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
    @Column(name = "DESCRIPTION", length = 200)
    private String description;
 
    @NotNull(message = "O score mínimo é obrigatório")
    @PositiveOrZero(message = "O score mínimo não pode ser negativo")
    @Column(name = "MIN_SCORE", nullable = false)
    private Integer minScore;
 
    @NotNull(message = "O score máximo é obrigatório")
    @Positive(message = "O score máximo deve ser positivo")
    @Column(name = "MAX_SCORE", nullable = false)
    private Integer maxScore;
}
 