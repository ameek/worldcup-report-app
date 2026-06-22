package com.fifa.reporting.worldcup_report_app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record WorldCupRequestDTO(
    @NotNull(message = "Year is required")
    @Min(value = 1930, message = "First World Cup was in 1930")
    Integer year,
    
    @NotBlank(message = "Host country is required")
    String hostCountry,
    
    @NotBlank(message = "Champion is required")
    String champion,
    
    String runnerUp,
    
    @Min(value = 8, message = "Minimum 8 teams")
    Integer totalTeams,
    
    @Min(value = 1, message = "Minimum 1 match")
    Integer totalMatches,
    
    @Min(value = 1, message = "Minimum 1 goal")
    Integer totalGoals,
    
    String topScorer,
    
    @Min(value = 1, message = "Minimum 1 goal")
    Integer topScorerGoals,
    
    @PastOrPresent(message = "Start date must be in the past or present")
    LocalDate startDate,
    
    @PastOrPresent(message = "End date must be in the past or present")
    LocalDate endDate
) {
    // Compact constructor for custom validation
    public WorldCupRequestDTO {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }
}
