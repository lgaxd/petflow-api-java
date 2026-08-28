package br.com.petflow.petflow_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryDTO {
    private Long id;
    private Integer points;
    private String reason;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
}