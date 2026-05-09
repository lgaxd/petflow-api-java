package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coupon_seq")
    @SequenceGenerator(name = "coupon_seq", sequenceName = "COUPON_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O código do cupom é obrigatório")
    @Size(max = 50, message = "O código deve ter no máximo 50 caracteres")
    @Column(name = "CODE", length = 50, nullable = false, unique = true)
    private String code;
 
    /*
     * Valores esperados: DISPONIVEL, RESGATADO, EXPIRADO
     */
    @NotBlank(message = "O status é obrigatório")
    @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
    @Column(name = "STATUS", length = 20, nullable = false)
    private String status;
 
    @Future(message = "A data de expiração deve ser uma data futura")
    @Column(name = "EXPIRATION_DATE")
    private LocalDate expirationDate;
 
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_COUPON_TEMPLATE"))
    @NotNull(message = "O template é obrigatório")
    private CouponTemplate template;
 
    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "DISPONIVEL";
        }
    }
}