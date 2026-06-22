package com.fifa.reporting.worldcup_report_app.dto.response;

public record WorldCupSummaryDTO(
    Integer year,
    String hostCountry,
    String champion,
    Integer totalTeams
) {}
