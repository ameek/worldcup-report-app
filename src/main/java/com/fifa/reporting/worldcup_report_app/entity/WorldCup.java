package com.fifa.reporting.worldcup_report_app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "world_cups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldCup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year", nullable = false, unique = true)
    @NotNull(message = "Year is required")
    @Min(value = 1930, message = "First World Cup was in 1930")
    private Integer year;

    @Column(name = "host_country", nullable = false)
    @NotBlank(message = "Host country is required")
    private String hostCountry;

    @Column(name = "champion", nullable = false)
    @NotBlank(message = "Champion is required")
    private String champion;

    @Column(name = "runner_up")
    private String runnerUp;

    @Column(name = "total_teams")
    @Min(value = 8, message = "Minimum 8 teams")
    private Integer totalTeams;

    @Column(name = "total_matches")
    private Integer totalMatches;

    @Column(name = "total_goals")
    private Integer totalGoals;

    @Column(name = "top_scorer")
    private String topScorer;

    @Column(name = "top_scorer_goals")
    private Integer topScorerGoals;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}