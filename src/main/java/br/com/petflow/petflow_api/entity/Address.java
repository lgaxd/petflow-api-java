package br.com.petflow.petflow_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
@Entity
@Table(name = "ADDRESS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "city", "state"})
public class Address {
 
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq")
    @SequenceGenerator(name = "address_seq", sequenceName = "ADDRESS_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;
 
    @NotBlank(message = "O logradouro é obrigatório")
    @Size(max = 200, message = "O logradouro deve ter no máximo 200 caracteres")
    @Column(name = "STREET", length = 200, nullable = false)
    private String street;
 
    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
    @Column(name = "CITY", length = 100, nullable = false)
    private String city;
 
    @NotBlank(message = "O estado é obrigatório")
    @Size(max = 50, message = "O estado deve ter no máximo 50 caracteres")
    @Column(name = "STATE", length = 50, nullable = false)
    private String state;
 
    @NotBlank(message = "O CEP é obrigatório")
    @Size(max = 10, message = "O CEP deve ter no máximo 10 caracteres")
    @Column(name = "ZIP_CODE", length = 10, nullable = false)
    private String zipCode;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TUTOR_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_ADDRESS_TUTOR"))
    @NotNull(message = "O tutor é obrigatório")
    private Tutor tutor;
}