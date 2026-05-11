package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "COUPON")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "code", "status"})
public class Coupon {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O código do cupom é obrigatório")
    @Size(max = 50, message = "O código deve ter no máximo 50 caracteres")
    @Column(name = "CODE", length = 50, nullable = false, unique = true)
    private String code;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;
 
    @Future(message = "A data de expiração deve ser uma data futura")
    @Column(name = "EXPIRATION_DATE")
    private LocalDate expirationDate;

    @NotNull(message = "O valor do desconto é obrigatório")
    @DecimalMin(value = "0.01", message = "O desconto deve ser maior que zero")
    @Column(name = "DISCOUNT_VALUE", nullable = false)
    private BigDecimal discountValue;

    @NotNull(message = "Os pontos necessários são obrigatórios")
    @Positive(message = "Os pontos necessários devem ser positivos")
    @Column(name = "POINTS_REQUIRED", nullable = false)
    private Integer pointsRequired;
 
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "DISPONIVEL";
        }
    }
}