package com.fifa.reporting.worldcup_report_app.dto.response;

import java.time.LocalDate;

public record WorldCupResponseDTO(
    Long id,
    Integer year,
    String hostCountry,
    String champion,
    String runnerUp,
    Integer totalTeams,
    Integer totalMatches,
    Integer totalGoals,
    String topScorer,
    Integer topScorerGoals,
    LocalDate startDate,
    LocalDate endDate
) {}
