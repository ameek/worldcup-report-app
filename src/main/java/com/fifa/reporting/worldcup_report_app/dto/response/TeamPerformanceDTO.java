package com.fifa.reporting.worldcup_report_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Match row for the match-history table subDataset (7+ fields including dates).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformanceDTO {
    private Integer year;
    private LocalDate matchDate;
    private String opponent;
    private String opponentFlagUrl;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private String result;
    private String stage;
    private String stadium;
}
