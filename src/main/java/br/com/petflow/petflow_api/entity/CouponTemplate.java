package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.math.BigDecimal;
 
@Entity
@Table(name = "COUPON_TEMPLATE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "title", "discountType"})
public class CouponTemplate {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O título é obrigatório")
    @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
    @Column(name = "TITLE", length = 100, nullable = false)
    private String title;
 
    @NotNull(message = "O valor do desconto é obrigatório")
    @DecimalMin(value = "0.01", message = "O desconto deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Formato inválido (máximo 10 dígitos, 2 decimais)")
    @Column(name = "DISCOUNT_VALUE", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountValue;
 
    @NotBlank(message = "O tipo de desconto é obrigatório")
    @Size(max = 20, message = "O tipo deve ter no máximo 20 caracteres")
    @Column(name = "DISCOUNT_TYPE", length = 20, nullable = false)
    private String discountType;
 
    @NotNull(message = "Os pontos necessários são obrigatórios")
    @Positive(message = "Os pontos necessários devem ser positivos")
    @Column(name = "POINTS_REQUIRED", nullable = false)
    private Integer pointsRequired;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTNER_DISCOUNT_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_CT_PARTNER"))
    @NotNull(message = "O desconto de parceiro é obrigatório")
    private PartnerDiscount partnerDiscount;
}