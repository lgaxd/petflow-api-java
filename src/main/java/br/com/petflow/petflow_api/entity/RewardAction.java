package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
@Entity
@Table(name = "REWARD_ACTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "pointsValue"})
public class RewardAction {
 
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reward_action_seq")
    @SequenceGenerator(name = "reward_action_seq", sequenceName = "REWARD_ACTION_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O nome da ação é obrigatório")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    @Column(name = "NAME", length = 50, nullable = false, unique = true)
    private String name;
 
    @NotNull(message = "O valor de pontos é obrigatório")
    @Positive(message = "O valor de pontos deve ser positivo")
    @Column(name = "POINTS_VALUE", nullable = false)
    private Integer pointsValue;
 
    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
    @Column(name = "DESCRIPTION", length = 200)
    private String description;
}
 