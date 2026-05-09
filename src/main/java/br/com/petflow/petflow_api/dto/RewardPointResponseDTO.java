package br.com.petflow.petflow_api.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardPointResponseDTO {

    private Long id;
    private Integer points;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
    private Long tutorId;
    private String tutorName;
    private Long rewardActionId;
    private String rewardActionName;
}