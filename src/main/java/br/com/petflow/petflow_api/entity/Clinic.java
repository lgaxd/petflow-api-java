package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
@Entity
@Table(name = "CLINIC")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name", "cnpj"})
public class Clinic {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O nome da clínica é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Column(name = "NAME", length = 100, nullable = false)
    private String name;
 
    @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres")
    @Column(name = "ADDRESS", length = 200)
    private String address;
 
    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    @Column(name = "PHONE", length = 20)
    private String phone;
 
    @NotBlank(message = "O CNPJ é obrigatório")
    @Size(min = 18, max = 18, message = "O CNPJ deve ter 18 caracteres (ex: 00.000.000/0000-00)")
    @Column(name = "CNPJ", length = 18, nullable = false, unique = true)
    private String cnpj;
 
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;
 
    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Plan> plans = new ArrayList<>();
 
    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PartnerDiscount> partnerDiscounts = new ArrayList<>();
 
    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}