package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
 
@Entity
@Table(name = "REDEEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "pointsUsed"})
public class Redeem {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotNull(message = "Os pontos utilizados são obrigatórios")
    @Positive(message = "Os pontos utilizados devem ser positivos")
    @Column(name = "POINTS_USED", nullable = false)
    private Integer pointsUsed;
 
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TUTOR_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_REDEEM_TUTOR"))
    @NotNull(message = "O tutor é obrigatório")
    private Tutor tutor;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUPON_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_REDEEM_COUPON"))
    @NotNull(message = "O cupom é obrigatório")
    private Coupon coupon;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}