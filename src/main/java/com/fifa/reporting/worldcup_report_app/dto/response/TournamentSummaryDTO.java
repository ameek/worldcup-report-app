package com.fifa.reporting.worldcup_report_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Row model for the tournament participation subDataset (separate data source).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentSummaryDTO {
    private Integer year;
    private String hostCountry;
    private LocalDate tournamentStart;
    private LocalDate tournamentEnd;
    private String role;
    private String hostFlagUrl;
}
