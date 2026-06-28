package com.fifa.reporting.worldcup_report_app.controller;

import com.fifa.reporting.worldcup_report_app.dto.request.WorldCupRequestDTO;
import com.fifa.reporting.worldcup_report_app.dto.response.WorldCupResponseDTO;
import com.fifa.reporting.worldcup_report_app.dto.response.WorldCupSummaryDTO;
import com.fifa.reporting.worldcup_report_app.service.WorldCupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/worldcups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "World Cups", description = "World Cup tournament CRUD and lookup operations")
public class WorldCupController {

    private final WorldCupService worldCupService;

    @GetMapping
    @Operation(summary = "List all World Cups")
    public ResponseEntity<List<WorldCupSummaryDTO>> getAll() {
        log.debug("Fetching all World Cups");
        return ResponseEntity.ok(worldCupService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorldCupResponseDTO> getById(@PathVariable Long id) {
        log.debug("Fetching World Cup by id: {}", id);
        return ResponseEntity.ok(worldCupService.findById(id));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<WorldCupResponseDTO> getByYear(@PathVariable Integer year) {
        log.debug("Fetching World Cup by year: {}", year);
        return ResponseEntity.ok(worldCupService.findByYear(year));
    }

    @GetMapping("/champion/{team}")
    public ResponseEntity<List<WorldCupSummaryDTO>> getByChampion(@PathVariable String team) {
        log.debug("Fetching World Cups won by: {}", team);
        return ResponseEntity.ok(worldCupService.findByChampion(team));
    }

    @GetMapping("/host/{country}")
    public ResponseEntity<List<WorldCupSummaryDTO>> getByHostCountry(@PathVariable String country) {
        log.debug("Fetching World Cups hosted by: {}", country);
        return ResponseEntity.ok(worldCupService.findByHostCountry(country));
    }

    @PostMapping
    public ResponseEntity<WorldCupResponseDTO> create(@Valid @RequestBody WorldCupRequestDTO dto) {
        log.info("Creating World Cup for year: {}", dto.year());
        WorldCupResponseDTO created = worldCupService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorldCupResponseDTO> update(@PathVariable Long id,
                                                       @Valid @RequestBody WorldCupRequestDTO dto) {
        log.info("Updating World Cup id: {}", id);
        return ResponseEntity.ok(worldCupService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting World Cup id: {}", id);
        worldCupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
