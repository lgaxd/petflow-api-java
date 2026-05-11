package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.math.BigDecimal;
 
@Entity
@Table(name = "PARTNER_DISCOUNT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "partnerName", "category"})
public class PartnerDiscount {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O nome do parceiro é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Column(name = "PARTNER_NAME", length = 100, nullable = false)
    private String partnerName;
 
    @NotBlank(message = "A categoria é obrigatória")
    @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres")
    @Column(name = "CATEGORY", length = 50, nullable = false)
    private String category;
 
    @NotNull(message = "O percentual de desconto é obrigatório")
    @DecimalMin(value = "0.01", message = "O desconto deve ser maior que zero")
    @DecimalMax(value = "100.00", message = "O desconto não pode ultrapassar 100%")
    @Digits(integer = 3, fraction = 2, message = "Formato inválido (máximo 5 dígitos, 2 decimais)")
    @Column(name = "DISCOUNT_PERCENT", precision = 5, scale = 2, nullable = false)
    private BigDecimal discountPercent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLINIC_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_PD_CLINIC"))
    @NotNull(message = "A clínica é obrigatória")
    private Clinic clinic;
}
 