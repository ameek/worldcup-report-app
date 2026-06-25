package com.fifa.reporting.worldcup_report_app.service;

import com.fifa.reporting.worldcup_report_app.dto.request.WorldCupRequestDTO;
import com.fifa.reporting.worldcup_report_app.dto.response.WorldCupResponseDTO;
import com.fifa.reporting.worldcup_report_app.dto.response.WorldCupSummaryDTO;
import com.fifa.reporting.worldcup_report_app.entity.WorldCup;
import com.fifa.reporting.worldcup_report_app.exception.ConflictException;
import com.fifa.reporting.worldcup_report_app.exception.ResourceNotFoundException;
import com.fifa.reporting.worldcup_report_app.mapper.WorldCupMapper;
import com.fifa.reporting.worldcup_report_app.repository.MatchRepository;
import com.fifa.reporting.worldcup_report_app.repository.WorldCupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorldCupService {

    private final WorldCupRepository worldCupRepository;
    private final MatchRepository matchRepository;
    private final WorldCupMapper worldCupMapper;

    @Transactional(readOnly = true)
    public List<WorldCupSummaryDTO> findAll() {
        return worldCupRepository.findAll().stream()
                .map(worldCupMapper::toSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorldCupResponseDTO findById(Long id) {
        return worldCupMapper.toResponseDTO(getWorldCupOrThrow(id));
    }

    @Transactional(readOnly = true)
    public WorldCupResponseDTO findByYear(Integer year) {
        return worldCupMapper.toResponseDTO(
                worldCupRepository.findByYear(year)
                        .orElseThrow(() -> new ResourceNotFoundException("World Cup", "year", year))
        );
    }

    @Transactional(readOnly = true)
    public List<WorldCupSummaryDTO> findByChampion(String champion) {
        return worldCupRepository.findByChampionIgnoreCase(champion).stream()
                .map(worldCupMapper::toSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorldCupSummaryDTO> findByHostCountry(String hostCountry) {
        return worldCupRepository.findByHostCountryIgnoreCase(hostCountry).stream()
                .map(worldCupMapper::toSummaryDTO)
                .toList();
    }

    @Transactional
    public WorldCupResponseDTO create(WorldCupRequestDTO dto) {
        if (worldCupRepository.existsByYear(dto.year())) {
            throw new ConflictException("World Cup for year " + dto.year() + " already exists");
        }
        WorldCup saved = worldCupRepository.save(worldCupMapper.toEntity(dto));
        log.info("Created World Cup: {}", saved.getYear());
        return worldCupMapper.toResponseDTO(saved);
    }

    @Transactional
    public WorldCupResponseDTO update(Long id, WorldCupRequestDTO dto) {
        WorldCup entity = getWorldCupOrThrow(id);
        if (worldCupRepository.existsByYearAndIdNot(dto.year(), id)) {
            throw new ConflictException("World Cup for year " + dto.year() + " already exists");
        }
        worldCupMapper.updateEntity(entity, dto);
        WorldCup saved = worldCupRepository.save(entity);
        log.info("Updated World Cup: {}", saved.getYear());
        return worldCupMapper.toResponseDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        WorldCup entity = getWorldCupOrThrow(id);
        if (matchRepository.existsByWorldCup_Id(id)) {
            throw new ConflictException("Cannot delete World Cup with existing matches");
        }
        worldCupRepository.delete(entity);
        log.info("Deleted World Cup: {}", entity.getYear());
    }

    private WorldCup getWorldCupOrThrow(Long id) {
        return worldCupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("World Cup", "id", id));
    }
}
