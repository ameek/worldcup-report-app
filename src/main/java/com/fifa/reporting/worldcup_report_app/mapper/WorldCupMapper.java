package com.fifa.reporting.worldcup_report_app.mapper;

import com.fifa.reporting.worldcup_report_app.dto.request.WorldCupRequestDTO;
import com.fifa.reporting.worldcup_report_app.dto.response.WorldCupResponseDTO;
import com.fifa.reporting.worldcup_report_app.dto.response.WorldCupSummaryDTO;
import com.fifa.reporting.worldcup_report_app.entity.WorldCup;
import org.springframework.stereotype.Component;

@Component
public class WorldCupMapper {

    // Entity → Response DTO (Record)
    public WorldCupResponseDTO toResponseDTO(WorldCup entity) {
        if (entity == null) return null;
        
        return new WorldCupResponseDTO(
            entity.getId(),
            entity.getYear(),
            entity.getHostCountry(),
            entity.getChampion(),
            entity.getRunnerUp(),
            entity.getTotalTeams(),
            entity.getTotalMatches(),
            entity.getTotalGoals(),
            entity.getTopScorer(),
            entity.getTopScorerGoals(),
            entity.getStartDate(),
            entity.getEndDate()
        );
    }

    // Entity → Summary DTO (Record)
    public WorldCupSummaryDTO toSummaryDTO(WorldCup entity) {
        if (entity == null) return null;
        
        return new WorldCupSummaryDTO(
            entity.getYear(),
            entity.getHostCountry(),
            entity.getChampion(),
            entity.getTotalTeams()
        );
    }

    // Request DTO (Record) → Entity
    public WorldCup toEntity(WorldCupRequestDTO dto) {
        if (dto == null) return null;
        
        return WorldCup.builder()
                .year(dto.year())
                .hostCountry(dto.hostCountry())
                .champion(dto.champion())
                .runnerUp(dto.runnerUp())
                .totalTeams(dto.totalTeams())
                .totalMatches(dto.totalMatches())
                .totalGoals(dto.totalGoals())
                .topScorer(dto.topScorer())
                .topScorerGoals(dto.topScorerGoals())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .build();
    }

    // Update entity from Request DTO (Record)
    public void updateEntity(WorldCup entity, WorldCupRequestDTO dto) {
        if (dto == null) return;
        
        entity.setYear(dto.year());
        entity.setHostCountry(dto.hostCountry());
        entity.setChampion(dto.champion());
        entity.setRunnerUp(dto.runnerUp());
        entity.setTotalTeams(dto.totalTeams());
        entity.setTotalMatches(dto.totalMatches());
        entity.setTotalGoals(dto.totalGoals());
        entity.setTopScorer(dto.topScorer());
        entity.setTopScorerGoals(dto.topScorerGoals());
        entity.setStartDate(dto.startDate());
        entity.setEndDate(dto.endDate());
    }
}
