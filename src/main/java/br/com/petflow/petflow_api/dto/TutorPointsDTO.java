package br.com.petflow.petflow_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorPointsDTO {
    private Long tutorId;
    private String tutorName;
    private Integer totalPoints;
    private List<PointHistoryDTO> history;
}