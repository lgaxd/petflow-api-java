package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
@Entity
@Table(name = "EVENT_TYPE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "category"})
public class EventType {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O nome do tipo de evento é obrigatório")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    @Column(name = "NAME", length = 50, nullable = false, unique = true)
    private String name;
 
    @NotNull(message = "O valor de pontos é obrigatório")
    @PositiveOrZero(message = "Os pontos de recompensa não podem ser negativos")
    @Column(name = "POINTS_REWARD", nullable = false)
    private Integer pointsReward;
 
    @NotBlank(message = "A categoria é obrigatória")
    @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres")
    @Column(name = "CATEGORY", length = 50, nullable = false)
    private String category;
}