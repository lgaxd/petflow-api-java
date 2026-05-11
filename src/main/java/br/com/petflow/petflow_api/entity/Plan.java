package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.math.BigDecimal;
 
@Entity
@Table(name = "PLAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "price"})
public class Plan {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O nome do plano é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Column(name = "NAME", length = 100, nullable = false)
    private String name;
 
    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
    @Column(name = "DESCRIPTION", length = 200)
    private String description;
 
    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser positivo")
    @Digits(integer = 8, fraction = 2, message = "Formato de preço inválido (máximo 10 dígitos, 2 decimais)")
    @Column(name = "PRICE", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
 
    @NotNull(message = "A duração em dias é obrigatória")
    @Positive(message = "A duração deve ser positiva")
    @Column(name = "DURATION_DAYS", nullable = false)
    private Integer durationDays;
 
    @NotNull(message = "Os pontos por evento são obrigatórios")
    @PositiveOrZero(message = "Os pontos por evento não podem ser negativos")
    @Column(name = "POINTS_PER_EVENT", nullable = false)
    private Integer pointsPerEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLINIC_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_PLAN_CLINIC"))
    @NotNull(message = "A clínica é obrigatória")
    private Clinic clinic;
}