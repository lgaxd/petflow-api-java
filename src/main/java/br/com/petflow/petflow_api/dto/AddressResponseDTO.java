package br.com.petflow.petflow_api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDTO {

    private Long id;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private Long tutorId;
}